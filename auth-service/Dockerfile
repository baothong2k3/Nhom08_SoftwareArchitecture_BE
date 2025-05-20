# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Downgrade Spring Boot version to a stable release
# Spring Boot 3.4.0 is causing dependency resolution issues
RUN sed -i 's/<version>3.4.0<\/version>/<version>3.2.5<\/version>/g' pom.xml

# Set encoding to avoid MalformedInputException
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Copy source code
COPY src ./src

# Build with retry mechanism in case of network issues
RUN mvn package -DskipTests -Dproject.build.sourceEncoding=UTF-8 || \
    (sleep 5 && mvn package -DskipTests -Dproject.build.sourceEncoding=UTF-8) || \
    (sleep 15 && mvn package -DskipTests -Dproject.build.sourceEncoding=UTF-8)

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose auth-service port
EXPOSE 8002

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]