#!/bin/bash

# Nexus Data Engine - Stop Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping Nexus Data Engine...${NC}"

# Stop all services
docker-compose down

echo -e "${GREEN}Nexus Data Engine stopped successfully!${NC}"
