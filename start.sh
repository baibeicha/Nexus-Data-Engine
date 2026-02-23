#!/bin/bash

# Nexus Data Engine - Startup Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting Nexus Data Engine...${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running${NC}"
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Error: docker-compose is not installed${NC}"
    exit 1
fi

# Generate JWT secret if not set
if [ -z "$JWT_SECRET" ]; then
    echo -e "${YELLOW}JWT_SECRET not set. Generating a random secret...${NC}"
    export JWT_SECRET=$(openssl rand -base64 32)
    echo -e "${GREEN}Generated JWT_SECRET: $JWT_SECRET${NC}"
    echo -e "${YELLOW}Please save this secret for future use!${NC}"
fi

# Build all services
echo -e "${GREEN}Building all services...${NC}"
docker-compose build

# Start infrastructure services first
echo -e "${GREEN}Starting infrastructure services...${NC}"
docker-compose up -d postgres postgres-core zookeeper kafka

# Wait for infrastructure to be ready
echo -e "${YELLOW}Waiting for infrastructure to be ready...${NC}"
sleep 30

# Start application services
echo -e "${GREEN}Starting application services...${NC}"
docker-compose up -d discovery-service auth-service core-service data-processor sync-service gateway

# Wait for all services to be ready
echo -e "${YELLOW}Waiting for all services to be ready...${NC}"
sleep 30

# Check service health
echo -e "${GREEN}Checking service health...${NC}"

check_service() {
    local service=$1
    local url=$2
    local max_attempts=10
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -sf "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service is healthy${NC}"
            return 0
        fi
        echo -e "${YELLOW}Waiting for $service... (attempt $attempt/$max_attempts)${NC}"
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗ $service is not responding${NC}"
    return 1
}

check_service "Discovery Service" "http://localhost:8761/actuator/health"
check_service "Auth Service" "http://localhost:9000/actuator/health"
check_service "Core Service" "http://localhost:8081/actuator/health"
check_service "Data Processor" "http://localhost:8082/actuator/health"
check_service "Sync Service" "http://localhost:8085/actuator/health"
check_service "Gateway" "http://localhost:8000/actuator/health"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Nexus Data Engine is ready!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${GREEN}Service URLs:${NC}"
echo -e "  API Gateway:    http://localhost:8000"
echo -e "  Eureka:         http://localhost:8761"
echo -e "  Auth Service:   http://localhost:9000"
echo -e "  Core Service:   http://localhost:8081"
echo -e "  Sync Service:   ws://localhost:8085/ws/sync"
echo ""
echo -e "${YELLOW}To view logs:${NC}"
echo -e "  docker-compose logs -f"
echo ""
echo -e "${YELLOW}To stop all services:${NC}"
echo -e "  docker-compose down"
echo ""
