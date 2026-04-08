# Dockerfile for Tribal Handicrafts Backend

FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:download-sources -DdownloadSources=true

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-slim

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/handicrafts-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/handicrafts_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -jar app.jar /health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
