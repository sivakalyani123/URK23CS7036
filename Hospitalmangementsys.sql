CREATE DATABASE hospital_db;

USE hospital_db;

CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    gender VARCHAR(10),
    contact VARCHAR(20)
);

CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    specialization VARCHAR(100),
    contact VARCHAR(20)
);
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor VARCHAR(100),
    date DATE,
    time TIME,
    status VARCHAR(20)
);

select * from appointments;
select * from doctors;
select * from patients;
alter table doctors add Experience int;
alter table doctors add Available_hours int;
DROP TABLE appointments;
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(100) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    appointment_date VARCHAR(20),
    appointment_time VARCHAR(20),
    reason TEXT
);
