.PHONY: up down infra runners logs ps reset config cursor-vendors

up:
	@./scripts/compose-up.sh

infra:
	docker compose -f docker-compose.local.yml up -d

runners:
	@./scripts/compose-runners.sh

cursor-vendors:
	@./scripts/install-cursor-vendors.sh

down:
	docker compose down

down-infra:
	docker compose -f docker-compose.local.yml down

logs:
	docker compose logs -f

ps:
	docker compose ps

reset:
	docker compose down -v

config:
	docker compose config
