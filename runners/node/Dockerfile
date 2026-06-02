FROM node:22-bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/* \
    && npm install -g c8@10.1.2 eslint@9.19.0 \
    && mkdir -p /opt/runner

WORKDIR /opt/runner
COPY run.py entrypoint.sh .eslintrc.json /opt/runner/
RUN chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]
