# Multi-stage build for Spring Boot application
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test

# Second stage: Runtime
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/taskmanager-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set entrypoint to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]