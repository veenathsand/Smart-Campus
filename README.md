# Sensor and Room Management API Smart Campus

A JAX-RS(Jersey 2.41) based RESTful API, deployed on Apache Tomcat server to 
administer university campus rooms and IoT sensors.

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
- Server : Apache Tomcat 9.0
- Data Storage : In-memory(ConcurrentHashMap)
- Build Tool : Maven

## Project Structure

smart-campus/
├── pom.xml
└── src/main/
    ├── java/com/smartcampus/
    │   ├── SmartCampusApplication.java
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   └── SensorReading.java
    │   ├── store/
    │   │   └── DataStore.java
    │   ├── resource/
    │   │   ├── DiscoveryResource.java
    │   │   ├── RoomResource.java
    │   │   ├── SensorResource.java
    │   │   └── SensorReadingResource.java
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java
    │   │   ├── LinkedResourceNotFoundException.java
    │   │   ├── SensorUnavailableException.java
    │   │   └── mapper/
    │   │       ├── RoomNotEmptyExceptionMapper.java
    │   │       ├── LinkedResourceNotFoundExceptionMapper.java
    │   │       ├── SensorUnavailableExceptionMapper.java
    │   │       └── GlobalExceptionMapper.java
    │   └── filter/
    │       └── LoggingFilter.java
    └── webapp/
        └── WEB-INF/
            └── web.xml

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
Right-click the project in NetBeans → Run
NetBeans will automatically deploy to Apache Tomcat.

### Step 4 — Verify the server is running
You should see in the output console:
OK - Deployed application at context path [/smart-campus]
OK - Started application at context path [/smart-campus]

### Base URL for all API requests:
http://localhost:8080/smart-campus/api/v1

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
curl http://localhost:8080/smart-campus/api/v1

### 2. Get all rooms
curl http://localhost:8080/smart-campus/api/v1/rooms

### 3. Create a new room
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-01","name":"Conference Room","capacity":20}'

### 4. Get sensors filtered by type
curl "http://localhost:8080/smart-campus/api/v1/sensors?type=Temperature"

### 5. Add a sensor reading
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'

### 6. Try deleting a room that has sensors (expect 409)
curl -X DELETE http://localhost:8080/smart-campus/api/v1/rooms/LIB-301

### 7. Register a sensor with a non-existent room (expect 422)
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEST-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"FAKE-999"}'

### 8. Post a reading to a MAINTENANCE sensor (expect 403)
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":10}'

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


## Questions and Answers

### Part 01
Question 01
In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.
JAX-RS by default, instantiates a new instance of a resource class each time there is an incoming HTTP request. This is referred to as the per-request lifecycle. Due to this, any information stored as an instance variable within a resource class would be lost with each request. To safely manage shared in-memory data across multiple requests, this project uses a singleton Data Store class. The Data Store keeps all data rooms, sensors and readings in Concurrent HashMap data structures, which are thread-safe by default. It implies that in case of several requests being made simultaneously, race condition will not corrupt or lose the data.

Question 02 
Why is the provision of “Hypermedia” (links and navigation within responses) considering a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?
HATEOAS is the initials of Hypermedia as the Engine of Application State. It implies that API responses have navigational links, which inform the client of what they can do and where they should proceed next. To illustrate, the discovery endpoint of this API provides links to /api/v1/rooms and /api/v1/sensors within the response body. This is advantageous to client developers since they do not have to hard code URLs or even depend on pre-recorded documentation. When API restructures its URLs and any clients that track links dynamically then the links will still be functional and will not need to change, which is much more resilient and maintains much better.

### Part 02
Question 01
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.
Use of only IDs makes the response very small and minimizes network bandwidth, which is desirable when very large collections are involved. It, however, makes the client issue a new HTTP request to get the complete details of each ID, which makes the total count of network calls. Full room objects also make the payload larger but provide the client with all the information it requires in a single request, cutting down round trips. In the case of this Smart Campus API, complete objects are provided, since the data set is small, and the client convenience is more important than bandwidth optimization.

