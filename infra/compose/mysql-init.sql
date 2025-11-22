-- Create databases
CREATE DATABASE IF NOT EXISTS users_db;
CREATE DATABASE IF NOT EXISTS orders_db;
CREATE DATABASE IF NOT EXISTS payments_db;
CREATE DATABASE IF NOT EXISTS delivery_db;

-- Create user with permissions
CREATE USER IF NOT EXISTS 'foodexpress'@'%' IDENTIFIED BY 'foodexpress123';

GRANT ALL PRIVILEGES ON users_db.* TO 'foodexpress'@'%';
GRANT ALL PRIVILEGES ON orders_db.* TO 'foodexpress'@'%';
GRANT ALL PRIVILEGES ON payments_db.* TO 'foodexpress'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'foodexpress'@'%';

FLUSH PRIVILEGES;
