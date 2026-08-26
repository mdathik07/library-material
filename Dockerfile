# Multi-stage build for Reading Material Library

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN mkdir -p /logs

COPY --from=builder /build/target/*.jar app.jar

RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app /logs

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=40s \
            --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Duser.timezone=UTC -Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar