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

## Conceptual Report

### Part 1: Service Architecture & Setup

#### Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

In JAX-RS, the resource classes follow a **per-request lifecycle** by default, meaning that a new instance of a resource class is created for each incoming HTTP request. Because each request runs on its own independent object instance, this approach greatly minimizes concurrency problems by preventing unwanted shared state between requests. However, this project uses a centralized in-memory data structure implemented by a singleton **DataStore** to keep shared application data such as rooms, sensors and readings. Consequently, the underlying data structures are shared, but the resource classes themselves are not. Because of this architectural choice, shared collections like lists and maps must be carefully managed to avoid race situations, inconsistent data and unintentional overwrites. Although full synchronization mechanisms are not implemented in this simplified system, the design still highlights the importance of thread-safe handling when dealing with shared resources in real world applications.

#### Q2: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

The use of **HATEOAS** (Hypermedia as the Engine of Application State) represents a key characteristic of advanced RESTful API design. In this project, the discovery endpoint provides links to available resources, allowing clients to dynamically navigate the API rather than relying on predefined documentation. This approach benefits client developers by making the API self-descriptive and adaptable, as clients can discover available actions at runtime. It reduces tight coupling between client and server implementations and ensures that changes to endpoints can be handled more gracefully without breaking client applications. Compared to static documentation, HATEOAS improves flexibility, scalability and long-term maintainability of the API.

---

### Part 2: Room Management

#### Q1: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

In sending data back out of an API, a design choice has to be made whether to send resource identifiers or full object representations. The **ID-only return** reduces network bandwidth and payload size, which can be useful in large-scale systems. This method, however, involves more client API calls to get the finer details thus raising client-side complexity and latency. Conversely, the **full room objects**, as in this project, offer all the information required in a single response, at the cost of a little heavier payload sizes. This trade-off is to our advantage in terms of client usability and efficiency.

#### Q2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

The `DELETE` operation in this implementation is **idempotent**, i.e. a series of identical DELETE operations have the same effect without any undesired side effects. The initial deletion of a room is a successful deletion of the room out of the system. In case of a repeat DELETE request, the system returns a `404 Not Found` status, which means that the resource is not there anymore. Notably, there are no additional deletions that follow the initial deletion, which meets the idempotency definition. This action will guarantee the clients reliable and safe interactions even in situations where the requests are accidentally iterated.

---

### Part 3: Sensor Operations & Linking

#### Q1: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation explicitly defines that the API only accepts JSON-formatted request bodies. If a client attempts to send data in a different format, such as `text/plain` or `application/xml`, the JAX-RS runtime will automatically reject the request and return a **415 Unsupported Media Type** response. This mechanism enforces strict adherence to the API contract and ensures that the server processes only valid and expected input formats. It also prevents potential parsing errors and inconsistencies that could arise from handling unsupported data formats.

#### Q2: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Filtering with query parameters, like `/api/v1/sensors?type=CO2` is more appropriate than filtering in the URL path. The query parameters are created to enable **optional filtering and searching** activities in which the clients can narrow down the results dynamically without changing the underlying resource structure. Path parameters, in contrast, are designed to identify individual resources and are less flexible to do filtering. The query parameters allow easy combination of multiple filters and remain clean and intuitive to the API, making it the choice of search and filtering features.

---

### Part 4: Deep Nesting with Sub-Resources

#### Q1: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

This project employs the **Sub-Resource Locator pattern** to deal with nested resources, which in this case are sensor readings. The system does not store all the logic in a single resource class, but instead leaves the work of dealing with nested paths to a special `SensorReadingResource` class. This will enhance the general architecture by ensuring that there is **separation of concerns** and minimization of complexity. It enables each resource group to specialize in a particular task, simplifying codebase management, extension and debugging. This trend in large-scale APIs is used to avoid the development of extremely complicated and hard-to-maintain controller classes.

To store the historical data, the API has a list of sensor readings. A new reading is appended to the reading history of a sensor when a POST request has been made. The system also changes the `currentValue` field of the appropriate sensor to the latest reading. This side effect provides continuity in relation to the historical data and real-time sensor values to enable the clients to obtain both past trends and present readings. This design illustrates the need to have a synchronized state among the related data entities.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### Q1: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

In this case, **HTTP 422 (Unprocessable Entity)** is viewed as a more accurate semantic statement than HTTP 404 since the request is syntactically correct and properly formatted, but the data in it is logically incorrect. In the scenario, in this project, a client trying to construct a sensor with a non-existing `roomId` will have a valid endpoint that is accessible and the JSON payload will be well-formed. But the request is not processed because of the violation of a business rule, namely, the presence of the mentioned room. The `404 Not Found` type of response is normally returned in situations where the resource or endpoint being requested is not there, which is not the case in this instance. Thus, HTTP 422 better conveys that the server is aware of the request, but cannot handle it because there are semantic errors in the data and it is therefore the more suitable response to validation errors.

#### Q2: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Publicizing internal Java stack traces to external API users poses a huge **security risk** as it may disclose confidential information concerning the system implementation. Stack traces frequently carry details like internal class names, package layouts, method names, file names, line numbers and framework or library usage details. The attacker can utilize this information to understand the architecture of the system and what the vulnerabilities are. As an illustration, the familiarity with a particular framework or version can enable attackers to take advantage of known vulnerabilities, whereas file paths and method structures can be used to know how to navigate the system internally. This puts more chance of targeted attacks like injection, unauthorized access or denial of service. The project mitigates these risks by applying a **global exception mapper**, which captures any unforeseen errors and sends a generic HTTP 500 response, without revealing any information about the system, so the system is not exposed to information leakage.

#### Q3: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Logging with JAX-RS filters is beneficial as it offers a **centralized and uniform way** of managing cross-cutting issues throughout the application. The filters can be used on request and response level, and all incoming HTTP requests and outgoing responses can be intercepted and logged in a consistent fashion without altering the individual resource methods. This will greatly decrease redundancy of the code and will also make sure that all endpoints have logging. By contrast, manually adding `Logger.info()` statements into every resource method results in redundant code, maintenance overhead, and are more likely to omit logs in certain endpoints. Also, filters are used to ensure the separation of concerns by ensuring that logging logic is kept apart from business logic, leading to cleaner, more modular, and maintainable code. This design is particularly beneficial in large scale use where scalability and consistency are important.

---

## Author

**Pamina Guruparan**
[GitHub Repository](https://github.com/pamina-guru/smart-campus-api)


