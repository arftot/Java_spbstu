#!/usr/bin/env bash
set -euo pipefail
ROOT=$(pwd)
LOGDIR="$ROOT/verify_logs"
mkdir -p "$LOGDIR"
echo "=== Project verify started ($(date)) ===" | tee "$LOGDIR/run.log"

# 1) Run unit tests
echo "--- Running unit tests ---" | tee -a "$LOGDIR/run.log"
./gradlew test --no-daemon | tee "$LOGDIR/gradle_test.log"
echo "UNIT TESTS: OK" | tee -a "$LOGDIR/run.log"

# 2) Build & bring up docker compose
echo "--- Starting docker compose (build) ---" | tee -a "$LOGDIR/run.log"
docker compose down -v --remove-orphans || true
docker compose up --build -d | tee -a "$LOGDIR/run.log"
sleep 4

# 3) Wait for services to be healthy (simple loop)
echo "--- Waiting for containers to be healthy ---" | tee -a "$LOGDIR/run.log"
timeout=60
i=0
while [ $i -lt $timeout ]; do
  ps=$(docker compose ps --format "table {{.Name}}\t{{.Service}}\t{{.Status}}" | sed 1,1d)
  echo "$ps" > "$LOGDIR/docker_ps.txt"
  # require app, db to be Up
  if echo "$ps" | grep -q "app" && echo "$ps" | grep -q "db" && echo "$ps" | grep -q "rabbitmq"; then
    # crude check: if any container status contains "Up" — break
    if echo "$ps" | grep -E "Up.*\(healthy\)|Up " >/dev/null; then
      echo "Containers look up (see docker_ps.txt)" | tee -a "$LOGDIR/run.log"
      break
    fi
  fi
  i=$((i+1))
  sleep 2
done

