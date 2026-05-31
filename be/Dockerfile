# Build from repository root: docker build -f be/Dockerfile .
FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle/wrapper/ gradle/wrapper/
COPY config/quality-gates.properties config/quality-gates.properties
COPY be/ be/
# settings.gradle includes :fe — directory must exist even when only building :be
COPY fe/build.gradle fe/package.json fe/package-lock.json fe/

RUN chmod +x gradlew \
    && ./gradlew :be:bootJar -x test --no-daemon

FROM eclipse-temurin:26-jre-jammy

LABEL org.opencontainers.image.title="code-challenge-ide-be"
LABEL org.opencontainers.image.description="Code Training Lab API"

ARG PORT=8080
ENV PORT=${PORT}
ENV SPRING_PROFILES_ACTIVE=production

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl docker.io \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home app

WORKDIR /app
COPY --from=build /workspace/be/build/libs/be-*.jar /app/app.jar
COPY challenges /challenges
RUN chown -R app:app /app /challenges

# Supplementary group for /var/run/docker.sock is set via compose group_add (DOCKER_GID).
USER app
EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl --fail "http://localhost:${PORT}/actuator/health/readiness" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
