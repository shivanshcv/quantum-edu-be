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

# Run stage - Java 21 JRE (use non-Alpine: Alpine + musl causes SIGSEGV with BCrypt/native libs)
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar /tmp/app.jar

# Class Data Sharing archive from a training run (context refresh only, no DB connection).
# Cuts startup substantially on small CPUs (e.g. Render free); the JVM ignores the archive if unusable.
RUN java -Djarmode=tools -jar /tmp/app.jar extract --destination /app \
    && rm /tmp/app.jar \
    && SPRING_PROFILES_ACTIVE=prod MYSQL_HOST=localhost JWT_SECRET=cds-training-only-0123456789abcdef0123456789abcdef \
       java -XX:ArchiveClassesAtExit=app.jsa -Xlog:cds=off -Xlog:cds+dynamic=off -Dspring.context.exit=onRefresh \
            -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false -jar app.jar

EXPOSE 8080
# SPRING_PROFILES_ACTIVE=staging set via deploy env vars
# stdbuf forces line-buffered stdout so docker logs -f shows real-time output (Java buffers when stdout is a pipe)
# Fallback to plain java if stdbuf is unavailable (e.g. minimal base images)
ENTRYPOINT ["/bin/sh", "-c", "if command -v stdbuf >/dev/null 2>&1; then exec stdbuf -oL -eL java -XX:SharedArchiveFile=app.jsa -jar app.jar; else exec java -XX:SharedArchiveFile=app.jsa -jar app.jar; fi"]
