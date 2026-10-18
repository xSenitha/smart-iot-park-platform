-- MySQL Initialization Script for Smart IoT Park Platform
CREATE DATABASE IF NOT EXISTS smartpark_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'smartpark_user'@'%' IDENTIFIED BY 'smartpark_dev_pass';
GRANT ALL PRIVILEGES ON smartpark_db.* TO 'smartpark_user'@'%';

FLUSH PRIVILEGES;
