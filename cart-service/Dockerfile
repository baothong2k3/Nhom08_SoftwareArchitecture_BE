# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .

# Fix 1: Downgrade Spring Boot version to a stable release
RUN sed -i 's/<version>3.4.0<\/version>/<version>3.2.5<\/version>/g' pom.xml

# Fix 2: Remove the version attribute from the lombok exclude in spring-boot-maven-plugin
RUN sed -i 's/<exclude>.*<groupId>org.projectlombok<\/groupId>.*<artifactId>lombok<\/artifactId>.*<version>[^<]*<\/version>.*<\/exclude>/<exclude><groupId>org.projectlombok<\/groupId><artifactId>lombok<\/artifactId><\/exclude>/g' pom.xml

# Set encoding environment variable
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Copy source code
COPY src ./src

# Build with explicit encoding parameters
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/cart-service-0.0.1-SNAPSHOT.jar app.jar

# Expose cart-service port
EXPOSE 8004

# Run application with database connection to mariadb container
ENTRYPOINT ["java", "-jar", "-Dspring.datasource.url=jdbc:mariadb://mariadb:3306/cart-service-bookstore?createDatabaseIfNotExist=true", "app.jar"]