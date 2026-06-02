FROM python:3.12-slim

# Bundle Node so pyright-langserver works with --network none (API spawns LSP this way).
RUN pip install --no-cache-dir "pyright[nodejs]==1.1.398"

WORKDIR /workspace
ENTRYPOINT ["pyright-langserver", "--stdio"]
