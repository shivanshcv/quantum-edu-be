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

## IDE Setup (Lombok)

This project uses Lombok. If you see compilation errors in your IDE (e.g. "cannot find symbol: method builder()") while Maven builds succeed:

1. **Enable annotation processing** — IntelliJ: *Settings → Build, Execution, Deployment → Compiler → Annotation Processors* → check **Enable annotation processing**
2. Install the **Lombok plugin** if your IDE doesn't include it (IntelliJ: *Settings → Plugins*)

Maven runs Lombok via `maven-compiler-plugin`'s `annotationProcessorPaths`; the IDE uses its own compiler and must have annotation processing enabled.

## Building

```bash
mvn clean install
```

## Running the Application

```bash
mvn -pl app spring-boot:run
```

**Environment profiles:**
- `dev` (default) — MySQL (quantum_education, root/root), email enabled. Use for local testing.
- `staging` / `prod` — Placeholder config; exact values to be provided later.

```bash
# Staging/Production
SPRING_PROFILES_ACTIVE=staging mvn -pl app spring-boot:run
```

## Running Tests

```bash
mvn test
```

## Endpoints

- `GET /health` — Health check (returns `{"status":"UP"}`)
- `POST /api/v1/auth/signup` — User registration
- `POST /api/v1/auth/login` — User login
- `POST /api/v1/auth/verify-email` — Email verification
- `POST /api/v1/auth/resend-verification` — Resend verification email

## API Documentation

- [API Response Format](docs/API_RESPONSE_FORMAT.md)
- [Auth API Spec](docs/AUTH_API_SPEC.md)
- [User Management API Spec](docs/USER_MANAGEMENT_API_SPEC.md)

## Admin Panel

Django admin for managing courses, products, and data: `quantum-edu-admin` (sibling repo). See [docs/DJANGO_ADMIN_SETUP.md](docs/DJANGO_ADMIN_SETUP.md) for migration requirements.
