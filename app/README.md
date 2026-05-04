# Banking Operations Platform

A Spring Boot 4.0 application for managing banking transactions with comprehensive monitoring and structured logging.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 14+

### Setup

1. **Initialize database**
```bash
./init-db.sh
```

2. **Start application**
```bash
./run-app.sh
```

The application will start on `http://localhost:8080`

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

## Monitoring (Actuator)

Access monitoring endpoints at `/actuator`:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Available metrics list
- `/actuator/metrics/{metric}` - Specific metric value
- `/actuator/env` - Environment variables
- `/actuator/beans` - Spring beans
- `/actuator/loggers` - Logger configuration

### Example Metrics
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/process.cpu.usage
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Testing

### Test API endpoints
```bash
./test-api.sh
```

### Test Actuator endpoints
```bash
./test-actuator.sh
```

## Logging

Logs are written to `logs/bankingops.log` in JSON format with:
- Timestamp
- Log level
- Service name
- Request ID
- Latency (ms)
- Context information

View logs:
```bash
tail -f logs/bankingops.log
```

## Configuration

Main configuration file: `app/src/main/resources/application.properties`

Key settings:
- `server.port=8080` - Server port
- `spring.datasource.url` - Database URL
- `spring.datasource.username` - DB username
- `spring.datasource.password` - DB password

Environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=bankingops
export DB_USER=postgres
export DB_PASSWORD=postgres
```

## Build & Compile

```bash
# Compile
mvn clean compile -DskipTests

# Run tests
mvn test

# Build JAR
mvn clean package

# Run with Maven
mvn spring-boot:run -DskipTests
```

## Project Structure

```
app/
├── src/main/java/com/msel/app/
│   ├── config/          - Configuration classes
│   ├── controller/      - REST endpoints
│   ├── service/         - Business logic
│   ├── repository/      - Database access
│   ├── entity/          - JPA entities
│   ├── dto/             - Data transfer objects
│   ├── exception/       - Exception handling
│   ├── interceptor/     - Request/response interceptor
│   └── util/            - Utilities
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── application-prod.properties
│   └── logback-spring.xml
└── pom.xml
```

## Technology Stack

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 17
- **Database**: PostgreSQL 14
- **Build Tool**: Maven
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback with JSON formatting
- **Security**: Spring Security
- **API Documentation**: SpringDoc OpenAPI

## Troubleshooting

### Application won't start
```bash
# Check PostgreSQL is running
psql -U postgres -l

# Reinitialize database
./init-db.sh

# Check logs
tail -f logs/bankingops.log
```

### Port already in use
```bash
# Kill process on port 8080
lsof -i :8080
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

### Database connection error
```bash
# Check database exists
psql -U postgres -l

# Reinitialize
./init-db.sh
```

## License

This project is provided as-is for educational and development purposes.

