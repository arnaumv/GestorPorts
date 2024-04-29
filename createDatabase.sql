CREATE DATABASE IF NOT EXISTS gestorfirewall;

USE gestorfirewall;

CREATE TABLE IF NOT EXISTS reglas_firewall (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    puerto INT NOT NULL,
    protocolo VARCHAR(255) NOT NULL,
    aplicacion VARCHAR(255),
    usuario VARCHAR(255),
    grupo VARCHAR(255),
    direccion_ip VARCHAR(255),
    accion VARCHAR(255) NOT NULL,
    interfaz_red VARCHAR(255),
    direccion VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_borrada TIMESTAMP
);



CREATE TABLE IF NOT EXISTS reglas_historial (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    puerto INT NOT NULL,
    protocolo VARCHAR(255) NOT NULL,
    aplicacion VARCHAR(255),
    usuario VARCHAR(255),
    grupo VARCHAR(255),
    direccion_ip VARCHAR(255),
    accion VARCHAR(255) NOT NULL,
    interfaz_red VARCHAR(255),
    direccion VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_borrada TIMESTAMP
);