# Multi-stage Dockerfile for Core API (Java 21)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml ./
# Download dependencies layer for Docker cache efficiency
RUN apk add --no-cache maven && mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime Image
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=30s \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
