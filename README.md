<p align="center">
  <img src="assets/banner.png" alt="FleetOps Banner" width="100%">
</p>

<h1 align="center">FleetOps 🚚 - Smart Fleet Management System</h1>

<p align="center">
  <strong>A high-performance, enterprise-grade fleet management backend built with Spring Boot 3, Hibernate, and MySQL.</strong>
</p>

<p align="center">
  <a href="https://java.com"><img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"></a>
  <a href="https://hibernate.org/"><img src="https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"></a>
  <a href="https://mysql.com/"><img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"></a>
  <a href="https://maven.apache.org/"><img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven"></a>
  <a href="https://swagger.io/"><img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger"></a>
</p>

<p align="center">
  <a href="https://github.com/KrupalDusane/FleetOps/issues"><img src="https://img.shields.io/github/issues/KrupalDusane/FleetOps?style=flat-square&color=critical" alt="Issues"></a>
  <a href="https://github.com/KrupalDusane/FleetOps/stargazers"><img src="https://img.shields.io/github/stars/KrupalDusane/FleetOps?style=flat-square&color=success" alt="Stars"></a>
  <a href="https://github.com/KrupalDusane/FleetOps/network/members"><img src="https://img.shields.io/github/forks/KrupalDusane/FleetOps?style=flat-square&color=blue" alt="Forks"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/KrupalDusane/FleetOps?style=flat-square" alt="License"></a>
</p>

<hr>

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Business Problem & Solution](#-business-problem--solution)
- [Key Features](#-key-features)
- [Architecture & Database](#-architecture--database)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Project Screenshots](#-project-screenshots)
- [Future Scope](#-future-scope)
- [Contributing](#-contributing)
- [License](#-license)

## 🏢 Project Overview
**FleetOps** is a comprehensive, RESTful backend service designed to manage the lifecycle of commercial vehicle fleets. It provides robust APIs for tracking vehicles, assigning drivers, logging fuel expenses, and scheduling preventive maintenance. 

Designed with enterprise-grade standards, FleetOps utilizes a layered Spring Boot architecture, rigorous Jakarta validation, and a global exception handling mechanism to ensure data integrity and system reliability.

## 💡 Business Problem & Solution
**The Problem:** Logistics companies often struggle with fragmented data. Tracking vehicle status, driver assignments, fuel costs, and maintenance schedules across different spreadsheets leads to operational inefficiencies, missed maintenance windows, and inflated expenses.

**The Solution:** FleetOps centralizes all logistics data into a single, cohesive relational database. By exposing structured REST APIs, frontend dashboards and mobile applications can consume real-time fleet analytics, ensuring vehicles are maintained, fueled, and driven efficiently.

## ✨ Key Features
- **🚗 Vehicle Management**: Complete CRUD operations to track fleet inventory (Brand, Model, Manufacturing Year, Fuel Type, and Status).
- **🧑‍✈️ Driver Management**: Assign drivers to vehicles, track licensing, and monitor driver availability.
- **⛽ Fuel Log Tracking**: Record fuel expenses, track price per liter, and automatically calculate total operational fuel costs.
- **🛠️ Maintenance Scheduling**: Log garage visits, track service costs, and schedule next-service dates to prevent breakdowns.
- **🔍 Advanced Search & Pagination**: Highly optimized Spring Data JPA queries to search, filter, and paginate through thousands of records efficiently.
- **🛡️ Robust Validation & Error Handling**: Comprehensive input validation with a centralized `@RestControllerAdvice` interceptor that maps runtime exceptions to meaningful HTTP status codes (e.g., 409 Conflict for duplicate data).
- **📑 OpenAPI Documentation**: Interactive Swagger UI for developers to explore and test the API effortlessly.

## 🏗 Architecture & Database
For deep dives into the technical implementation, please refer to the dedicated documentation:
- 📐 [**Architecture Documentation**](ARCHITECTURE.md): Request flow, dependency injection, and system design.
- 🗄️ [**Database Documentation**](DATABASE.md): Entity-Relationship (ER) diagrams, normalization, and constraints.

## 🛠️ Tech Stack
- **Core**: Java 21, Spring Boot 3.1
- **Persistence**: Spring Data JPA, Hibernate, MySQL 8
- **Build & Dependency Management**: Apache Maven
- **Validation**: Jakarta Bean Validation
- **Documentation**: Springdoc OpenAPI (Swagger 3)
- **Security**: Spring Security (In-Memory Auth)
- **Templating**: Thymeleaf (for minimal internal dashboard rendering)

## 🚀 Getting Started

### Prerequisites
- JDK 21 or higher
- MySQL 8.x
- Maven 3.x

### Installation & Configuration
1. **Clone the repository:**
   ```bash
   git clone https://github.com/KrupalDusane/FleetOps.git
   cd FleetOps
   ```

2. **Configure Database:**
   Ensure MySQL is running and create a database named `fleetops`.
   Update the `src/main/resources/application.properties` with your credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/fleetops
   spring.datasource.username=root
   spring.datasource.password=${DB_PASSWORD:password}
   ```
   *(Note: Set the `DB_PASSWORD` environment variable, or replace `password` with your local DB password).*

3. **Build the Project:**
   ```bash
   mvn clean install
   ```

4. **Run the Application:**
   ```bash
   mvn spring-boot:run
   ```
   The server will start on `http://localhost:9090`.

## 📚 API Documentation
For detailed API specifications, request/response payloads, and status codes, please view the [**API Documentation**](API.md).

Once the application is running, you can access the interactive Swagger UI at:
👉 **`http://localhost:9090/swagger-ui/index.html`**

## 📸 Project Screenshots

<details>
  <summary><b>Click to expand screenshots</b></summary>

  ### Dashboard Overview
  *(A screenshot of the dashboard metrics goes here)*
  <img src="assets/dashboard-overview.png" width="800" alt="Dashboard">

  ### Vehicle Data Grid
  *(A screenshot of the paginated vehicles API/table goes here)*
  <img src="assets/vehicle-grid.png" width="800" alt="Vehicles">

  ### Swagger Interactive UI
  <img src="assets/swagger-api.png" width="800" alt="Swagger UI">
</details>

## 🔮 Future Scope
Read the [**Roadmap**](ROADMAP.md) for our long-term vision, which includes implementing JWT Authentication, Data Transfer Objects (DTOs), and Redis caching.

## 🤝 Contributing
Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**. 

Please read the [**Contributing Guidelines**](CONTRIBUTING.md) and the [**Code of Conduct**](CODE_OF_CONDUCT.md) before submitting a Pull Request.

## 📝 License
Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.

## 👤 Author
**Krupal Dusane**
- GitHub: [@KrupalDusane](https://github.com/KrupalDusane)
- LinkedIn: [Krupal Dusane](https://linkedin.com/in/krupaldusane) (Update link if necessary)

## 🙏 Acknowledgements
- [Spring Initializr](https://start.spring.io/)
- [Shields.io](https://shields.io/)
- [FontAwesome](https://fontawesome.com/)
