# 🏔️ MountEdge E-Commerce Platform

![MountEdge Banner](assets/readme/banner.png)

## 🌟 Overview
**MountEdge** is a premium, full-stack e-commerce platform built with a robust **Java Spring Boot** backend and a sleek, modern **Vanilla HTML/CSS/JS** frontend. Designed with a focus on scalability, security, and Object-Oriented Programming (OOPS) principles, it provides a seamless shopping experience for users and a comprehensive management dashboard for administrators.

---

## 🚀 Key Features

### 👤 User Features
- **Secure Authentication**: JWT-based login and registration system.
- **Product Discovery**: Browse items by category with real-time stock availability.
- **Persistent Cart**: Add/remove items with automatic total calculation.
- **Order Management**: Track order history and status (Pending, Shipped, Delivered).
- **Reviews & Ratings**: Share feedback on products with a 5-star rating system.
- **Address Management**: Save multiple shipping addresses for faster checkout.

### 🛠️ Admin Dashboard
- **Inventory Control**: Real-time management of product stock levels.
- **Category Management**: Organize products into logical clusters.
- **Order Oversight**: Monitor and update the status of all customer orders.
- **User Management**: View and manage the platform's user base.

---

## 🛠️ Tech Stack

<p align="left">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate-5.6.5-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white" />
</p>

- **Backend**: Spring Boot 3.2.4 (Rest API), Spring Security (JWT), Spring Data JPA.
- **Database**: MySQL (Relational Mapping).
- **Frontend**: Responsive HTML5, CSS3 (Glassmorphism & Dark Theme), Vanilla JavaScript (Fetch API).
- **Architecture**: Layered Architecture (Controller-Service-Repository-Entity).

---

## 📁 Project Structure

```text
mountedge-e-commerce/
├── src/main/java/com/mountedge/ecommerce/
│   ├── config/      # Security & JWT Configuration
│   ├── controller/  # REST Endpoints
│   ├── dto/         # Data Transfer Objects
│   ├── entity/      # JPA Entities (Database Mapping)
│   ├── repository/  # Data Access Layer
│   ├── service/     # Business Logic Layer
│   └── exception/   # Custom Global Exception Handling
├── src/main/resources/
│   ├── static/      # Frontend (HTML, CSS, JS)
│   ├── schema.sql   # Database Initialization Script
│   └── application.properties # Configurations
├── assets/          # README media & assets
└── pom.xml          # Maven Dependencies
```

---

## ⚙️ Setup & Installation

### 1. Database Configuration
1. Ensure MySQL is installed and running.
2. Create a database named `mountedge`.
3. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

### 2. Build and Run
1. Clone the repository.
2. Navigate to the project root and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. Access the application at: `http://localhost:8080`

---

## 🔐 API Reference Highlights

| Endpoint | Method | Description | Access |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | `POST` | User Registration | Public |
| `/api/auth/login` | `POST` | Get JWT Token | Public |
| `/api/products` | `GET` | All Products | Authenticated |
| `/api/cart` | `GET` | View User Cart | Authenticated |
| `/api/orders` | `POST` | Place Order | Authenticated |
| `/api/admin/inventory` | `PUT` | Update Stock | Admins Only |

---

## 🤝 Contribution
Contributions are welcome! Please fork the repository and submit a pull request.

## 📄 License
This project is licensed under the MIT License.

---
<p align="center">Made with ❤️ by Shlok Shinde</p>
