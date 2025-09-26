# Simple Dockerfile for Spring Boot application
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the pre-built jar from local build
COPY build/libs/taskmanager-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set entrypoint to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
