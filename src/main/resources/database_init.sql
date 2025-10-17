-- 创建数据库
CREATE DATABASE IF NOT EXISTS ishopping
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE ishopping;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    role ENUM('CUSTOMER', 'SELLER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    balance DECIMAL(10,2) DEFAULT 0.00,
    points INT DEFAULT 0,
    shipping_address TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    discount DECIMAL(5,2),
    seller_id BIGINT,
    stock INT DEFAULT 0,
    sales INT DEFAULT 0,
    image_url VARCHAR(255),
    images TEXT,
    status ENUM('ON_SALE', 'OFF_SALE', 'OUT_OF_STOCK') DEFAULT 'ON_SALE',
    category VARCHAR(100),
    tags VARCHAR(255),
    rating DECIMAL(3,2) DEFAULT 5.00,
    review_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL
    );

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_fee DECIMAL(10,2) DEFAULT 0.00,
    actual_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING_PAYMENT',
    shipping_address TEXT,
    receiver_name VARCHAR(100),
    receiver_phone VARCHAR(20),
    payment_method VARCHAR(50),
    payment_time TIMESTAMP NULL,
    delivery_time TIMESTAMP NULL,
    receive_time TIMESTAMP NULL,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 创建订单项表
CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           order_id BIGINT NOT NULL,
                                           product_id BIGINT NOT NULL,
                                           quantity INT NOT NULL,
                                           price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
    );

-- 创建购物车表
CREATE TABLE IF NOT EXISTS shopping_cart (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             product_id BIGINT NOT NULL,
                                             quantity INT NOT NULL DEFAULT 1,
                                             create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id)
    );
ALTER TABLE products ADD COLUMN available BOOLEAN DEFAULT TRUE;
-- 修改 orders 表的 status 字段长度
ALTER TABLE orders
    MODIFY COLUMN status VARCHAR(20)
        NOT NULL DEFAULT 'PENDING_PAYMENT';

-- 同时检查其他枚举字段
ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20)
        NOT NULL DEFAULT 'CUSTOMER';

ALTER TABLE products
    MODIFY COLUMN status VARCHAR(20)
        NOT NULL DEFAULT 'ON_SALE';
-- 创建索引以提高查询性能
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_shopping_cart_user ON shopping_cart(user_id);