# Banking Operations Platform

A Spring Boot 4.0 application for managing banking transactions with comprehensive monitoring and structured logging.

## Project Structure

```
bankingops-platform/
├── README.md                     # Root documentation
├── app/                          # Main application
│   ├── README.md                 # Application documentation
│   ├── DOCKER.md                 # Docker setup guide
│   ├── pom.xml                   # Maven dependencies
│   ├── Dockerfile                # Multi-stage Docker build
│   ├── docker-compose.yml        # Docker Compose configuration
│   ├── .env.example              # Environment variables template
│   ├── .dockerignore             # Docker build ignore
│   ├── docker-start.sh           # Docker startup script
│   ├── init-db.sh                # Database initialization
│   ├── run-app.sh                # Start application
│   ├── tests/                    # Test scripts directory
│   │   ├── test-api.sh           # API endpoint tests
│   │   ├── test-actuator.sh      # Actuator endpoint tests
│   │   ├── docker-test.sh        # Docker integration tests
│   │   └── README.md             # Test documentation
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/msel/app/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── interceptor/
│   │   │   │   └── util/
│   │   │   └── resources/
│   │   └── test/
│   └── logs/
```

## Quick Start

### Local Development

```bash
cd app

# Initialize database
./init-db.sh

# Start application
./run-app.sh

# Test endpoints
./tests/test-api.sh
./tests/test-actuator.sh
```

### Docker Deployment

```bash
cd app

# Create environment file
cp .env.example .env

# Start services
./docker-start.sh up

# Run integration tests
./tests/docker-test.sh

# Stop services
./docker-start.sh down
```

## Documentation

- **[app/README.md](app/README.md)** - Complete application documentation
- **[app/DOCKER.md](app/DOCKER.md)** - Docker setup and deployment guide
- **[app/tests/README.md](app/tests/README.md)** - Test scripts documentation

## Key Features

- ✅ Spring Boot 4.0 with Java 17
- ✅ PostgreSQL database integration
- ✅ Spring Boot Actuator monitoring
- ✅ Structured JSON logging
- ✅ Spring Security configuration
- ✅ Comprehensive error handling
- ✅ Request ID tracking
- ✅ Latency measurement
- ✅ Docker multi-stage build
- ✅ Docker Compose orchestration
- ✅ Health checks and monitoring

## Technology Stack

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 17
- **Database**: PostgreSQL 14
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback with JSON formatting
- **Security**: Spring Security

## API Endpoints

### Users
- `POST /users` - Create user
- `GET /users/{id}` - Get user
- `GET /users/{id}/transactions` - Get user transactions

### Transactions
- `POST /transactions` - Create transaction
- `GET /transactions/{id}` - Get transaction
- `POST /transactions/{id}/validate` - Validate transaction
- `POST /transactions/{id}/fail` - Fail transaction

## Monitoring

Access monitoring endpoints at `/actuator`:

- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Available metrics
- `/actuator/env` - Environment variables
- `/actuator/beans` - Spring beans
- `/actuator/loggers` - Logger configuration

## Build & Compile

```bash
cd app

# Compile
mvn clean compile -DskipTests

# Run tests
mvn test

# Build JAR
mvn clean package

# Run with Maven
mvn spring-boot:run -DskipTests
```

## Docker Commands

```bash
cd app

# Build and start
docker compose up -d --build

# View logs
docker compose logs -f

# Stop services
docker compose down

# Clean up (remove volumes)
docker compose down -v
```

## Troubleshooting

### Application won't start
```bash
# Check PostgreSQL is running
psql -U postgres -l

# Reinitialize database
./init-db.sh

# Check logs
tail -f app/logs/bankingops.log
```

### Docker issues
```bash
# Check running containers
docker compose ps

# View logs
docker compose logs

# Restart services
docker compose restart

# Clean rebuild
docker compose down -v
docker compose up -d --build
```

For more information, see [app/README.md](app/README.md) and [app/DOCKER.md](app/DOCKER.md)

