# Test Scripts

This directory contains test scripts for the Banking Operations Platform.

## Prerequisites

- Application must be running on `http://localhost:8080`
- `curl` command-line tool
- `jq` (optional, for JSON formatting in test-actuator.sh)

## Test Scripts

### test-api.sh
Tests all API endpoints with comprehensive test cases.

**Usage:**
```bash
./test-api.sh
```

**Tests:**
1. Health Check - `/health`
2. Ready Check - `/ready`
3. Create User - `POST /users`
4. Get User - `GET /users/{id}`
5. Create Transaction - `POST /transactions`
6. Get Transaction - `GET /transactions/{id}`
7. Validate Transaction - `POST /transactions/{id}/validate`
8. Get User Transactions - `GET /transactions/user/{userId}`
9. Error Handling - Non-existent user (404)

**Output:**
- Green ✓ for passed tests
- Red ✗ for failed tests
- HTTP status codes and response bodies

### test-actuator.sh
Tests all Spring Boot Actuator monitoring endpoints.

**Usage:**
```bash
./test-actuator.sh
```

**Tests:**
1. Actuator Index - `/actuator`
2. Health - `/actuator/health`
3. Info - `/actuator/info`
4. Metrics - `/actuator/metrics`
5. Environment - `/actuator/env`
6. Beans - `/actuator/beans`
7. HTTP Trace - `/actuator/httptrace`

**Output:**
- JSON formatted responses (requires `jq`)

## Running Tests

### From app directory
```bash
cd app
./tests/test-api.sh
./tests/test-actuator.sh
```

### From tests directory
```bash
cd app/tests
./test-api.sh
./test-actuator.sh
```

## Example Output

### test-api.sh
```
=== Banking Operations API Test Suite ===

Test 1: Health Check
✓ PASS - Health check returned 200
Response: {"status":"UP"}

Test 2: Ready Check
✓ PASS - Ready check returned 200
Response: {"status":"UP"}

...
```

### test-actuator.sh
```
=== Actuator Endpoints Test ===

1. Actuator Index
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/actuator",
      "templated": false
    },
    ...
  }
}

...
```

## Troubleshooting

### Connection refused
- Ensure application is running: `mvn spring-boot:run -DskipTests`
- Check port 8080 is accessible

### jq not found
- Install jq: `sudo apt install jq` (Linux) or `brew install jq` (macOS)
- Or modify test-actuator.sh to remove `| jq .`

### Tests failing
- Check application logs: `tail -f logs/bankingops.log`
- Verify database is initialized: `./init-db.sh`
- Ensure all endpoints are accessible

