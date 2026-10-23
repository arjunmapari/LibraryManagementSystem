# 📚 Library Management System

A web-based **Library Management System** built using **Java, Java Servlets, JDBC, MySQL, Maven, HTML, CSS, and JavaScript**.

The application helps library staff manage books, members, book issuing, returns, availability, and late-return fines through a browser-based interface.

## ✨ Features

### 📊 Dashboard
- View total books, available books, issued books, and registered members.
- View recently added books.
- Monitor library statistics.

### 📖 Book Management
- Add, edit, and delete books.
- Store ISBN, title, author, category, and quantity.
- Search books by ISBN, title, author, or category.

### 👥 Member Management
- Add, edit, and delete library members.
- Store member names, email addresses, phone numbers, and addresses.
- Search registered members.

### 📕 Book Issuing
- Issue books to registered members.
- Record issue dates and due dates.
- Update available book quantities automatically.
- Prevent issuing books when no copies are available.

### 📗 Book Returns
- Record book return dates.
- Update issue status and book availability.
- Calculate overdue days and applicable fines.

**Fine calculation:** ₹5 × number of late days.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Java Servlets | HTTP request handling |
| JDBC | Database connectivity |
| MySQL | Persistent data storage |
| Maven | Dependency management and build |
| HTML & CSS | Page structure and styling |
| JavaScript | Client-side interactions |
| Apache Tomcat 10.1 | Application server |

## 🏗️ Application Architecture

```text
Web Browser
HTML / CSS / JavaScript
          |
          v
Apache Tomcat
Java Servlets
          |
          v
JDBC
          |
          v
MySQL Database
library_db
```

## 🗄️ Database Setup

### Prerequisites

Install and configure:
- Java Development Kit (JDK)
- Apache Maven
- MySQL Server
- MySQL Workbench (optional)
- Apache Tomcat 10.1

### Step 1: Create the database

Open MySQL Workbench, create a new SQL query, and execute the following script.

```sql
CREATE DATABASE IF NOT EXISTS library_db;

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
```

**Important:** This script creates the database tables but does not create an administrator account or configure the application's database connection. Configure the MySQL credentials according to your project settings. Store administrator passwords as secure hashes, never as plain text.

## 🚀 How to Run the Project

The following instructions assume Windows, Maven, and Tomcat 10.1.

### Step 1: Open the project folder

Open PowerShell and navigate to your project directory:

```powershell
cd C:\LibraryManagementSystem
```

Adjust the path if your project is stored elsewhere.

### Step 2: Build the project

Run:

```powershell
mvn clean package
```

After a successful build, Maven should generate a WAR file under the project's `target` directory. The exact filename depends on your Maven configuration.

For example:

```text
target/LibraryManagementSystem.war
```

### Step 3: Deploy the WAR file

Copy the generated WAR file into the Tomcat `webapps` directory.

Update the source and destination paths to match your computer:

```powershell
Copy-Item "C:\LibraryManagementSystem\target\LibraryManagementSystem.war" "C:\path\to\apache-tomcat\webapps\"
```

### Step 4: Start Apache Tomcat

Navigate to Tomcat's `bin` directory:

```powershell
cd "C:\path\to\apache-tomcat\bin"
.\startup.bat
```

### Step 5: Open the application

Once Tomcat has deployed the application, open:

```text
http://localhost:8080/LibraryManagementSystem/
```

If the application does not load, check the Tomcat logs, deployment status, database configuration, and JDBC connection settings.

## 📂 Project Structure

A typical Maven web application may use a structure like this:

```text
LibraryManagementSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── ... (Servlets and Java classes)
│       ├── resources/
│       └── webapp/
│           ├── WEB-INF/
│           ├── css/
│           ├── js/
│           └── ... (Web pages)
├── pom.xml
└── README.md
```

Your actual project structure may differ.

## 🔮 Future Improvements

- Add role-based authentication and authorization.
- Improve input validation and error handling.
- Add automated tests for issuing, returning, and fine calculations.
- Add pagination and more advanced search.
- Improve accessibility and responsive design.

## 👨‍💻 Author

**Arjun Mapari**

- GitHub: [@arjunmapari](https://github.com/arjunmapari)

---

⭐ If you find this project useful, consider giving the repository a star!