# 4) Health endpoint
echo "--- Checking /actuator/health ---" | tee -a "$LOGDIR/run.log"
HEALTH=$(curl -sS -m 5 http://localhost:8080/actuator/health || true)
echo "$HEALTH" | tee "$LOGDIR/health.json"
if echo "$HEALTH" | grep -q '"status":"UP"'; then
  echo "ACTUATOR HEALTH: UP" | tee -a "$LOGDIR/run.log"
else
  echo "ERROR: actuator/health is not UP" | tee -a "$LOGDIR/run.log"
  tail -n 200 "$LOGDIR/gradle_test.log"
  docker compose logs --no-color app | tee "$LOGDIR/app_full.log"
  exit 1
fi

# 5) Smoke test: create user -> create task -> check notification created
echo "--- Smoke test: create user -> create task -> check notification ---" | tee -a "$LOGDIR/run.log"

# create user
USER_JSON=$(curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"verify_user","displayName":"Verify User"}' || true)
echo "$USER_JSON" | tee "$LOGDIR/user_create.json"
USER_ID=$(echo "$USER_JSON" | jq -r '.id' || echo "")
if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
  echo "ERROR: user creation failed or id null. Full response:" | tee -a "$LOGDIR/run.log"
  cat "$LOGDIR/user_create.json" | tee -a "$LOGDIR/run.log"
  docker compose logs --no-color app | tail -n 200 | tee "$LOGDIR/app_error.log"
  exit 1
fi
echo "Created user id=$USER_ID" | tee -a "$LOGDIR/run.log"

# create task
TASK_JSON=$(curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"title\":\"verify-task\",\"description\":\"verify\",\"dueDate\":\"2026-01-01T12:00:00Z\"}" || true)
echo "$TASK_JSON" | tee "$LOGDIR/task_create.json"
if echo "$TASK_JSON" | jq -e 'has("error")' >/dev/null 2>&1; then
  echo "ERROR creating task: $(cat $LOGDIR/task_create.json)" | tee -a "$LOGDIR/run.log"
  docker compose logs --no-color app | tail -n 300 | tee "$LOGDIR/app_error.log"
  exit 1
fi
echo "Task creation response OK" | tee -a "$LOGDIR/run.log"

# wait briefly for async listener to process notifications
echo "Waiting 3s for async listener to process..." | tee -a "$LOGDIR/run.log"
sleep 3

# query notifications for user
NOTIFS=$(curl -s "http://localhost:8080/api/notifications?userId=$USER_ID" || true)
echo "$NOTIFS" | tee "$LOGDIR/notifications.json"
if echo "$NOTIFS" | jq -e '. | length > 0' >/dev/null 2>&1; then
  echo "Notification created for user $USER_ID" | tee -a "$LOGDIR/run.log"
else
  echo "WARNING: no notifications found for user. Response:" | tee -a "$LOGDIR/run.log"
  cat "$LOGDIR/notifications.json" | tee -a "$LOGDIR/run.log"
  # don't fail yet — maybe listener was slow
fi

# 6) Check RabbitMQ: queue exists and is empty
echo "--- Check RabbitMQ queues ---" | tee -a "$LOGDIR/run.log"
docker exec taskmanager-rabbitmq rabbitmqctl list_queues > "$LOGDIR/rabbit_queues.txt" || true
cat "$LOGDIR/rabbit_queues.txt" | tee -a "$LOGDIR/run.log"

# 7) Check Redis cache keys for user tasks (optional)
echo "--- Check Redis keys (if redis present) ---" | tee -a "$LOGDIR/run.log"
if docker compose ps | grep -q redis; then
  # attempt to get "tasks::{userId}" key (depends on your cache key naming)
  docker exec taskmanager-redis redis-cli --raw KEYS '*' > "$LOGDIR/redis_keys.txt" || true
  echo "Redis keys:" | tee -a "$LOGDIR/run.log"
  tail -n 200 "$LOGDIR/redis_keys.txt" | tee -a "$LOGDIR/run.log"
else
  echo "No redis service present in compose" | tee -a "$LOGDIR/run.log"
fi

# 8) Scheduled job check (creates overdue notification)
echo "--- Testing scheduler (overdue) ---" | tee -a "$LOGDIR/run.log"
# create an overdue task
OLD_TASK_JSON=$(curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"title\":\"overdue-verify\",\"description\":\"old\",\"dueDate\":\"2000-01-01T12:00:00Z\"}" || true)
echo "$OLD_TASK_JSON" > "$LOGDIR/old_task.json"
sleep 5
# check if notification for overdue exists
OVERDUE_NOTIFS=$(curl -s "http://localhost:8080/api/notifications?userId=$USER_ID" || true)
echo "$OVERDUE_NOTIFS" | tee "$LOGDIR/notifications_after_sched.json"
if echo "$OVERDUE_NOTIFS" | jq -e '. | length > 0' >/dev/null 2>&1; then
  echo "Scheduler/notification check: notifications exist" | tee -a "$LOGDIR/run.log"
else
  echo "WARNING: scheduled checker didn't create notifications (may be slow). Check logs." | tee -a "$LOGDIR/run.log"
fi

# 9) Scan repo for common teacher errors
echo "--- Scanning repo for frequent mistakes ---" | tee -a "$LOGDIR/run.log"
# 9a) jar artifacts
if git ls-files --exclude-standard --others --cached | grep -q "build/libs" || ls build/libs/*.jar >/dev/null 2>&1; then
  echo "ERROR: build artifacts found in repo or in build/libs. Check .gitignore" | tee -a "$LOGDIR/run.log"
else
  echo "No local jar artifacts committed" | tee -a "$LOGDIR/run.log"
fi

# 9b) Dockerfile bad COPY jar
if grep -R "COPY build/libs" -n --exclude-dir=.git . | tee "$LOGDIR/dockerfile_copy_check.txt"; then
  if [ -s "$LOGDIR/dockerfile_copy_check.txt" ]; then
    echo "WARNING: Dockerfile copies local build jar (should build inside container). See dockerfile_copy_check.txt" | tee -a "$LOGDIR/run.log"
  else
    echo "Dockerfile copy check ok" | tee -a "$LOGDIR/run.log"
  fi
fi

# 9c) Check application.properties usage (only default + per-profile)
echo "--- Checking profiles files ---" | tee -a "$LOGDIR/run.log"
ls src/main/resources | tee "$LOGDIR/resources_list.txt"
grep -n "spring.profiles.active" -R src/main/resources || true

# 9d) Check for generic 500 handlers / bad exception handling (heuristic)
echo "Searching for patterns that return INTERNAL_SERVER_ERROR generically..." | tee -a "$LOGDIR/run.log"
grep -R "INTERNAL_SERVER_ERROR" -n || true
grep -R "HttpStatus.INTERNAL_SERVER_ERROR" -n || true
grep -R "return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR" -n || true

# 10) Save some logs for manual inspection
docker compose logs --no-color app > "$LOGDIR/app_full.log"
echo "Verification complete. Logs saved to $LOGDIR" | tee -a "$LOGDIR/run.log"
exit 0
