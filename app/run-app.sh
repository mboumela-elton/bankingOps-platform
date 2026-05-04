#!/bin/bash

# Colors
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${YELLOW}Starting Banking Operations Application...${NC}\n"

# Check if PostgreSQL is running
echo "Checking PostgreSQL connection..."
if ! nc -z localhost 5432 2>/dev/null; then
    echo -e "${YELLOW}PostgreSQL not found on localhost:5432${NC}"
    echo "Make sure PostgreSQL is running with:"
    echo "  docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres"
    echo "  or start your local PostgreSQL service"
    echo ""
fi

# Initialize database
echo "Initializing database..."
./init-db.sh
if [ $? -ne 0 ]; then
    echo "Database initialization failed!"
    exit 1
fi

# Build the application
echo -e "${YELLOW}Building application...${NC}"
cd app
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}\n"

# Start the application
echo -e "${YELLOW}Starting Spring Boot application...${NC}"
echo "Application will run on http://localhost:8080"
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run -DskipTests
