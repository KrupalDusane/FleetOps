# 🚀 REST API Documentation

FleetOps exposes a comprehensive, RESTful API. All responses are returned in `application/json`. The application employs standard HTTP status codes and a centralized error handling mechanism.

## 📡 Base URL
`http://localhost:9090/api`

---

## 🚙 1. Vehicle Management (`/api/vehicles`)

### 1.1 Create a Vehicle
- **Purpose**: Registers a new vehicle into the fleet.
- **Method**: `POST`
- **URL**: `/vehicles`
- **Request Body**:
  ```json
  {
    "vehicleNumber": "MH-12-AB-1234",
    "brand": "Volvo",
    "model": "FH16",
    "manufacturingYear": 2022,
    "fuelType": "DIESEL",
    "currentOdometer": 15000,
    "status": "ACTIVE"
  }
  ```
- **Success Response (`201 Created`)**: Returns the saved vehicle object with the generated `id`.
- **Error Responses**:
  - `400 Bad Request`: Validation failure (e.g., blank fields, year out of range).
  - `409 Conflict`: `vehicleNumber` already exists.

### 1.2 Get All Vehicles
- **Purpose**: Retrieves a list of all fleet vehicles.
- **Method**: `GET`
- **URL**: `/vehicles`

### 1.3 Search & Paginate Vehicles
- **Purpose**: Server-side pagination, sorting, and dynamic filtering.
- **Method**: `GET`
- **URL**: `/vehicles/search?search=MH&status=ACTIVE&page=0&size=10&sort=id,desc`
- **Success Response (`200 OK`)**: Returns a Spring Data `Page<Vehicle>` object containing `content`, `totalElements`, and `totalPages`.

### 1.4 Get / Update / Delete Vehicle by ID
- **GET `/vehicles/{id}`** ➔ `200 OK` or `404 Not Found`.
- **PUT `/vehicles/{id}`** ➔ `200 OK`. (Requires full payload).
- **DELETE `/vehicles/{id}`** ➔ `204 No Content`. (Will return `409 Conflict` if the vehicle is currently attached to a driver or log).

---

## 🧑‍✈️ 2. Driver Management (`/api/drivers`)

### 2.1 Create a Driver
- **Method**: `POST`
- **URL**: `/drivers`
- **Request Body**:
  ```json
  {
    "name": "John Doe",
    "licenseNumber": "DL-12345678",
    "phone": "+1-555-0198",
    "status": "AVAILABLE",
    "currentVehicle": {
        "id": 1
    }
  }
  ```
- **Business Logic**: If `currentVehicle` is provided, the backend verifies if the Vehicle ID exists. If not, a `404 Not Found` is thrown before saving the driver.

### 2.2 Driver Endpoints Summary
- **GET `/drivers`**: List all drivers.
- **GET `/drivers/search`**: Paginated search by name/license and status.
- **GET `/drivers/{id}`**: Get driver by ID.
- **PUT `/drivers/{id}`**: Update driver details (can be used to assign/unassign vehicles).
- **DELETE `/drivers/{id}`**: Remove a driver.

---

## ⛽ 3. Fuel Log Management (`/api/fuel-logs`)

### 3.1 Create Fuel Log
- **Method**: `POST`
- **URL**: `/fuel-logs`
- **Request Body**:
  ```json
  {
    "fuelDate": "2026-07-29",
    "fuelQuantity": 50.5,
    "pricePerLitre": 1.20,
    "odometerAtFueling": 15500,
    "vehicle": {
        "id": 1
    }
  }
  ```
- **Business Logic**:
  - The `totalCost` is **auto-calculated** by the backend (`fuelQuantity * pricePerLitre`) and stored in the database. 
  - Throws `400 Bad Request` if `vehicle` is missing.

### 3.2 Fuel Log Endpoints Summary
- **GET `/fuel-logs`**: List all logs.
- **GET `/fuel-logs/search`**: Paginate and filter logs by `vehicleId` and `fuelDate`.
- **GET `/fuel-logs/{id}`**: Get log by ID.
- **PUT `/fuel-logs/{id}`**: Update log (auto-recalculates `totalCost`).
- **DELETE `/fuel-logs/{id}`**: Remove log.

---

## 🛠️ 4. Maintenance Management (`/api/maintenance`)

### 4.1 Create Maintenance Log
- **Method**: `POST`
- **URL**: `/maintenance`
- **Request Body**:
  ```json
  {
    "garage": "Downtown Auto Repair",
    "cost": 450.00,
    "serviceDate": "2026-07-01",
    "nextServiceDate": "2026-12-01",
    "status": "COMPLETED",
    "vehicle": {
        "id": 1
    }
  }
  ```
- **Business Logic**:
  - The backend strictly enforces that `nextServiceDate` must be **after** `serviceDate`. If violated, it throws an `IllegalArgumentException` mapped to `400 Bad Request`.

### 4.2 Maintenance Endpoints Summary
- **GET `/maintenance`**: List all logs.
- **GET `/maintenance/search`**: Paginate and filter logs by `vehicleId`, `garage`, or `status`.
- **GET `/maintenance/{id}`**: Get log by ID.
- **PUT `/maintenance/{id}`**: Update log.
- **DELETE `/maintenance/{id}`**: Remove log.

---

## 🛑 Common HTTP Status Codes

- `200 OK`: Request succeeded.
- `201 Created`: Resource successfully created.
- `204 No Content`: Resource successfully deleted.
- `400 Bad Request`: Validation failure (e.g., missing fields, bad JSON format, invalid sorting parameters).
- `404 Not Found`: The requested resource (ID) does not exist.
- `409 Conflict`: A database constraint was violated (e.g., duplicate unique keys, foreign key deletion restriction).
- `500 Internal Server Error`: An unexpected server-side exception occurred.
