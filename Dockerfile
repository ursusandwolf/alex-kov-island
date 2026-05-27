# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copy only pom.xml files to cache dependencies
COPY pom.xml .
COPY island-engine/pom.xml island-engine/pom.xml
COPY island-nature/pom.xml island-nature/pom.xml
COPY island-simcity/pom.xml island-simcity/pom.xml
COPY island-app/pom.xml island-app/pom.xml
COPY island-benchmarks/pom.xml island-benchmarks/pom.xml

# 2. Pre-fetch dependencies
RUN mvn dependency:go-offline -B

# 3. Copy source code and build
COPY . .
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/island-app/target/island-app-*.jar app.jar

# Expose ports for Spring Boot (8080) and Prometheus (included in 8080/actuator)
EXPOSE 8080

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=nature
ENV SIM_WIDTH=30
ENV SIM_HEIGHT=30

# Fix for JPMS accessibility in some Spring scenarios if needed
ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "app.jar"]
