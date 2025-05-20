# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Download dependencies (leveraging Docker cache)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
# Fix the spring-boot-maven-plugin issue by modifying the pom.xml
RUN sed -i '/<exclude>/,/<\/exclude>/s/<version>1.18.36<\/version>//' pom.xml && \
    mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose user-service port 8001
EXPOSE 8001

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]