.PHONY: all help up down dev restart logs ps test lint seed build clean

# Default target
all: help

help:
	@echo "Smart IoT Park Platform - Command Reference"
	@echo "============================================"
	@echo "make up        - Launch full production-like local stack in background"
	@echo "make down      - Tear down all running containers and networks"
	@echo "make dev       - Run stack with hot-reload overrides (docker-compose.dev.yml)"
	@echo "make restart   - Restart all containers"
	@echo "make logs      - Stream unified logs from all services"
	@echo "make ps        - List status of all microservices"
	@echo "make build     - Build all Docker images without starting"
	@echo "make test      - Run automated tests across all microservices (Core, Ingestion, AI, Dashboard)"
	@echo "make lint      - Run linters and code format checkers across services"
	@echo "make seed      - Execute demo data seeding scripts for MySQL and MongoDB"
	@echo "make clean     - Remove stopped containers, test artifacts, and build caches"

up:
	docker compose up -d --build

down:
	docker compose down

dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

restart:
	docker compose restart

logs:
	docker compose logs -f

ps:
	docker compose ps

build:
	docker compose build

test:
	@echo "Running tests across all microservices..."
	@echo "1/4 Testing Core API..."
	cd services/core-api && ./mvnw test || cd services/core-api && mvn test
	@echo "2/4 Testing Ingestion Service..."
	cd services/ingestion-service && ./mvnw test || cd services/ingestion-service && mvn test
	@echo "3/4 Testing AI Engine..."
	cd services/ai-engine && pytest
	@echo "4/4 Testing Frontend Dashboard..."
	cd frontend/dashboard && npm test -- --run

lint:
	@echo "Linting services..."
	cd services/ai-engine && ruff check . || true
	cd frontend/dashboard && npm run lint || true

seed:
	@echo "Executing database seeding scripts..."
	bash scripts/seed-data.sh || powershell -File scripts/seed-data.ps1

clean:
	docker compose down -v --remove-orphans
	@echo "Clean complete."
