DROP DATABASE IF EXISTS `PahanaEdu_OnlineBillingSystem`;
CREATE DATABASE IF NOT EXISTS `PahanaEdu_OnlineBillingSystem`;

USE `PahanaEdu_OnlineBillingSystem`;

-- Users Table
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'STAFF') DEFAULT 'STAFF',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers Table
CREATE TABLE customers (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           account_number VARCHAR(20) UNIQUE NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           address TEXT,
                           phone VARCHAR(15),
                           units_consumed INT DEFAULT 0,

                           created_by INT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_by INT,
                           updated_at TIMESTAMP NULL,
                           deleted_by INT,
                           deleted_at TIMESTAMP NULL,

                           FOREIGN KEY (created_by) REFERENCES users(id),
                           FOREIGN KEY (updated_by) REFERENCES users(id),
                           FOREIGN KEY (deleted_by) REFERENCES users(id)
);

-- Items Table
CREATE TABLE items (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       unit_price DECIMAL(10, 2) NOT NULL,

                       created_by INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_by INT,
                       updated_at TIMESTAMP NULL,
                       deleted_by INT,
                       deleted_at TIMESTAMP NULL,

                       FOREIGN KEY (created_by) REFERENCES users(id),
                       FOREIGN KEY (updated_by) REFERENCES users(id),
                       FOREIGN KEY (deleted_by) REFERENCES users(id)
);

-- Bills Table
CREATE TABLE bills (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       customer_id INT NOT NULL,
                       item_id INT NOT NULL,
                       units INT NOT NULL,
                       total DECIMAL(10,2) NOT NULL,
                       generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       FOREIGN KEY (customer_id) REFERENCES customers(id),
                       FOREIGN KEY (item_id) REFERENCES items(id)
);
