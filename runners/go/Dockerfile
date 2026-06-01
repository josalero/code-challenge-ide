FROM golang:1.23-bookworm

RUN apt-get update && apt-get install -y --no-install-recommends python3 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/runner
COPY run.py entrypoint.sh /opt/runner/
RUN chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]
