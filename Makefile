.PHONY: up down infra down-infra reset-infra runners lsp-warm lsp-warm-force logs ps reset config cursor-vendors

up:
	@./scripts/compose-up.sh

infra:
	docker compose -f docker-compose.local.yml up -d --remove-orphans

runners:
	@./scripts/compose-runners.sh

lsp-warm:
	@./scripts/warm-lsp-images.sh

lsp-warm-force:
	@CTL_FORCE_LSP_WARM=1 ./scripts/warm-lsp-images.sh --force

cursor-vendors:
	@./scripts/install-cursor-vendors.sh

down:
	docker compose down

down-infra:
	docker compose -f docker-compose.local.yml down -v --remove-orphans

reset-infra: down-infra

logs:
	docker compose logs -f

ps:
	docker compose ps

reset:
	docker compose down -v

config:
	docker compose config
