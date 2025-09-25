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
