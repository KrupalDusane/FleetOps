# 🗺️ Roadmap

The FleetOps backend is continually evolving. Below is the proposed roadmap for future architectural improvements and feature additions.

## 🟢 Phase 1: Security & Modernization
- [ ] **Data Transfer Objects (DTOs)**: Introduce MapStruct or ModelMapper to decouple JPA Entities from the Controller boundary, preventing over-posting and accidental data exposure.
- [ ] **JWT Authentication**: Migrate from Spring Security In-Memory authentication to stateless JSON Web Tokens (JWT) for secure, scalable REST API access.
- [ ] **Role-Based Access Control (RBAC)**: Implement User, Role, and Authority entities to differentiate between `ADMIN`, `FLEET_MANAGER`, and `DRIVER` permissions.

## 🟡 Phase 2: Performance & Scalability
- [ ] **Redis Caching**: Integrate Spring Cache (`@Cacheable`) with Redis to temporarily store frequently accessed data (like Vehicle listings) and reduce database load.
- [ ] **Soft Deletes**: Implement Hibernate `@SQLDelete` and `@Where` annotations on core entities to prevent accidental hard-deletions of critical audit logs.
- [ ] **Database Migrations**: Adopt Flyway or Liquibase for strict, version-controlled database schema management rather than relying on `ddl-auto=update`.

## 🟠 Phase 3: Observability & DevOps
- [ ] **Dockerization**: Create a `Dockerfile` and `docker-compose.yml` to spin up the Spring Boot app and MySQL database simultaneously.
- [ ] **CI/CD Pipeline**: Add GitHub Actions for automated Maven building, Checkstyle linting, and running unit tests on every pull request.
- [ ] **Centralized Logging**: Introduce ELK Stack (Elasticsearch, Logstash, Kibana) or integration with Datadog for production-grade logging and monitoring.

## 🔴 Phase 4: Advanced Features
- [ ] **Geolocation Tracking**: Integrate PostGIS and mobile GPS endpoints to track vehicle locations in real-time.
- [ ] **Automated Alerts**: Use Spring Scheduling or Quartz to trigger email/SMS alerts when a vehicle is due for maintenance based on its `nextServiceDate` or `currentOdometer`.
- [ ] **Reporting Service**: Build endpoints that aggregate fuel consumption and maintenance costs to generate monthly PDF/CSV expense reports.
