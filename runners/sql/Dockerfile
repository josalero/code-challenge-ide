FROM python:3.12-slim-bookworm

RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    postgresql postgresql-client libpq-dev util-linux \
  && pip install --no-cache-dir pytest==8.3.4 psycopg2-binary==2.9.10 \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p /var/lib/postgresql/data /var/run/postgresql \
  && chown -R postgres:postgres /var/lib/postgresql /var/run/postgresql

WORKDIR /opt/runner
COPY run.py ctl_sql.py conftest.py entrypoint.sh /opt/runner/
RUN chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]
