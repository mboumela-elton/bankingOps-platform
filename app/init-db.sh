#!/bin/bash

# Colors
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Initializing PostgreSQL Database...${NC}\n"

# Check if PostgreSQL is running
if ! nc -z localhost 5432 2>/dev/null; then
    echo -e "${RED}✗ PostgreSQL is not running on localhost:5432${NC}"
    echo "Start PostgreSQL with:"
    echo "  docker-compose up -d postgres"
    exit 1
fi

echo -e "${GREEN}✓ PostgreSQL is running${NC}\n"

# Create database
echo "Creating database 'bankingops'..."
psql -U postgres -h localhost -c "CREATE DATABASE bankingops;" 2>&1 | grep -v "already exists"

if [ $? -eq 0 ] || [ $? -eq 1 ]; then
    echo -e "${GREEN}✓ Database ready${NC}\n"
else
    echo -e "${RED}✗ Failed to create database${NC}"
    exit 1
fi

# Verify connection
echo "Verifying connection..."
psql -U postgres -h localhost -d bankingops -c "SELECT 1;" > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Connection successful${NC}\n"
    echo -e "${GREEN}Database initialization complete!${NC}"
    exit 0
else
    echo -e "${RED}✗ Connection failed${NC}"
    exit 1
fi
