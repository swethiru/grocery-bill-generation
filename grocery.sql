CREATE DATABASE grocery;
USE grocery;
CREATE TABLE items (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Items VARCHAR(50),
    Amount DOUBLE,
    stock INT
);
INSERT INTO items (Items, Amount, stock)
VALUES
    ('Apple', 30.5, 50),
    ('Milk', 20, 30),
    ('Bread', 25, 20);
SELECT * FROM items;
CREATE TABLE bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    Items VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    Amount DOUBLE NOT NULL
);
CREATE TABLE grocery_details (
    cust_id INT AUTO_INCREMENT PRIMARY KEY,
    purchased_amt DOUBLE NOT NULL
);
