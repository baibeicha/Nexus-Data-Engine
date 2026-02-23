.PHONY: help build start stop restart logs test clean

# Default target
help:
	@echo "Nexus Data Engine - Available Commands:"
	@echo ""
	@echo "  make build    - Build all Docker images"
	@echo "  make start    - Start all services"
	@echo "  make stop     - Stop all services"
	@echo "  make restart  - Restart all services"
	@echo "  make logs     - View logs from all services"
	@echo "  make test     - Run API tests"
	@echo "  make clean    - Stop and remove all containers and volumes"
	@echo ""

# Build all services
build:
	@echo "Building all services..."
	docker-compose build

# Start all services
start:
	@echo "Starting Nexus Data Engine..."
	@./start.sh

# Stop all services
stop:
	@echo "Stopping Nexus Data Engine..."
	@./stop.sh

# Restart all services
restart: stop start

# View logs
logs:
	docker-compose logs -f

# Run API tests
test:
	@echo "Running API tests..."
	@./test-api.sh

# Clean up everything
clean:
	@echo "Cleaning up..."
	docker-compose down -v
	docker system prune -f

# Development mode - start only infrastructure
dev-infra:
	@echo "Starting infrastructure services..."
	docker-compose up -d postgres postgres-core zookeeper kafka
	@echo "Waiting for infrastructure to be ready..."
	@sleep 30
	@echo "Infrastructure is ready!"

# Development mode - start application services
dev-app:
	@echo "Starting application services..."
	docker-compose up -d discovery-service auth-service core-service data-processor sync-service gateway

# Run tests for Core Service
test-core:
	@echo "Running Core Service tests..."
	cd "Nexus Core Service" && ./gradlew test

# Run tests for Auth Service
test-auth:
	@echo "Running Auth Service tests..."
	cd "Nexus Auth Service" && ./gradlew test

# Run tests for Data Processor Service
test-processor:
	@echo "Running Data Processor Service tests..."
	cd "Nexus Data Processor Service" && ./gradlew test

# Run all tests
test-all: test-core test-auth test-processor
