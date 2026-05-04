#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="http://localhost:8080/actuator"

echo -e "${YELLOW}=== Actuator Endpoints Test ===${NC}\n"

# Test 1: Actuator Index
echo -e "${YELLOW}1. Actuator Index${NC}"
curl -s $BASE_URL | jq .
echo ""

# Test 2: Health
echo -e "${YELLOW}2. Health Endpoint${NC}"
curl -s $BASE_URL/health | jq .
echo ""

# Test 3: Info
echo -e "${YELLOW}3. Info Endpoint${NC}"
curl -s $BASE_URL/info | jq .
echo ""

# Test 4: Metrics
echo -e "${YELLOW}4. Metrics Endpoint${NC}"
curl -s $BASE_URL/metrics | jq .
echo ""

# Test 5: Environment
echo -e "${YELLOW}5. Environment Endpoint${NC}"
curl -s $BASE_URL/env | jq .
echo ""

# Test 6: Beans
echo -e "${YELLOW}6. Beans Endpoint${NC}"
curl -s $BASE_URL/beans | jq .
echo ""

# Test 7: HTTP Trace
echo -e "${YELLOW}7. HTTP Trace Endpoint${NC}"
curl -s $BASE_URL/httptrace | jq .
echo ""

echo -e "${GREEN}=== All Actuator Endpoints Tested ===${NC}"
