# Design Spec: Event Search API (Part 1)

**Goal:** Implement a high-performance, paginated search API for concert events that adheres to the Project Constitution's architectural and response standards.

## 1. Architectural Alignment
- **Module:** `event`
- **Package Structure:**
  - `wingorithm.ticketing.vibeengineering.event.controller`
  - `wingorithm.ticketing.vibeengineering.event.service`
  - `wingorithm.ticketing.vibeengineering.event.repository`
  - `wingorithm.ticketing.vibeengineering.event.model.entity`
  - `wingorithm.ticketing.vibeengineering.event.model.dto`
  - `wingorithm.ticketing.vibeengineering.event.model.mapper`
- **Base Class:** `EventEntity` must extend `wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity`.
- **Response Format:** All responses must use the standardized envelope from Section 5 of the Constitution.

## 2. Database Schema (PostgreSQL)
### Table: `vibeengineer.event`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PRIMARY KEY | Unique identifier |
| `name` | VARCHAR(100) | NOT NULL | Event name |
| `artist` | VARCHAR(100) | NOT NULL | Performer name |
| `location` | VARCHAR(100) | NOT NULL | Venue or city |
| `date_time` | TIMESTAMP | NOT NULL | Event schedule |
| `total_tickets` | INTEGER | NOT NULL | Capacity |
| `base_price` | DECIMAL(15,2) | NOT NULL | Price before tier discount |
| `created_at` | TIMESTAMP | NOT NULL | Handled by BaseEntity |
| `updated_at` | TIMESTAMP | NOT NULL | Handled by BaseEntity |

### Performance Optimization (UAC 2: <100ms)
- **`idx_event_search`**: Composite index on `(location, date_time)`.
- **`idx_event_artist`**: B-tree index on `artist`.

## 3. API Specification
### Endpoint: `GET /api/v1/events`
**Description:** Search for events by location, artist, or date with pagination.

**Query Parameters:**
- `location` (String, optional): Filter by venue/city.
- `artist` (String, optional): Filter by artist name.
- `date` (ISO-8601 Date, optional): Filter by event date.
- `page` (Integer, optional, default: 0): Page index.
- `size` (Integer, optional, default: 10): Page size.

**Standardized Response (Success):**
```json
{
  "errorSchema": {
    "errorCode": "0000",
    "message": "Success"
  },
  "outputSchema": {
    "content": [
      {
        "id": "...",
        "name": "Neon Nights",
        "artist": "Alice",
        "location": "Jakarta",
        "dateTime": "2026-05-01T20:00:00Z",
        "totalTickets": 500,
        "basePrice": 150000.00
      }
    ],
    "pageable": { ... },
    "totalPages": 1,
    "totalElements": 1,
    "last": true
  }
}
```

### API Documentation (Constitution Rule)
- Use `@Operation` and `@ApiResponses` to document endpoints for Swagger UI.
- Dependencies required: `springdoc-openapi-starter-webmvc-ui`.

## 4. Acceptance Criteria Verification
- **UAC 1 (Pagination):** Verify response contains `content` and pagination metadata.
- **UAC 2 (Performance):** Verify database indexes are created and used. (Benchmark: <100ms response time).
