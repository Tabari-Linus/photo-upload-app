# Multi-stage build for optimized container size
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven POM file first (for better layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Update packages to fix vulnerabilities like CVE-2025-59375 in libexpat
RUN apk update && apk upgrade

# Install required packages for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1000 spring && \
    adduser -D -s /bin/sh -u 1000 -G spring spring

# Set working directory
WORKDIR /app

# Copy JAR file from builder stage
COPY --from=builder /app/target/photo-upload-app.jar app.jar

# Create directory for temporary files
RUN mkdir -p /tmp/uploads && \
    chown -R spring:spring /app /tmp/uploads

# Switch to non-root user
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Health check for container
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Set Spring profile for production
ENV SPRING_PROFILES_ACTIVE=default

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]