# 📚 Library Management System

A web-based **Library Management System** developed using **Java, Servlets, JDBC, MySQL, Maven, HTML, CSS, and JavaScript**.

The system provides a simple and efficient way to manage books, library members, book issuing, and book returns through a browser-based interface.

---

## 📌 Project Overview

The Library Management System is designed to digitize common library operations.

It allows an administrator/library staff to:

- Manage books
- Manage library members
- Issue books to members
- Return issued books
- Track available and issued books
- Calculate fines for late returns
- Search books and members
- View library statistics through a dashboard

The application uses **Java Servlets** for backend processing, **JDBC** for database connectivity, and **MySQL** for persistent data storage.

---

## ✨ Features

### 📊 Dashboard

The dashboard provides an overview of the library:

- Total books
- Available books
- Issued books
- Total members
- Recently added books
- Library status
- 
Frontend (HTML/CSS/JS)
        ↓
Java Servlet / REST API
        ↓
JDBC
        ↓
MySQL

1.Put this SQL into MySQL
Open MySQL Workbench → SQL Editor → New Query, paste the following, and click the ⚡ Execute button:
CREATE DATABASE library_db;

USE library_db;

CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(30),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issues (
    id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',

    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE TABLE admins (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

---

### 📖 Book Management

The system provides complete CRUD operations for books.

#### Add Book

Library staff can add a new book with:

- ISBN
- Book title
- Author
- Category
- Quantity

#### Edit Book

Existing book information can be updated.

#### Delete Book

Books can be removed from the library database when appropriate.

#### Search Books

Books can be searched using:

- ISBN
- Title
- Author
- Category

---
How to run the project
1.cd LibraryManagementSystem
2.mvn clean package
3.This create C:\LibraryManagementSystem\target\LibraryManagementSystem.war
4.Copy the WAR file to your Tomcat webapps folder:Copy-Item "C:\LibraryManagementSystem\target\LibraryManagementSystem.war" "C:\Users\mapar\OneDrive\Documents\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\"
5.start tomcat cd "C:\Users\mapar\OneDrive\Documents\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin"
.\startup.bat
6.open chrome http://localhost:8080/LibraryManagementSystem/

### 👥 Member Management

Library members can be managed through the system.

Member information includes:

- Name
- Email
- Phone
- Address

Available operations:

- Add member
- Edit member
- Delete member
- Search members

---

### 📕 Issue Book

Books can be issued to registered members.

The system records:

- Book
- Member
- Issue date
- Due date

When a book is issued, its available quantity is automatically decreased.

The system also prevents issuing a book when no copies are available.

---

### 📗 Return Book

Issued books can be returned through the Return Book section.

The system:

- Records the return date
- Changes the issue status
- Updates the available book quantity
- Calculates late days
- Calculates the applicable fine

#### Fine Calculation

The current fine rate is:Fine = Late Days × ₹5
┌──────────────────────┐
│      Web Browser     │
│  HTML / CSS / JS     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│    Apache Tomcat     │
│      Java Servlets   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│         JDBC         │
│   Database Access    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│        MySQL         │
│     library_db       │
└──────────────────────┘


```text
₹5 per late day
