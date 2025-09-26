# TaskManager - Spring Boot Application

A task management application built with Spring Boot, supporting both in-memory and database storage.

## Step 3: H2 Database + Spring Data JPA Integration

This step adds H2 database support with Spring Data JPA while maintaining backward compatibility with in-memory storage.

### New Files Added

#### JPA Entities
- `src/main/java/com/alina/taskmanager/entity/TaskEntity.java`
- `src/main/java/com/alina/taskmanager/entity/UserEntity.java`
- `src/main/java/com/alina/taskmanager/entity/NotificationEntity.java`

#### Spring Data JPA Repositories
- `src/main/java/com/alina/taskmanager/repository/jpa/SpringTaskJpaRepository.java`
- `src/main/java/com/alina/taskmanager/repository/jpa/SpringUserJpaRepository.java`
- `src/main/java/com/alina/taskmanager/repository/jpa/SpringNotificationJpaRepository.java`

#### Repository Adapters
- `src/main/java/com/alina/taskmanager/repository/adapter/TaskRepositoryAdapter.java`
- `src/main/java/com/alina/taskmanager/repository/adapter/UserRepositoryAdapter.java`
- `src/main/java/com/alina/taskmanager/repository/adapter/NotificationRepositoryAdapter.java`

#### Configuration Files
- `src/main/resources/application-inmemory.properties`
- `src/main/resources/application-db.properties`

#### Tests
- `src/test/java/com/alina/taskmanager/repository/jpa/SpringTaskJpaRepositoryTest.java`
- `src/test/java/com/alina/taskmanager/repository/jpa/SpringUserJpaRepositoryTest.java`
- `src/test/java/com/alina/taskmanager/repository/jpa/SpringNotificationJpaRepositoryTest.java`
- `src/test/java/com/alina/taskmanager/integration/TaskServiceDbIntegrationTest.java`

### Running the Application

#### With In-Memory Storage (Default)
```bash
./gradlew bootRun
```
or
```bash
./gradlew bootRun --args='--spring.profiles.active=inmemory'
```

#### With H2 Database
```bash
./gradlew bootRun --args='--spring.profiles.active=db'
```

#### With PostgreSQL Database
```bash
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

## Docker

The application supports Docker deployment with PostgreSQL database.

### Prerequisites
- Docker and Docker Compose installed
- Ports 8080 and 5432 available

### Quick Start

#### Build and Run with Docker Compose
```bash
# Build and start all services in background
docker compose up --build -d

# View application logs
docker compose logs -f app

# View database logs
docker compose logs -f db

# Stop all services and remove volumes
docker compose down -v
```

#### Manual Docker Commands
```bash
# Build the application image
docker build -t taskmanager-app .

# Run PostgreSQL database
docker run -d --name taskmanager-db \
  -e POSTGRES_USER=taskuser \
  -e POSTGRES_PASSWORD=taskpass \
  -e POSTGRES_DB=taskdb \
  -p 5432:5432 \
  postgres:15

# Run the application
docker run -d --name taskmanager-app \
  --link taskmanager-db:db \
  -e SPRING_PROFILES_ACTIVE=postgres \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/taskdb \
  -e SPRING_DATASOURCE_USERNAME=taskuser \
  -e SPRING_DATASOURCE_PASSWORD=taskpass \
  -p 8080:8080 \
  taskmanager-app
```

### Docker Services

#### Application Service
- **Image**: Built from local Dockerfile
- **Port**: 8080
- **Profile**: `postgres` (uses PostgreSQL database)
- **Health Check**: `/actuator/health` endpoint

#### Database Service
- **Image**: postgres:15
- **Port**: 5432
- **Database**: taskdb
- **User**: taskuser
- **Password**: taskpass

### Testing the Application

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Test API endpoints
curl http://localhost:8080/api/tasks?userId=test-user

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser"}'

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-id",
    "title": "Test Task",
    "description": "Test Description",
    "dueDate": "2024-12-31T23:59:59Z"
  }'
```

### Important Notes

- **JAR Building**: The application JAR is built inside the Docker container, not copied from local build
- **Environment Variables**: Database connection is configured via environment variables
- **Profile**: Uses `postgres` profile for Docker deployment
- **Non-root User**: Application runs as non-root user for security
- **Health Checks**: Both application and database have health checks configured

### Database Console

When running with `db` profile, H2 console is available at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (empty)

### Running Tests

#### All Tests
```bash
./gradlew test
```

#### JPA Repository Tests Only
```bash
./gradlew test --tests "*JpaRepositoryTest"
```

#### Integration Tests Only
```bash
./gradlew test --tests "*IntegrationTest"
```

### Profiles

- **`inmemory`** (default): Uses existing in-memory implementations with `@Profile("inmemory")`
- **`db`**: Uses JPA adapters with H2 database with `@Profile("db")`

### API Endpoints

All existing API endpoints remain unchanged:

#### Tasks
- `GET /api/tasks?userId={userId}` - Get all tasks for user
- `GET /api/tasks/pending?userId={userId}` - Get pending tasks for user
- `POST /api/tasks` - Create new task
- `DELETE /api/tasks/{id}` - Mark task as deleted

#### Users
- `POST /api/users` - Register new user
- `GET /api/users/login?username={username}` - Login user

#### Notifications
- `GET /api/notifications?userId={userId}` - Get all notifications for user
- `GET /api/notifications/pending?userId={userId}` - Get pending notifications for user

### Dependencies Added

- `spring-boot-starter-data-jpa` - Spring Data JPA support
- `h2database` - H2 in-memory database

### Architecture

The application uses the Adapter pattern to maintain compatibility:

1. **Domain Models**: `Task`, `User`, `Notification` (unchanged)
2. **JPA Entities**: `TaskEntity`, `UserEntity`, `NotificationEntity` (new)
3. **Repository Interfaces**: Existing interfaces unchanged
4. **Repository Implementations**:
   - In-memory: `InMemory*Repository` with `@Profile("inmemory")`
   - Database: `*RepositoryAdapter` with `@Profile("db")`
5. **Services**: Unchanged, use repository interfaces

This design allows switching between storage types without changing service or controller code.
