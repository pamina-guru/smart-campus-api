# Smart Campus Sensor & Room Management API

## Overview

This project is a **RESTful API** developed using **JAX-RS (Jersey)** and an **embedded HTTP server**. It simulates a **Smart Campus** system to manage:

- **Rooms**
- **Sensors**
- **Sensor Readings**

The API follows REST principles and includes:

- HATEOAS (Discovery endpoint)
- Sub-resource architecture
- Validation & constraints
- Exception mapping
- Logging filters

---

## API Design Overview

The API is designed using a **resource-based architecture**.

### Versioning

```
/api/v1
```

Ensures scalability and backward compatibility.

### Resource Structure

| Resource   | Endpoint                                  |
| ---------- | ----------------------------------------- |
| Rooms      | `/api/v1/rooms`                           |
| Sensors    | `/api/v1/sensors`                         |
| Readings   | `/api/v1/sensors/{sensorId}/readings`     |

### Design Patterns Used

- **Singleton DataStore** - shared in-memory storage
- **Sub-resource Locator** - for sensor readings
- **Exception Mapping** - clean error responses
- **Filters** - logging cross-cutting concerns

### HATEOAS

The root endpoint (`/api/v1`) provides links to all resources, allowing clients to **navigate dynamically**.

---

## How to Build & Run

### 1. Clone the repository

```bash
git clone https://github.com/pamina-guru/smart-campus-api.git
cd smart-campus-api
```

### 2. Build project

```bash
mvn clean install
```

### 3. Run server

```bash
mvn exec:java
```

### 4. Access API

```
http://localhost:8090/api/v1
```

---

## Sample cURL Commands (MANDATORY)

### Get API discovery

```bash
curl http://localhost:8090/api/v1
```

### Get all rooms

```bash
curl http://localhost:8090/api/v1/rooms
```

### Create a room

```bash
curl -X POST http://localhost:8090/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"BUS-201","name":"Business Lab","capacity":60}'
```

### Filter sensors

```bash
curl http://localhost:8090/api/v1/sensors?type=CO2
```

### Add sensor reading

```bash
curl -X POST http://localhost:8090/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{"value":25.5}'
```

### Delete room (error case)

```bash
curl -X DELETE http://localhost:8090/api/v1/rooms/LIB-301
```

---

## Error Handling Summary

| Code | Meaning                  |
| ---- | ------------------------ |
| 409  | Room has sensors         |
| 422  | Invalid room reference   |
| 403  | Sensor unavailable       |
| 500  | Internal server error    |

---

## Conceptual Report (Answers)

### Part 1 - Q1: JAX-RS Lifecycle

JAX-RS resource classes are **instantiated per request** by default, meaning a new instance is created for every incoming request.

This reduces concurrency issues because each request has its own object instance. However, shared data structures such as the in-memory **DataStore** must still be carefully managed to avoid **race conditions** and data inconsistency.

### Part 1 - Q2: HATEOAS

**HATEOAS** (Hypermedia as the Engine of Application State) allows clients to navigate the API using links provided in responses rather than relying on static documentation.

This improves flexibility, reduces coupling, and makes the API **self-descriptive**.

### Part 2 - Q1: IDs vs Full Objects

Returning only **IDs** reduces network bandwidth but requires additional API calls by the client. Returning **full objects** increases payload size but improves usability and reduces client-side complexity.

### Part 2 - Q2: DELETE Idempotency

`DELETE` is **idempotent** because repeating the same request does not change the outcome. The first request deletes the resource, and subsequent requests return `404` without additional side effects.

### Part 3 - Q1: @Consumes(JSON)

If a client sends data in a format other than JSON (e.g., XML or text), JAX-RS will return:

```
415 Unsupported Media Type
```

This ensures that the server only processes supported formats and maintains API consistency.

### Part 3 - Q2: QueryParam vs Path

Using **query parameters** is more suitable for filtering because they are optional and flexible. **Path parameters** are better for identifying specific resources, whereas query parameters are ideal for searching and filtering collections.

### Part 4 - Q1: Sub-Resource Locator Pattern

The **Sub-resource Locator** pattern improves modularity by delegating nested logic to separate classes. This avoids large, complex controllers and makes the system easier to maintain and extend.

### Part 4 - Q2: Data Consistency

When a new reading is added, the system updates both:

- the **readings history**
- the sensor's **currentValue**

This ensures consistency across the API.

### Part 5 - Q2: Why 422?

HTTP `422` is more accurate than `404` because the request is **syntactically correct** but contains **invalid data** (e.g., non-existing `roomId`).

### Part 5 - Q4: Stack Trace Risks

Exposing stack traces can reveal:

- file paths
- class names
- internal logic

This information can be exploited by attackers to identify **vulnerabilities**.

### Part 5 - Q5: Filters vs Manual Logging

**Filters** allow centralized logging for all requests and responses. This avoids repetitive code and ensures **consistency** across the application.

---

## Author

**Pamina Guruparan**
[GitHub Repository](https://github.com/pamina-guru/smart-campus-api)

---

## License

This project is developed for academic purposes. 
