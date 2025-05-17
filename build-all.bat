@echo off
echo Building all Spring Boot microservices...

cd service-discovery
call mvn clean package -DskipTests
cd ..

cd config-server
call mvn clean package -DskipTests
cd ..

cd api-gateway
call mvn clean package -DskipTests
cd ..

cd user_service
call mvn clean package -DskipTests
cd ..

cd auth-service
call mvn clean package -DskipTests
cd ..

cd book-service
call mvn clean package -DskipTests
cd ..

cd cart-service
call mvn clean package -DskipTests
cd ..

cd order-service
call mvn clean package -DskipTests
cd ..

cd inventory-service
call mvn clean package -DskipTests
cd ..

echo All services built successfully!
