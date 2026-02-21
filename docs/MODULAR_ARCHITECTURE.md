# Modular Monolith — Architecture & Repository Structure

---

## POM Structure Recommendation

**Use a parent POM with per-module child POMs.**

| Approach | Pros | Cons |
|----------|------|------|
| **Parent + module POMs** ✅ | Clear boundaries, independent builds, version management in one place, standard Maven practice | Slightly more files |
| Single POM, multiple source roots | Simpler | Poor isolation, harder to extract modules |

**Recommendation:** Parent POM at root; each module has its own `pom.xml` with only its dependencies.

---

## Repository Layout

```
quantum-edu-be/
├── pom.xml                          # Parent POM (aggregates modules, defines versions)
├── docs/
│   ├── API_RESPONSE_FORMAT.md
│   ├── AUTH_API_SPEC.md
│   ├── USER_MANAGEMENT_API_SPEC.md
│   └── MODULAR_ARCHITECTURE.md
├── modules/
│   ├── auth/                        # Auth module
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/.../auth/
│   │       │   └── resources/
│   │       └── test/
│   │           └── java/.../auth/
│   ├── user-management/             # User Management module
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/.../usermgmt/
│   │       │   └── resources/
│   │       └── test/
│   ├── product-catalogue/           # Product Catalogue (Phase-2)
│   ├── ownership/                   # Ownership Registry (Phase-2)
│   ├── cart/                        # Cart (Phase-2)
│   ├── lms/                         # LMS (Phase-2)
│   └── common/                      # Shared DTOs, utilities (optional)
│       ├── pom.xml
│       └── src/main/java/.../common/
└── app/                             # Runnable Spring Boot application
    ├── pom.xml                      # Depends on all modules
    └── src/
        ├── main/
        │   ├── java/.../QuantumEduApplication.java
        │   └── resources/
        │       └── application.properties
        └── test/
```

---

## Module Isolation Rules

1. **Each module** has its own package (e.g. `com.quantum.edu.auth`, `com.quantum.edu.usermgmt`).
2. **No cross-module imports** of internal classes. Auth may depend on `user-management` and call its **public API** (service interface), not its repositories or internal classes.
3. **Public API** of a module: interfaces, DTOs, and exceptions that other modules are allowed to use.
4. **Inter-module communication:** In-process calls (Auth → UserProfileService). When extracting to microservices, replace with HTTP/REST.

---

## Parent POM Responsibilities

- Spring Boot version
- Java version (21)
- Dependency management (common versions)
- Plugin management (spring-boot-maven-plugin, etc.)
- Aggregates all modules (`<modules>`)

---

## Module POM Responsibilities

- Declare only dependencies needed by that module
- Parent provides versions via `dependencyManagement`
- No `spring-boot-maven-plugin` in modules (only in `app`)

---

## App Module Responsibilities

- Contains `@SpringBootApplication` main class
- Depends on all domain modules
- Holds `application.properties` / `application.yml`
- Scans component packages of all modules
- **Single deployable artifact** (executable JAR)

---

## Inter-Module Dependencies (Current)

| Module | Depends On | Reason |
|--------|------------|--------|
| auth | user-management | Create user profile on signup |
| app | auth, user-management, ... | Wire and run all modules |

---

## Build & Run

```bash
# Build all
mvn clean install

# Run (from root)
mvn -pl app spring-boot:run

# Build single module
mvn -pl modules/auth clean install
```

---

## Current Module Status

| Module | Status | Notes |
|--------|--------|-------|
| auth | Skeleton | Ready for Auth API implementation |
| user-management | Skeleton | Ready for Create Profile API implementation |
| product-catalogue | Not started | Phase-2 |
| ownership | Not started | Phase-2 |
| cart | Not started | Phase-2 |
| lms | Not started | Phase-2 |
