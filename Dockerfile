# Dockerfile
# Multi-stage build for smaller final image
FROM openjdk:21-jdk-slim as builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/photo-upload-app-1.0.0.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application with optimized JVM settings
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]