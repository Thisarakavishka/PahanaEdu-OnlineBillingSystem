-- Drop if exists and recreate if not exists database
DROP DATABASE IF EXISTS `PahanaEdu_OnlineBillingSystem`;
CREATE DATABASE IF NOT EXISTS `PahanaEdu_OnlineBillingSystem`;

-- Use Database
USE `PahanaEdu_OnlineBillingSystem`;

-- Users Table
CREATE TABLE users
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100)           NOT NULL UNIQUE,
    password   VARCHAR(255)           NOT NULL,
    salt       VARCHAR(255)           NOT NULL,
    role       ENUM ('ADMIN', 'USER') NOT NULL DEFAULT 'USER',

    created_by INT                             DEFAULT NULL,
    created_at TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP,
    updated_by INT                             DEFAULT NULL,
    updated_at TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by INT                             DEFAULT NULL,
    deleted_at TIMESTAMP              NULL     DEFAULT NULL,

    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
);

-- Customers Table
CREATE TABLE customers
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    name           VARCHAR(100)       NOT NULL,
    address        TEXT,
    phone          VARCHAR(20) UNIQUE NOT NULL,
    units_consumed INT                NOT NULL DEFAULT 0,

    created_by     INT                         DEFAULT NULL,
    created_at     TIMESTAMP                   DEFAULT CURRENT_TIMESTAMP,
    updated_by     INT                         DEFAULT NULL,
    updated_at     TIMESTAMP                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by     INT                         DEFAULT NULL,
    deleted_at     TIMESTAMP          NULL     DEFAULT NULL,

    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
);

-- Items Table
CREATE TABLE items
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255)   NOT NULL UNIQUE,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,

    created_by INT       DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT       DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by INT       DEFAULT NULL,
    deleted_at TIMESTAMP      NULL,

    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
);

-- Bills Table
CREATE TABLE bills
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT            NOT NULL,
    item_id      INT            NOT NULL,
    units        INT            NOT NULL,
    total        DECIMAL(10, 2) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
);
