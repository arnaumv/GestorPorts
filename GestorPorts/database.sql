CREATE DATABASE IF NOT EXISTS gestorports;

USE gestorports;

CREATE TABLE IF NOT EXISTS firewall_rules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    protocol VARCHAR(255) NOT NULL,
    application VARCHAR(255),
    user VARCHAR(255),
    `group` VARCHAR(255),
    ip_address VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    network_interface VARCHAR(255),
    direction VARCHAR(255) NOT NULL
);