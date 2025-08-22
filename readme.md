# Pahana Edu - Online Billing System

This repository contains the source code for the **Pahana Edu - Online Billing System**, a comprehensive Java EE web application designed to digitize and streamline the operations of the Pahana Edu bookshop. This project was developed as part of the Advanced Programming (CIS6003) module.

![Dashboard Screenshot](https://i.imgur.com/your-dashboard-screenshot-link.png)
_Figure 1: The Main Application Dashboard_

---

### 🚀 Project Overview

Pahana Edu is a leading bookshop in Colombo that has traditionally managed its customer accounts and billing processes manually. This system replaces those paper-based methods with an efficient, secure, and user-friendly online (web-based) platform. The application is designed to handle all core operations of the bookshop, from managing customer and product data to generating bills and insightful reports, ultimately improving speed and operational efficiency.

### ✨ Key Features

* **👤 User Management**: Secure user creation, login, and management with role-based access control (RBAC).
    * **Super Admin**: Initial user with full system privileges, including managing other admins and users.
    * **Admin**: Can manage products and customers but has restrictions on deleting critical data.
    * **User**: Can view data and generate bills for customers.
* **📚 Item (Product) Management**: Full CRUD (Create, Read, Update, Delete) functionality for bookshop items, including search, restock, and view capabilities.
* **👥 Customer Management**: Efficiently add, update, delete, and view customer details.
* **🧾 Bill Generation**: A streamlined process to generate bills by searching for a customer (via phone number), adding items, and calculating the total based on units consumed.
* **📊 Dashboards & Reporting**:
    * Custom dashboards for each user role with relevant analytics and shortcuts.
    * Generation of reports for sales, inventory, and customer activity, adding value to the system.
* **❓ Help & Support**: A dedicated help section to provide system usage guidelines for new users.

![Billing Page Screenshot](https://i.imgur.com/your-billing-screenshot-link.png)
_Figure 2: The Bill Generation Interface_

### 🏛️ Architecture

The application is built using a robust, **multi-layered architecture** to ensure a clear separation of concerns, making the system scalable and easy to maintain. This approach is a core requirement of the assignment, which calls for a distributed application with a tiered structure.

1.  **Presentation Layer (View)**: Consists of JavaServer Pages (`.jsp` files) located in the `webapp/pages` directory, along with frontend assets (CSS, JS). This layer is responsible for rendering the user interface.
2.  **Controller Layer (Servlets)**: Java Servlets act as the bridge between the view and the service layer, handling all incoming HTTP requests and routing them appropriately.
3.  **Service Layer (Business Logic)**: Contains the core application logic. Classes like `UserServiceImpl` and `BillServiceImpl` orchestrate business processes and enforce business rules.
4.  **Data Access Layer (DAO)**: This layer abstracts the database interactions. It uses DAO interfaces and their implementations (e.g., `ItemDAO` and `ItemDAOImpl`) to perform CRUD operations using JDBC.
5.  **Entity/Model Layer**: Plain Old Java Objects (POJOs) that represent the core data structures of the application (e.g., `User`, `Customer`, `Item`).

The project follows a modular package structure under `com.icbt.pahanaeduonlinebillingsystem`, with separate packages for each feature (e.g., `bill`, `customer`, `item`).

---

### 🛠️ Technology Stack

This project utilizes a robust stack of technologies to deliver a modern web application experience.

| Category | Technology / Framework | Version |
| :--- | :--- | :--- |
| **Backend** | Java EE (Servlet, JSP) | Servlet 4.0, JSP 2.3 |
| | JDBC | 4.2 |
| **Frontend** | HTML5, CSS3, JavaScript (ES6) | |
| | Tailwind CSS (via CDN) | 3.x |
| | Feather Icons | 4.x |
| | Toastify JS | latest |
| **Database** | MySQL | 8.0 |
| **Build Tool** | Apache Maven | 3.8+ |
| **Web Server** | Apache Tomcat | 9.0+ |
| **Testing** | JUnit | 5.x |
| **PDF Generation**| jsPDF | 2.5+ |
| **Charting** | Chart.js (via jsDelivr) | 3.x |

### 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

#### **Prerequisites**

* JDK 11 or higher
* Apache Maven 3.8 or higher
* Apache Tomcat 9.0 or higher
* MySQL Server 8.0 or higher

#### **Installation & Setup**

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/your-username/PahanaEdu-OnlineBillingSystem.git](https://github.com/your-username/PahanaEdu-OnlineBillingSystem.git)
    cd PahanaEdu-OnlineBillingSystem
    ```
2.  **Database Setup:**
    * Create a new database in MySQL named `pahana_edu_db`.
    * Import the database schema from the `.sql` file provided in the repository.
    * Update the database connection details in `src/main/resources/db.properties`.
3.  **Build the project:**
    ```sh
    mvn clean install
    ```
    This will compile the code, run the tests, and package the application into a `.war` file in the `target/` directory.
4.  **Deploy to Tomcat:**
    * Copy the generated `PahanaEdu-OnlineBillingSystem.war` file to the `webapps` directory of your Apache Tomcat installation.
    * Start the Tomcat server.
5.  **Access the application:**
    * Open your web browser and navigate to `http://localhost:8080/PahanaEdu-OnlineBillingSystem/`

---

### ✅ Testing

The project follows a **Test-Driven Development (TDD)** approach for the backend layers, as required by the assignment. Unit tests for the DAO and Service layers are written using **JUnit**.

* **DAO Layer Tests**: Verify that all CRUD operations are working correctly and that the SQL queries are valid.
* **Service Layer Tests**: Ensure that the business logic, data validation, and transactional integrity are correctly implemented.

To run the tests, execute the following Maven command:
```sh
mvn test