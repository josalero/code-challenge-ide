FROM rust:1.84-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 clang \
    && rm -rf /var/lib/apt/lists/* \
    && rustup component add llvm-tools-preview clippy rustfmt \
    && cargo install cargo-llvm-cov --locked --version 0.6.21

WORKDIR /opt/runner
COPY Cargo.toml run.py entrypoint.sh /opt/runner/
RUN chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]
