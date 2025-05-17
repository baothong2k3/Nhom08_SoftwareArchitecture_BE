FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Thêm tham số -Dspring-boot.repackage.skip=true để bỏ qua bước repackage
RUN mvn clean package -DskipTests -Dspring-boot.repackage.skip=true

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar inventory-service.jar
EXPOSE 8006
ENTRYPOINT ["java", "-jar", "inventory-service.jar"]
