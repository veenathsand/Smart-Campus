**Sensor and Room Management API Smart Campus**

A JAX-RS(Jersey 2.41) based RESTful API, with an embedded Grizzly HTTP server to administer uniersty campus rooms and IoT sensors.

Module : 5COSC022W Client-Server Architectures  
Student : S.A.N.D.Veenath
University : University of Westminster

## API Overview

This API gives a full interface to the facilities managers in the campuses to manage:
--Rooms - create, retrieve and decommission campus rooms.
--Sensors - register and monitor IoT sensors (Temperature, CO2, Occupancy)
--Sensor Readings - save the past sensor data and recall to that data.

### Base URL

http://localhost:8080/api/v1

### Technology Stack

- Language : Java 11
- Framework : JAX-RS with Jersey 2.41
- Server : Embedded Grizzly HTTP Server
- Data Storage : In-memory(ConcurrentHashMap)
- Build Tool : Maven

## Project Structure

smart-campus/
├── pom.xml
└── src/main/java/com/smartcampus/
├── Main.java
├── SmartCampusApplication.java
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── DataStore.java
├── resource/
│   ├── DiscoveryResource.java
│   ├── RoomResource.java
│   ├── SensorResource.java
│   └── SensorReadingResource.java
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   └── mapper/
│       ├── RoomNotEmptyExceptionMapper.java
│       ├── LinkedResourceNotFoundExceptionMapper.java
│       ├── SensorUnavailableExceptionMapper.java
│       └── GlobalExceptionMapper.java
└── filter/
└── LoggingFilter.java

## How to Build and Run

### Prereuisties
- Java 11 or higher
- Maven 3.6+
- NetBeans IDE (recommended) or any Maven-compatiable IDE

### Step 1 - Clone the repository
```bash
git clone https://github.com/veenathsand/Smart-Campus.git
cd Smart-Campus
```

### Step 2 - Build the Project
```bash
mvn clean install
```

### Step 3 — Run the server
```bash
mvn exec:java -Dexec.mainClass=com.smartcampus.Main
```
Or in NetBeans: Right-click `Main.java` → Run File

### Step 4 — Verify the server is running
You should see: 
Smart Campus API started successfully!
URL: http://localhost:8080/api/v1/

---

## API Endpoints

### Part 1 — Discovery
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | API metadata and HATEOAS links |

### Part 2 — Room Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/rooms` | Get all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) |

### Part 3 — Sensor Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/sensors` | Get all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |

### Part 4 — Sensor Readings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading |

---

## Sample curl Commands

### 1. Get API discovery information
```bash
curl http://localhost:8080/api/v1
```

### 2. Get all rooms
```bash
curl http://localhost:8080/api/v1/rooms
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-01","name":"Conference Room","capacity":20}'
```

### 4. Get sensors filtered by type
```bash
curl http://localhost:8080/api/v1/sensors?type=Temperature
```

### 5. Add a sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

### 6. Try deleting a room that has sensors (expect 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 7. Register a sensor with a non-existent room (expect 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEST-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"FAKE-999"}'
```

### 8. Post a reading to a MAINTENANCE sensor (expect 403)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":10}'
```

---

## Error Handling

| HTTP Status | Error Code | Scenario |
|-------------|------------|----------|
| 409 Conflict | `ROOM_NOT_EMPTY` | Deleting a room that still has sensors |
| 422 Unprocessable Entity | `LINKED_RESOURCE_NOT_FOUND` | Registering sensor with non-existent room |
| 403 Forbidden | `SENSOR_UNAVAILABLE` | Posting reading to a MAINTENANCE sensor |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Any unexpected runtime error |

---

## Pre-loaded Sample Data

The API comes with pre-loaded data for testing:

**Rooms:**
- `LIB-301` — Library Quiet Study (capacity: 50)
- `LAB-101` — Computer Lab (capacity: 30)
- `HALL-A` — Main Hall (capacity: 200)
- `EMPTY-01` — Empty Room (capacity: 10) — no sensors, safe to delete

**Sensors:**
- `TEMP-001` — Temperature, ACTIVE, in LIB-301
- `CO2-001` — CO2, ACTIVE, in LIB-301
- `OCC-001` — Occupancy, MAINTENANCE, in LAB-101 ← use to test 403
- `TEMP-002` — Temperature, OFFLINE, in HALL-A






