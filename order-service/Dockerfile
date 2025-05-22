# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Fix the Spring Boot Maven Plugin configuration issue
RUN sed -i '/<exclude>/,/<\/exclude>/{ s/<version>[^<]*<\/version>//g }' pom.xml

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/order-service-0.0.1-SNAPSHOT.jar app.jar

# Expose order-service port 8005
EXPOSE 8005

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]