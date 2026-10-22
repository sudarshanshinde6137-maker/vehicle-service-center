CREATE DATABASE IF NOT EXISTS vehicle_service_center;
USE vehicle_service_center;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash CHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  address VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS vehicles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  reg_no VARCHAR(20) UNIQUE NOT NULL,
  make VARCHAR(50),
  model VARCHAR(50),
  year INT,
  photo VARCHAR(255) DEFAULT NULL,
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS services (
  id INT AUTO_INCREMENT PRIMARY KEY,
  vehicle_id INT NOT NULL,
  service_type VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  service_date DATE,
  cost DECIMAL(10,2) DEFAULT 0,
  status ENUM('Pending','In Progress','Completed','Delivered') DEFAULT 'Pending',
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

DROP TRIGGER IF EXISTS set_service_date;
DELIMITER //
CREATE TRIGGER set_service_date
BEFORE INSERT ON services
FOR EACH ROW
BEGIN
  IF NEW.service_date IS NULL THEN
    SET NEW.service_date = CURDATE();
  END IF;
END//
DELIMITER ;

-- default login: admin / admin123
INSERT IGNORE INTO users (username, password_hash) VALUES ('admin', SHA2('admin123', 256));

-- sample customers
INSERT IGNORE INTO customers (name, phone, email, address) VALUES
  ('Sudarshan Kumar', '9876543210', 'sudarshan@example.com', 'Chennai'),
  ('Anita Sharma', '9123456780', 'anita.sharma@example.com', 'Bengaluru'),
  ('Rahul Verma', '9988776655', 'rahul.verma@example.com', 'Hyderabad');


