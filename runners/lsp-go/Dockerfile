FROM golang:1.23-bookworm

ENV GOBIN=/usr/local/bin
RUN go install golang.org/x/tools/gopls@v0.18.1

WORKDIR /workspace
ENTRYPOINT ["gopls", "serve"]
