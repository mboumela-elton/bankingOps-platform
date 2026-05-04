#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
REQUEST_ID=$(uuidgen)

echo -e "${YELLOW}=== Banking Operations API Test Suite ===${NC}\n"

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/health")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ PASS${NC} - Health check returned 200"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAIL${NC} - Health check returned $HTTP_CODE"
fi
echo ""

# Test 2: Ready Check
echo -e "${YELLOW}Test 2: Ready Check${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/ready")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ PASS${NC} - Ready check returned 200"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAIL${NC} - Ready check returned $HTTP_CODE"
fi
echo ""

# Test 3: Create User
echo -e "${YELLOW}Test 3: Create User${NC}"
USER_PAYLOAD='{
  "name": "John Doe",
  "email": "john.doe@example.com"
}'
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: $REQUEST_ID" \
  -d "$USER_PAYLOAD")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)
if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ PASS${NC} - User created with status 201"
    USER_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "User ID: $USER_ID"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAIL${NC} - User creation returned $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 4: Get User
if [ ! -z "$USER_ID" ]; then
    echo -e "${YELLOW}Test 4: Get User${NC}"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/users/$USER_ID" \
      -H "X-Request-ID: $REQUEST_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ PASS${NC} - User retrieved with status 200"
        echo "Response: $BODY"
    else
        echo -e "${RED}✗ FAIL${NC} - User retrieval returned $HTTP_CODE"
        echo "Response: $BODY"
    fi
    echo ""

    # Test 5: Create Transaction
    echo -e "${YELLOW}Test 5: Create Transaction${NC}"
    TRANSACTION_PAYLOAD="{
      \"userId\": \"$USER_ID\",
      \"amount\": 150.75,
      \"currency\": \"EUR\"
    }"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
      -H "Content-Type: application/json" \
      -H "X-Request-ID: $REQUEST_ID" \
      -d "$TRANSACTION_PAYLOAD")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    if [ "$HTTP_CODE" = "201" ]; then
        echo -e "${GREEN}✓ PASS${NC} - Transaction created with status 201"
        TRANSACTION_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
        echo "Transaction ID: $TRANSACTION_ID"
        echo "Response: $BODY"
    else
        echo -e "${RED}✗ FAIL${NC} - Transaction creation returned $HTTP_CODE"
        echo "Response: $BODY"
    fi
    echo ""

    # Test 6: Get Transaction
    if [ ! -z "$TRANSACTION_ID" ]; then
        echo -e "${YELLOW}Test 6: Get Transaction${NC}"
        RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions/$TRANSACTION_ID" \
          -H "X-Request-ID: $REQUEST_ID")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | head -n-1)
        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✓ PASS${NC} - Transaction retrieved with status 200"
            echo "Response: $BODY"
        else
            echo -e "${RED}✗ FAIL${NC} - Transaction retrieval returned $HTTP_CODE"
            echo "Response: $BODY"
        fi
        echo ""

        # Test 7: Validate Transaction
        echo -e "${YELLOW}Test 7: Validate Transaction${NC}"
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions/$TRANSACTION_ID/validate" \
          -H "X-Request-ID: $REQUEST_ID")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | head -n-1)
        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✓ PASS${NC} - Transaction validated with status 200"
            echo "Response: $BODY"
        else
            echo -e "${RED}✗ FAIL${NC} - Transaction validation returned $HTTP_CODE"
            echo "Response: $BODY"
        fi
        echo ""

        # Test 8: Get User Transactions
        echo -e "${YELLOW}Test 8: Get User Transactions${NC}"
        RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions/user/$USER_ID" \
          -H "X-Request-ID: $REQUEST_ID")
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        BODY=$(echo "$RESPONSE" | head -n-1)
        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✓ PASS${NC} - User transactions retrieved with status 200"
            echo "Response: $BODY"
        else
            echo -e "${RED}✗ FAIL${NC} - User transactions retrieval returned $HTTP_CODE"
            echo "Response: $BODY"
        fi
        echo ""
    fi
fi

# Test 9: Get Non-existent User
echo -e "${YELLOW}Test 9: Get Non-existent User (Error Handling)${NC}"
FAKE_ID="00000000-0000-0000-0000-000000000000"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/users/$FAKE_ID" \
  -H "X-Request-ID: $REQUEST_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)
if [ "$HTTP_CODE" = "404" ]; then
    echo -e "${GREEN}✓ PASS${NC} - Non-existent user returned 404"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAIL${NC} - Non-existent user returned $HTTP_CODE (expected 404)"
    echo "Response: $BODY"
fi
echo ""

echo -e "${YELLOW}=== Test Suite Complete ===${NC}"
