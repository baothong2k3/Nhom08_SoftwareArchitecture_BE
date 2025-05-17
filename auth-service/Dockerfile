FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar auth-service.jar
EXPOSE 8002
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
