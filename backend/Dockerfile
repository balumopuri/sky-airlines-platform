FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN apk add --no-cache wget && \
    wget -O /app/dd-java-agent.jar https://dtdg.co/latest-java-tracer

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

ENV DD_SERVICE="sky-airlines-api" \
    DD_ENV="local" \
    DD_VERSION="0.0.1" \
    DD_TRACE_ENABLED="false" \
    DD_LOGS_INJECTION="true" \
    DD_PROFILING_ENABLED="false"

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
