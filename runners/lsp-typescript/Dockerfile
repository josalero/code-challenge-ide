FROM node:22-bookworm-slim

RUN npm install -g \
    typescript@5.7.3 \
    typescript-language-server@4.3.4 \
    @vue/language-server@2.2.0

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /workspace
ENTRYPOINT ["/entrypoint.sh"]
