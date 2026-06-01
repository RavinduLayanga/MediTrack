# MediTrack: Pharmacy Inventory Management System

MediTrack is a robust, modular Command Line Interface (CLI) application built in Java designed to streamline pharmacy inventory management. It utilizes a two-tier architecture with a strictly relational MySQL database backend.

This project was built with a strong emphasis on core Object-Oriented Programming (OOP) principles, secure database transactions, and clean data access patterns.

## Key Features

* **Polymorphic Inventory Handling:** Dynamically handles generic medicines alongside specialized categories (like Tablets and Liquids) using Java inheritance.
* **Secure Data Persistence:** Communicates with MySQL exclusively via `PreparedStatements` to ensure 100% protection against SQL Injection attacks.
* **Atomic Database Transactions:** Utilizes manual JDBC commit/rollback (`setAutoCommit(false)`) to ensure data integrity during complex multi-table inserts (e.g., inserting a base medicine and its specific dosage simultaneously).
* **Supplier Management:** Complete CRUD functionality for tracking pharmaceutical suppliers and updating contact information.
* **Audit Logging:** Built-in tracking for inventory changes and error states.

## Tech Stack

* **Language:** Java (JDK 11+)
* **Database:** MySQL 8.0
* **Database API:** JDBC (Java Database Connectivity)
* **Interface:** CLI (Console)

## Architectural Highlights (Under the Hood)

As a modular application, MediTrack focuses on the strict separation of business logic and data access:

* **The DRY Principle via Varargs:** The Data Access Layer uses Java Varargs (`Object... params`) to create a unified, highly reusable method for executing `SELECT` queries across the entire application without code bloat.
* **Relational Data Mapping:** Uses `Statement.RETURN_GENERATED_KEYS` to safely capture `AUTO_INCREMENT` primary keys immediately upon insertion, preventing race conditions when linking child records (like Tablets) to parent records (Medicines).
* **Safe Type Checking:** Employs `instanceof` and dynamic casting to bridge the gap between Java's OOP memory model and MySQL's relational tables.

## Database Schema Overview

The MySQL database is fully normalized to prevent data redundancy. Core tables include:

* `medicine` (id, name, brand, type, stock, price)
* `tablets` (medicine_id, dosage) - *Foreign key linked to medicine*
* `liquids` (medicine_id, volume) - *Foreign key linked to medicine*
* `supplier` (supplier_id, name, contact, email, address)

## Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/RavinduLayanga/MediTrack.git
```

### 2. Set up the Database

* Open MySQL Workbench or your preferred SQL client.
* Run the provided `schema.sql` file located in the `/database` folder to generate the required tables.

### 3. Configure Environment Variables (Security)

To prevent hardcoding sensitive credentials, this application strictly uses Environment Variables for database connections. Before running the Main class, you must configure the following variables in your IDE (e.g., IntelliJ Run/Debug Configurations) or your system environment:

```env
DB_URL=jdbc:mysql://localhost:3306/meditrack
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password
```

Update the database URL, port, or database name if your configuration differs.

### 4. Compile and Run

Build the project using your preferred IDE and run the application.
