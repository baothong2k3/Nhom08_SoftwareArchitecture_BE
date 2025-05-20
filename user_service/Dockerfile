# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Fix the exclude configuration by removing the version attribute
RUN sed -i 's/<exclude>.*<groupId>org\.projectlombok<\/groupId>.*<artifactId>lombok<\/artifactId>.*<version>[^<]*<\/version>.*<\/exclude>/<exclude><groupId>org.projectlombok<\/groupId><artifactId>lombok<\/artifactId><\/exclude>/g' pom.xml

# Set proper encoding to avoid MalformedInputException
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Download dependencies (leveraging Docker cache)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose service port
EXPOSE 8001

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]