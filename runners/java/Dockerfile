ARG JAVA_MAJOR=26
FROM eclipse-temurin:${JAVA_MAJOR}-jdk-jammy

LABEL org.opencontainers.image.title="code-challenge-ide-runner-java-${JAVA_MAJOR}"
LABEL org.opencontainers.image.description="Java ${JAVA_MAJOR} Maven/JUnit/JaCoCo/Checkstyle runner for Code Training Lab"

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
        coreutils \
        findutils \
        maven \
        python3 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home runner

COPY pom-template.xml checkstyle.xml run.py /opt/runner/
COPY entrypoint.sh /entrypoint.sh

# Warm Maven repository so sandbox runs work with --network none.
RUN mkdir -p /opt/runner/warm/src/main/java/com/challenge \
    && mkdir -p /opt/runner/warm/src/test/java/com/challenge/public_ \
    && sed "s/__JAVA_MAJOR__/${JAVA_MAJOR}/g" /opt/runner/pom-template.xml > /opt/runner/warm/pom.xml \
    && cp /opt/runner/checkstyle.xml /opt/runner/warm/checkstyle.xml \
    && printf '%s\n' \
        'package com.challenge;' \
        'public class Solution { public static String reverse(String input) { return input; } }' \
        > /opt/runner/warm/src/main/java/com/challenge/Solution.java \
    && printf '%s\n' \
        'package com.challenge.public_;' \
        'import com.challenge.Solution;' \
        'import org.junit.jupiter.api.Test;' \
        'import static org.junit.jupiter.api.Assertions.assertEquals;' \
        'class WarmTest { @Test void warm() { assertEquals("a", Solution.reverse("a")); } }' \
        > /opt/runner/warm/src/test/java/com/challenge/public_/WarmTest.java \
    && mvn -q -Dmaven.repo.local=/opt/m2/repository -f /opt/runner/warm/pom.xml \
        test jacoco:report checkstyle:checkstyle

RUN chmod +x /entrypoint.sh /opt/runner/run.py \
    && mkdir -p /tmp/workspace \
    && chown -R runner:runner /tmp/workspace /opt/runner /opt/m2

ENV JAVA_MAJOR=${JAVA_MAJOR}
USER runner
ENTRYPOINT ["/entrypoint.sh"]
