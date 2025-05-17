FROM maven:3.8.5-openjdk-17 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar service-discovery.jar
ENTRYPOINT ["java", "-jar", "service-discovery.jar"]
