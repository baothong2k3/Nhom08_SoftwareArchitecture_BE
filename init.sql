-- Tạo databases cho các service
CREATE DATABASE IF NOT EXISTS `auth-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `user-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `cart-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `order-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `book-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `payment-service-bookstore`;
CREATE DATABASE IF NOT EXISTS `inventory-service-bookstore`;

-- Cấp quyền cho user root
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES; 