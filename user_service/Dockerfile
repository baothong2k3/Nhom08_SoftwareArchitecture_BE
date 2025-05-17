FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Thay đổi lệnh build để tránh sử dụng spring-boot-maven-plugin
RUN mvn clean package -DskipTests -Dspring-boot.repackage.skip -Dmaven.test.skip=true

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar user-service.jar
EXPOSE 8001
ENTRYPOINT ["java", "-jar", "user-service.jar"]
