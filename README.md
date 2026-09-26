<div align="center">

# 🍕 Food Fiesta

**A geospatial, real-time food-delivery platform built on Spring Boot 3 and Java 21.**

Ordering, live driver tracking, event-driven routing, and AI-assisted delivery estimates — on a PostGIS + Redis + Kafka foundation.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-6db33f?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20%2B%20PostGIS-4169e1?logo=postgresql&logoColor=white)](https://postgis.net/)
[![Redis](https://img.shields.io/badge/Redis-Geo-dc382d?logo=redis&logoColor=white)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-3.7-231f20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**[Live Demo](https://food-fiesta-0sej.onrender.com/)** · **[API Docs (`/swagger-ui/index.html`)](https://food-fiesta-0sej.onrender.com/swagger-ui/index.html)**

</div>

---

## Overview

Food Fiesta started as a classic server-rendered food-ordering app and has been re-architected into an **enterprise-grade delivery logistics platform**. It keeps the Thymeleaf storefront for customers and admins, and layers a modern, distributed backend underneath:

- **Geospatial core** — restaurants, drivers, and delivery points are first-class PostGIS geometries (SRID 4326) with GiST indexes for fast proximity search.
- **Real-time tracking** — driver positions stream over STOMP WebSockets and are cached in Redis Geo for sub-millisecond nearest-driver lookups.
- **Event-driven routing** — checkout publishes to Kafka; a consumer asynchronously assigns the closest available driver instead of blocking the request.
- **Intelligent ETAs** — delivery time blends PostGIS distance, restaurant prep time, live road routing, and an optional Spring AI refinement.
- **Resilient by design** — external map calls sit behind a Resilience4j circuit breaker, and request/WebSocket handling runs on Java 21 virtual threads.

## Key features

| Area | Capability |
| --- | --- |
| 🗺️ **Geospatial** | `ST_DWithin` nearby-restaurant search ordered by true ground distance |
| 📡 **Real-time** | Live driver location broadcast + order status updates over STOMP |
| 🚦 **Order lifecycle** | Guarded state machine: `PLACED → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED` (or `CANCELLED`) |
| 🔀 **Routing** | Kafka `new-order-created` topic decouples checkout from driver assignment |
| ⏱️ **ETA** | PostGIS distance + prep time + road routing + optional Spring AI estimate |
| 🛡️ **Resilience** | Resilience4j circuit breaker with straight-line fallback; virtual threads |
| 🔐 **Security** | Session auth, BCrypt passwords, Google OAuth2 login |
| 🧰 **Schema** | Flyway-owned migrations; Hibernate runs in `validate` mode |

## Architecture

```text
                         ┌─────────────────────────────┐
                         │   Browser / Mobile Client   │
                         └──────────────┬──────────────┘
                    HTTP (MVC/REST)     │     STOMP over WebSocket
                         ┌──────────────▼──────────────┐
                         │      Spring Boot 3 App      │
                         │  (Java 21 virtual threads)  │
                         └──┬───────┬───────┬───────┬──┘
              ┌─────────────┘       │       │       └─────────────┐
              ▼                     ▼       ▼                     ▼
     ┌────────────────┐   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
     │ PostgreSQL +   │   │   Kafka      │  │  Redis Geo   │  │  Spring AI   │
     │ PostGIS        │   │ new-order-   │  │ drivers:geo  │  │  (OpenAI)    │
     │ (Flyway)       │   │ created      │  │              │  │  + OSRM API  │
     └────────────────┘   └──────────────┘  └──────────────┘  └──────────────┘
       durable state      async routing     live positions     ETA enrichment
```

### Order lifecycle (event-driven routing)

```text
Checkout ──▶ save Orders (PLACED) ──▶ publish OrderCreatedEvent ──▶ Kafka
                                                                      │
                     ┌───────────────────────────────────────────────┘
                     ▼
        OrderRoutingConsumer ──▶ Redis Geo radius search (nearest AVAILABLE driver)
                     │
                     ├─▶ driver  → ASSIGNED   (pessimistic row lock)
                     └─▶ order   → CONFIRMED  (broadcast on /topic/orders/{id})
```

### Tech stack

| Layer | Technology |
| --- | --- |
| Runtime | Java 21 (virtual threads), Spring Boot 3.4.2 |
| Web | Spring MVC, Thymeleaf, Spring WebSocket (STOMP + SockJS) |
| Persistence | PostgreSQL 16 + PostGIS 3.4, Spring Data JPA, Hibernate Spatial |
| Migrations | Flyway (single source of schema truth) |
| Cache / Geo | Redis 7 (Geo commands) |
| Messaging | Apache Kafka 3.7 (KRaft), Spring for Apache Kafka |
| AI / Routing | Spring AI (OpenAI), OSRM-compatible routing API |
| Resilience | Resilience4j circuit breaker |
| Security | Spring Security, OAuth2 (Google), BCrypt |
| Docs | springdoc-openapi (Swagger UI) |

## Screenshots

<table>
  <tr>
    <td align="center"><img src="./screenshot/home.jpeg" width="360" alt="Home page"><br><sub>Home</sub></td>
    <td align="center"><img src="./screenshot/products.jpeg" width="360" alt="Menu"><br><sub>Menu</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="./screenshot/cart.jpeg" width="360" alt="Cart"><br><sub>Cart</sub></td>
    <td align="center"><img src="./screenshot/user-dashboard.jpeg" width="360" alt="Customer dashboard"><br><sub>Customer dashboard</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="./screenshot/admin-services.jpeg" width="360" alt="Admin console"><br><sub>Admin console</sub></td>
    <td align="center"><img src="./screenshot/swagger-ui-index-html.png" width="360" alt="Swagger UI"><br><sub>Swagger UI</sub></td>
  </tr>
</table>

## Getting started

### Prerequisites

- **JDK 21**
- **Docker** + **Docker Compose** (recommended — brings up PostGIS, Redis, and Kafka for you)
- An **OpenAI API key** (optional — only needed for AI ETA refinement)

> The Maven wrapper (`./mvnw` / `mvnw.cmd`) is included, so a local Maven install is not required.

### Run with Docker Compose (recommended)

The full stack — app, PostgreSQL/PostGIS, Redis, and Kafka — starts with one command:

```bash
docker compose up --build
```

Then open:

| URL | Purpose |
| --- | --- |
| http://localhost:8080 | Storefront |
| http://localhost:8080/swagger-ui/index.html | Interactive API docs |

Flyway automatically creates the PostGIS extension and the full baseline schema on first boot.

### Run against local services (no Docker)

If you already run PostgreSQL+PostGIS, Redis, and Kafka locally, point the app at them via environment variables and start it directly:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/foodfiesta"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="your_password"
export REDIS_HOST="localhost"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

./mvnw spring-boot:run
```

> **Note:** PostGIS is mandatory — a vanilla PostgreSQL install without the extension will fail the migration.

### Seeded demo data

On an empty database, `DataLoader` seeds sample data so the platform is usable immediately:

- **Admin** — `admin@foodfiesta.com` / `admin123`
- **Restaurants** — *Spice Route Kitchen* (MG Road) and *Golden Wok* (Indiranagar), Bengaluru
- **Drivers** — two `AVAILABLE` drivers with live positions
- **Menu** — 11 products distributed across the two restaurants

## API reference

Full, always-current documentation is served by Swagger UI at `/swagger-ui/index.html`. Highlights below.

### Geospatial search

```http
GET /restaurants/nearby?lat=12.9716&lon=77.5946&radius=5km
```

Active restaurants within `radius`, ordered by true ground distance. `radius` accepts `km`/`m` (e.g. `5km`, `800m`, or a bare number treated as km), defaults to `5km`, and is capped at `50km`.

```json
[
  {
    "id": 1,
    "name": "Spice Route Kitchen",
    "address": "MG Road, Bengaluru",
    "avgPrepMinutes": 25,
    "distanceMeters": 1284
  }
]
```

### Delivery ETA

```http
PUT /api/orders/{orderId}/delivery-location
Content-Type: application/json

{ "lon": 77.5946, "lat": 12.9716 }
```

```http
GET /estimate-delivery/{orderId}
```

```json
{
  "orderId": 42,
  "distanceMeters": 3120.5,
  "preparationMinutes": 25,
  "trafficDelayMinutes": 9,
  "deliveryMinutes": 34,
  "estimatedArrivalAt": "2026-09-25T10:15:00Z",
  "source": "spring-ai"
}
```

`source` is `spring-ai` when an OpenAI key is configured and the model responds, otherwise `heuristic`. Returns `404` for an unknown order and `409` until a delivery location is set.

### Driver location

```http
POST /api/drivers/{driverId}/location
Content-Type: application/json

{ "lon": 77.6150, "lat": 12.9760 }
```

Writes to Redis Geo, updates the durable Postgres snapshot, and fans out to WebSocket subscribers. Returns `202 Accepted`.

### Health

```http
GET /api/health
```

### Endpoint summary

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/restaurants/nearby` | PostGIS radius search for active restaurants |
| `PUT` | `/api/orders/{orderId}/delivery-location` | Set an order's delivery point |
| `GET` | `/estimate-delivery/{orderId}` | Compute the delivery ETA |
| `POST` | `/api/drivers/{driverId}/location` | Push a driver's live position (REST) |
| `GET` | `/api/health` | Liveness probe |
| `POST` | `/cart/checkout` | Place orders and publish routing events |

## Real-time (WebSocket / STOMP)

Connect to the SockJS endpoint at **`/ws`**, then subscribe to broker topics:

| Destination | Direction | Payload |
| --- | --- | --- |
| `/topic/drivers/{driverId}` | subscribe | live driver position |
| `/topic/orders/{orderId}` | subscribe | order status transitions |
| `/app/drivers/{driverId}/location` | send | `{ "lon": ..., "lat": ... }` |

```javascript
const socket = new SockJS("http://localhost:8080/ws");
const client = Stomp.over(socket);

client.connect({}, () => {
  // Track a driver
  client.subscribe("/topic/drivers/1", (msg) => {
    const { lon, lat, at } = JSON.parse(msg.body);
    console.log("Driver 1 moved to", lon, lat, "at", at);
  });

  // Track an order's lifecycle
  client.subscribe("/topic/orders/42", (msg) => {
    console.log("Order 42 is now", msg.body); // e.g. CONFIRMED
  });

  // Stream this driver's position (alternative to the REST endpoint)
  client.send("/app/drivers/1/location", {}, JSON.stringify({ lon: 77.615, lat: 12.976 }));
});
```

## Configuration

All settings are externalized through environment variables (see `application.properties`).

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL/PostGIS JDBC URL | `jdbc:postgresql://localhost:5432/foodfiesta` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `REDIS_HOST` / `REDIS_PORT` | Redis Geo server | `localhost` / `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap server | `localhost:9092` |
| `MAP_ROUTING_BASE_URL` | OSRM-compatible routing API | `https://router.project-osrm.org` |
| `OPENAI_API_KEY` | Enables Spring AI ETA refinement | unset |
| `OPENAI_CHAT_MODEL` | OpenAI chat model | `gpt-4o-mini` |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 login | unset |

> Keep secrets in a git-ignored `.env` file or your platform's secret manager — never commit them.

## Project structure

```text
src/main/java/com/example/demo
├── config/         Redis, Kafka topic, WebSocket, and data-seeding configuration
├── controllers/    MVC pages + REST (restaurants, orders, drivers, cart, admin)
├── dto/            Request/response records (LocationUpdate, DeliveryEstimate, …)
├── entities/       JPA entities (Orders, Restaurant, Driver, Product, User, …)
├── events/         Kafka event payloads (OrderCreatedEvent)
├── repositories/   Spring Data JPA + Redis Geo repositories
└── services/       Order state machine, routing consumer, ETA, driver tracking

src/main/resources
├── application.properties
├── db/migration/   V1__baseline_and_geospatial.sql (Flyway)
└── templates/      Thymeleaf views
```

## Resilience & performance

- **Circuit breaker** — the external routing client runs behind a Resilience4j `mapRouting` breaker (sliding window 10, 50% failure threshold, 10s open wait, auto half-open). When the vendor is slow or down, the breaker opens and ETAs degrade gracefully to the straight-line PostGIS estimate rather than cascading failures.
- **Virtual threads** — `spring.threads.virtual.enabled=true` runs HTTP and WebSocket handling on Java 21 virtual threads, ideal for the many concurrent, I/O-bound tracking connections this workload produces.
- **Async routing** — driver assignment happens off the request thread via Kafka, keeping checkout latency flat under load.
- **Spatial indexing** — GiST indexes on restaurant, driver, and delivery geometries keep proximity queries fast as data grows.

## Testing

```bash
./mvnw test
```

The Spring context test requires a reachable PostgreSQL+PostGIS instance with the configured credentials; supply them (or run via Docker Compose) before executing the suite.

## Deployment

Deploy with a **PostGIS-enabled** PostgreSQL, Redis, and Kafka. Set the environment variables above on your platform and **override the local defaults with production secrets**. The included `docker-compose.yml` is intended for local development; for production, use managed backing services and a hardened broker configuration.

## License

Released under the [MIT License](LICENSE).

---

<div align="center">

Built with ☕ and 🍕 by **[imrajeevnayan](https://github.com/imrajeevnayan)**

</div>
