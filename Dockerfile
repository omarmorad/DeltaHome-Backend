# ---------- Build stage ----------
# maven:3.9-eclipse-temurin-21 is built on the Eclipse Temurin JDK 21 image,
# so no local Maven/Gradle install is required to build this image.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Resolve dependencies first so they are cached as a separate layer.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the application (tests skipped: they need Postgres/Testcontainers).
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user (Dockerfile best practice).
RUN useradd --create-home --shell /usr/sbin/nologin appuser \
    && chown -R appuser:appuser /app
USER appuser

COPY --from=build /workspace/target/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
