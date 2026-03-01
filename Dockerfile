# Build stage - Maven + Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent and module poms first for better layer caching
COPY pom.xml .
COPY modules/common/pom.xml modules/common/
COPY modules/auth/pom.xml modules/auth/
COPY modules/user-management/pom.xml modules/user-management/
COPY modules/product-catalogue/pom.xml modules/product-catalogue/
COPY modules/ownership/pom.xml modules/ownership/
COPY modules/cart/pom.xml modules/cart/
COPY modules/lms/pom.xml modules/lms/
COPY modules/bff/pom.xml modules/bff/
COPY app/pom.xml app/

# Download dependencies (cached if poms unchanged)
RUN mvn dependency:go-offline -B -q || true

# Copy source and build
COPY . .
RUN mvn clean package -DskipTests -B -q

# Run stage - Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
# SPRING_PROFILES_ACTIVE=staging set via Render env vars
ENTRYPOINT ["java", "-jar", "app.jar"]
