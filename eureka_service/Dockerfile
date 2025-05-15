FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
ENV LANG=en_US.UTF-8
RUN ./mvnw clean package -DskipTests -Dfile.encoding=UTF-8

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","app.jar"]
