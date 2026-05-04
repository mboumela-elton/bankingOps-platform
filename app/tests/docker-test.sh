#!/bin/bash

################################################################################
# Docker Integration Tests
# Tests the application running in Docker containers
################################################################################

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
PASSED=0
FAILED=0

echo -e "${YELLOW}=== Docker Integration Tests ===${NC}\n"

# Function to test endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local endpoint=$3
    local expected_code=$4
    
    echo -n "Testing $name... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC} (Expected $expected_code, got $http_code)"
        ((FAILED++))
    fi
}

# Wait for application to be ready
echo -e "${YELLOW}Waiting for application to be ready...${NC}"
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Application is ready${NC}\n"
        break
    fi
    attempt=$((attempt + 1))
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}✗ Application failed to start${NC}"
    exit 1
fi

# Test endpoints
echo -e "${YELLOW}Testing API Endpoints:${NC}"
test_endpoint "Health Check" "GET" "/actuator/health" "200"
test_endpoint "Info" "GET" "/actuator/info" "200"
test_endpoint "Metrics" "GET" "/actuator/metrics" "200"
test_endpoint "Env" "GET" "/actuator/env" "200"
test_endpoint "Beans" "GET" "/actuator/beans" "200"

echo ""
echo -e "${YELLOW}Testing Application Endpoints:${NC}"
test_endpoint "Create User" "POST" "/users" "201"
test_endpoint "Non-existent User" "GET" "/users/00000000-0000-0000-0000-000000000000" "404"

echo ""
echo -e "${YELLOW}=== Test Results ===${NC}"
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}✗ Some tests failed${NC}"
    exit 1
fi
