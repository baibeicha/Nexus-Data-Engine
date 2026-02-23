#!/bin/bash

# Nexus Data Engine - API Test Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8000"
AUTH_TOKEN=""
REFRESH_TOKEN=""
USER_ID=""
PROJECT_ID=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Nexus Data Engine - API Test Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to make API calls
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local auth=${4:-true}
    
    local headers="-H 'Content-Type: application/json'"
    if [ "$auth" = true ] && [ -n "$AUTH_TOKEN" ]; then
        headers="$headers -H 'Authorization: Bearer $AUTH_TOKEN'"
    fi
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$BASE_URL$endpoint" $headers -d "$data"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" $headers
    fi
}

echo -e "${YELLOW}1. Testing Auth Service - Register${NC}"
REGISTER_RESPONSE=$(api_call "POST" "/api/v1/auth/register" '{"username":"test@example.com","password":"password123"}' false)
echo "Response: $REGISTER_RESPONSE"
echo ""

echo -e "${YELLOW}2. Testing Auth Service - Login (Get Tokens)${NC}"
TOKENS_RESPONSE=$(api_call "POST" "/api/v1/auth/tokens" '{"username":"test@example.com","password":"password123"}' false)
echo "Response: $TOKENS_RESPONSE"

# Extract tokens
AUTH_TOKEN=$(echo "$TOKENS_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo "$TOKENS_RESPONSE" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$AUTH_TOKEN" ]; then
    echo -e "${RED}Failed to get access token${NC}"
    exit 1
fi

echo -e "${GREEN}Got access token: ${AUTH_TOKEN:0:20}...${NC}"
echo ""

echo -e "${YELLOW}3. Testing Auth Service - Validate Token${NC}"
VALIDATE_RESPONSE=$(api_call "POST" "/api/v1/auth/validate" "\"$AUTH_TOKEN\"" false)
echo "Response: $VALIDATE_RESPONSE"
echo ""

echo -e "${YELLOW}4. Testing Core Service - Create Project${NC}"
CREATE_PROJECT_RESPONSE=$(api_call "POST" "/api/v1/projects" '{
    "ownerId": "test@example.com",
    "name": "Test Project",
    "description": "This is a test project"
}')
echo "Response: $CREATE_PROJECT_RESPONSE"

# Extract project ID
PROJECT_ID=$(echo "$CREATE_PROJECT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}Created project with ID: $PROJECT_ID${NC}"
echo ""

echo -e "${YELLOW}5. Testing Core Service - Get Projects${NC}"
GET_PROJECTS_RESPONSE=$(api_call "GET" "/api/v1/projects" '"test@example.com"')
echo "Response: $GET_PROJECTS_RESPONSE"
echo ""

echo -e "${YELLOW}6. Testing Core Service - Get Project by ID${NC}"
GET_PROJECT_RESPONSE=$(api_call "GET" "/api/v1/projects/$PROJECT_ID" '"test@example.com"')
echo "Response: $GET_PROJECT_RESPONSE"
echo ""

if [ -n "$PROJECT_ID" ]; then
    echo -e "${YELLOW}7. Testing Core Service - Create Folder${NC}"
    CREATE_FOLDER_RESPONSE=$(api_call "POST" "/api/v1/files" "{
        \"projectId\": \"$PROJECT_ID\",
        \"parentId\": null,
        \"name\": \"Test Folder\",
        \"type\": \"FOLDER\",
        \"userId\": \"test@example.com\"
    }")
    echo "Response: $CREATE_FOLDER_RESPONSE"
    echo ""
    
    echo -e "${YELLOW}8. Testing Core Service - List Files${NC}"
    LIST_FILES_RESPONSE=$(api_call "GET" "/api/v1/files/project/$PROJECT_ID" "")
    echo "Response: $LIST_FILES_RESPONSE"
    echo ""
fi

echo -e "${YELLOW}9. Testing Core Service - Delete Project${NC}"
DELETE_RESPONSE=$(api_call "DELETE" "/api/v1/projects/$PROJECT_ID" '"test@example.com"')
echo "Response: $DELETE_RESPONSE"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}API Tests Completed!${NC}"
echo -e "${GREEN}========================================${NC}"
