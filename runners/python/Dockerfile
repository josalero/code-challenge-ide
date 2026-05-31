FROM python:3.12-slim-bookworm

RUN pip install --no-cache-dir pytest==8.3.4 pytest-cov==6.0.0 ruff==0.9.6

WORKDIR /opt/runner
COPY run.py ruff.toml entrypoint.sh /opt/runner/
RUN chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]