Question 02
Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.
Yes, in this implementation, the DELETE operation is idempotent. The initial request to be sent to delete a room that exists and has no sensors is removed and the server responds with 204 No Content. When the same request of deleting a room is sent again, the room is not available any more hence the server will respond with 404 Not Found. The significant aspect is that the server state remains the same when the initial successful deletion is made. Repeat of the request yields the same result, the room does not exist, and this fulfils the definition of idempotency.

### Part 03 
Question 01
We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
A client request sent with a Content-Type header value different than application/json, such as text/plain or application/xml, is automatically intercepted by JAX-RS before it is sent to the resource method and causes a 415 Unsupported Media Type response to be sent. The resource method code is never run. Here is among the most important benefits of utilizing the @Consumes annotation - it serves as an inbuilt content type guard and prevents the API being exposed to malformed or unexpected types of data without any explicit validation code within the method itself.

Question 02 
You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the queryparameterapproachgenerallyconsideredsuperiorforfilteringandsearching collections?
Filtering with such easy method as @QueryParam /api/v1/sensors?type=CO2 is believed to be better since query parameters are optional in their nature. The endpoint /api/v1/sensors are completely valid and returns all the sensors in case no filter is given. When the type is in the path e.g. /api/v1/sensors/type/CO2, then this would mean type is a mandatory identifier of a particular resource, and not an optional filter. The defined REST convention of searching, filtering, and sorting collections is query parameters, making the API more understandable and simpler to use by client developers.

### Part 04 
Question 01
Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?
Sub-Resource Locator pattern enhances the structure of huge APIs by assigning the task of the nested resources to specific classes. Sensor Resource in this project coordinates all sensor-level activities and in case a request relates to readings, it forwards to another Sensor Reading Resource class. This method ensures that individual classes are dedicated to a single task and the code is less cumbersome to read, test, and maintain. Assuming that the logic of readings must be altered, then Sensor Reading Resource should be altered but not Sensor Resource. Conversely, having all logic within a single huge controller class would render the code very hard to maintain as the API expands.

### Part 05
Question 01 
WhyisHTTP422oftenconsideredmoresemanticallyaccurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?
Status 404 Not Found indicates that the endpoint of the URL requested is not there on the server. Processable Entity is an HTTP code of status 422 that indicates that the server has understood the message and has located the endpoint, but the contents of the request body are semantically invalid. In case a client adds a new sensor with a room Id that is not found, the endpoint /api/v1/sensors are fully valid and has been located correctly. It is not the URL that is the problem but the data within the payload, the referenced room no longer exists in the system. Thus 422 is more precise as it conveys the idea that the request structure was successful, but the business logic checks failed.

Question 02
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
It is a severe security weakness when Java stack traces are exposed to the external users. A stack trace shows introspective internal data such as names of classes and packages that have been used in the application, exact library versions, and their paths, precise line numbers of errors, and internal logic flow of the application. This information may be used to determine the vulnerabilities that are known to exist versions of the libraries and how the internal system is organized, enabling an attacker to create specific attacks. In this project, the GlobalExceptionMapper is used to avoid this, by trapping all errors that are not expected and only responding with a generic 500 Internal Server Error message, and the internal errors do not get disclosed to the client.
		
Question 03
Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info () statements inside every single re source method?
Logging with JAX-RS filters is much better than simply adding Logger.info () calls within each resource method due to a number of reasons. To start with, filters are automated on each request and response without any additional business logic code. Second, it adheres to the principle of separation of concerns - logging is a cross-cutting concern that should not be treated as part of the real business logic of each endpoint. Third, in case the logging requirements evolve in the future, then only the single filter class will need to be adjusted instead of adjusting all of the resource methods of the entire API. This simplifies the codebase, makes it easier to maintain, and reduces the likelihood of errors.





