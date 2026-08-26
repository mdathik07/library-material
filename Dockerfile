# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven project descriptor first.
# This allows Docker to cache dependency resolution.
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy application source
COPY src ./src

# Build the Spring Boot application.
# Tests are executed separately in CI.
RUN mvn clean package -DskipTests


# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for Docker health check
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root application user
RUN useradd -m -u 1000 appuser

# Copy packaged application
COPY --from=builder /build/target/*.jar /app/app.jar

# Give application user ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

# Container-level health check
HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=40s \
            --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run using UTC
ENV JAVA_OPTS="-Duser.timezone=UTC -Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]