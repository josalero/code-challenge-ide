FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends clangd-14 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
ENTRYPOINT ["clangd-14", "--background-index=0", "--compile-commands-dir=/workspace"]
