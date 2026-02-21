# Quantum Edu Backend

Modular monolith backend for Quantum Education — e-commerce for beauty courses + LMS.

## Architecture

Modular monolith with isolated modules. See [docs/MODULAR_ARCHITECTURE.md](docs/MODULAR_ARCHITECTURE.md).

## Tech Stack

- **Java 21**
- **Spring Boot 3.3.5**

## Project Structure

```
quantum-edu-be/
├── pom.xml                 # Parent POM
├── modules/
│   ├── auth/               # Auth module
│   └── user-management/    # User Management module
└── app/                    # Runnable application
```

## Prerequisites

- JDK 21
- Maven 3.6+

## Building

```bash
mvn clean install
```

## Running the Application

```bash
mvn -pl app spring-boot:run
```

## Running Tests

```bash
mvn test
```

## Endpoints

- `GET /health` — Health check (returns `{"status":"UP"}`)

## API Documentation

- [API Response Format](docs/API_RESPONSE_FORMAT.md)
- [Auth API Spec](docs/AUTH_API_SPEC.md)
- [User Management API Spec](docs/USER_MANAGEMENT_API_SPEC.md)
