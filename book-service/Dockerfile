# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Fix the Spring Boot Maven plugin configuration
RUN sed -i '/<exclude>/,/<\/exclude>/s/<version>1.18.36<\/version>//g' pom.xml && \
    sed -i '/<annotationProcessorPaths>/,/<\/annotationProcessorPaths>/s/<version>1.18.36<\/version>/<version>${lombok.version}<\/version>/g' pom.xml

# Download dependencies (leveraging Docker cache)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/book-service-0.0.1-SNAPSHOT.jar app.jar

# Expose book-service port 8003
EXPOSE 8003

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]