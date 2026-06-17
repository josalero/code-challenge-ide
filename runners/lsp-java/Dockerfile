# JDK 26 is bundled in the final image at /opt/java-26 so JDT (running on JDK 21)
# can analyze Java 26 source code via the LSP `java.configuration.runtimes`
# setting. Multi-stage pull keeps the final image lean — only the JDK 26 home
# directory is copied; we never run JDT on the JDK 26 binary because JDT hangs
# during `initialize` on non-LTS JDKs (24/25/26) Eclipse has not validated.
FROM eclipse-temurin:26-jdk-jammy AS jdk26

FROM eclipse-temurin:21-jre-jammy

# Validated host JVM for JDT Language Server. JDT itself runs on JDK 21 LTS;
# user code is analyzed against JavaSE-26 via the runtime path below, so Java 26
# language features (e.g. instance main methods, structured concurrency) still
# resolve in IntelliSense.
COPY --from=jdk26 /opt/java/openjdk /opt/java-26
ENV JAVA_26_HOME=/opt/java-26

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates tar \
    && rm -rf /var/lib/apt/lists/*

ENV JDTLS_HOME=/opt/jdtls
RUN mkdir -p "${JDTLS_HOME}" \
    && curl -fsSL "https://download.eclipse.org/jdtls/snapshots/jdt-language-server-latest.tar.gz" \
    | tar -xz -C "${JDTLS_HOME}"

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /workspace
ENTRYPOINT ["/entrypoint.sh"]
