# Docker Setup Guide

This guide explains how to run the Banking Operations Platform using Docker.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Create .env file

```bash
cp .env.example .env
```

Edit `.env` and customize for your environment:
```bash
DB_USER=postgres
DB_PASSWORD=your_secure_password
DB_NAME=bankingops
SERVER_PORT=8080
```

### 2. Start Services

```bash
./docker-start.sh up
```

Or manually:
```bash
docker compose up -d --build
```

### 3. Verify Services

```bash
# Check running containers
docker compose ps

# View logs
docker compose logs -f

# Test health endpoint
curl http://localhost:8080/actuator/health
```

## Services

### PostgreSQL Database
- **Container**: bankingops-postgres
- **Image**: postgres:14-alpine
- **Port**: 5432 (default)
- **Volume**: postgres_data (persistent storage)
- **Network**: bankingops-network

### Spring Boot API
- **Container**: bankingops-api
- **Image**: Built from Dockerfile
- **Port**: 8080 (default)
- **Volume**: ./logs (application logs)
- **Network**: bankingops-network

## Docker Compose Commands

### Start Services
```bash
./docker-start.sh up
# or
docker compose up -d --build
```

### Stop Services
```bash
./docker-start.sh down
# or
docker compose down
```

### View Logs
```bash
./docker-start.sh logs
# or
docker compose logs -f
```

### Rebuild Images
```bash
./docker-start.sh build
# or
docker compose build --no-cache
```

### View Running Containers
```bash
./docker-start.sh ps
# or
docker compose ps
```

### Restart Services
```bash
./docker-start.sh restart
# or
docker compose restart
```

### Clean Up (Remove volumes)
```bash
./docker-start.sh clean
# or
docker compose down -v
```

## Dockerfile Details

### Multi-Stage Build

**Stage 1: Builder**
- Uses `maven:3.9-eclipse-temurin-17`
- Downloads dependencies
- Compiles application
- Creates JAR file

**Stage 2: Runtime**
- Uses `eclipse-temurin:17-jre-alpine` (smaller image)
- Copies JAR from builder
- Creates non-root user for security
- Includes health check
- Exposes port 8080

### Image Size
- Builder stage: ~600MB (not included in final image)
- Final image: ~200MB

## Environment Variables

### Database
- `DB_HOST` - Database hostname (default: postgres)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: bankingops)
- `DB_USER` - Database user (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)

### Application
- `SERVER_PORT` - Application port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: prod)
- `LOG_LEVEL` - Logging level (default: INFO)

### Security
- `SECURITY_USER_NAME` - Basic auth username
- `SECURITY_USER_PASSWORD` - Basic auth password

### JVM
- `JAVA_OPTS` - JVM options (default: -Xmx512m -Xms256m)

## Volumes

### postgres_data
- **Type**: Named volume
- **Purpose**: Persistent PostgreSQL data
- **Location**: Docker managed storage
- **Persistence**: Data survives container restart

### ./logs
- **Type**: Bind mount
- **Purpose**: Application logs
- **Location**: `app/logs` on host
- **Persistence**: Logs accessible from host

## Networks

### bankingops-network
- **Type**: Bridge network
- **Purpose**: Internal communication between services
- **Services**: postgres, api
- **Isolation**: Services can communicate by service name

## Health Checks

### PostgreSQL
```bash
pg_isready -U postgres
```
- Interval: 10s
- Timeout: 5s
- Retries: 5

### Spring Boot API
```bash
curl -f http://localhost:8080/actuator/health
```
- Interval: 30s
- Timeout: 10s
- Retries: 3
- Start period: 40s

## Testing

### Run Integration Tests
```bash
./tests/docker-test.sh
```

Tests:
- Health endpoint
- Info endpoint
- Metrics endpoint
- Environment endpoint
- Beans endpoint
- User creation
- Error handling

## Troubleshooting

### Application won't start
```bash
# Check logs
docker compose logs api

# Check health
curl http://localhost:8080/actuator/health

# Restart
docker compose restart api
```

### Database connection error
```bash
# Check PostgreSQL logs
docker compose logs postgres

# Check database is ready
docker compose ps

# Restart database
docker compose restart postgres
```

### Port already in use
```bash
# Change port in .env
SERVER_PORT=8081

# Restart services
docker compose restart
```

### Clean rebuild
```bash
./docker-start.sh clean
./docker-start.sh up
```

## Production Considerations

### Security
- [ ] Change default database password
- [ ] Change default application credentials
- [ ] Use strong passwords
- [ ] Enable HTTPS/TLS
- [ ] Use secrets management (Docker Secrets, Vault)

### Performance
- [ ] Adjust JVM heap size in JAVA_OPTS
- [ ] Configure database connection pool
- [ ] Enable caching
- [ ] Use CDN for static content

### Monitoring
- [ ] Set up log aggregation (ELK, Splunk)
- [ ] Configure metrics collection (Prometheus)
- [ ] Set up alerting
- [ ] Monitor resource usage

### Backup
- [ ] Backup PostgreSQL data regularly
- [ ] Test restore procedures
- [ ] Use managed database services

## Docker Compose Override

For local development, create `docker-compose.override.yml`:

```yaml
version: '3.9'

services:
  api:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      LOG_LEVEL: DEBUG
    ports:
      - "8080:8080"
    volumes:
      - ./src:/app/src
```

This file is automatically loaded and overrides settings in `docker-compose.yml`.

## References

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)

