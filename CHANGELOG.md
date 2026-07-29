# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Standard Open-Source repository configuration (Code of Conduct, Contributing Guidelines, Security Policy).
- Detailed Architectural and Database schemas.
- Comprehensive REST API documentation.

### Changed
- Centralized `GlobalExceptionHandler` to handle `DataIntegrityViolationException`, returning `409 Conflict` instead of `500 Internal Server Error`.
- Upgraded mapping of `IllegalArgumentException`, `HttpMessageNotReadableException`, and `InvalidDataAccessApiUsageException` to return `400 Bad Request`.
- Refactored `application.properties` to utilize environment variables for database credentials (`${DB_PASSWORD:password}`).

### Removed
- Cleaned Git tracking tree by removing temporary test payloads (`cookie.txt`, `payload.json`), local debug scripts (`TestEntityManager.java`), and compiled bytecode (`.class`).

## [1.0.0] - Initial Release

### Added
- Core backend framework utilizing Spring Boot 3 and Java 21.
- Complete domain logic for Vehicles, Drivers, Fuel Logs, and Maintenance.
- MySQL database integration via Spring Data JPA and Hibernate.
- Pagination and Dynamic Search functionality for all entities.
- Jakarta Validation applied to all incoming DTO requests.
- Automatic Swagger/OpenAPI documentation generation.
