# Design: Role-Based Access Foundation

**Date:** 2026-04-26
**Status:** Approved

## Goal
Establish database connectivity to PostgreSQL and use Flyway to initialize the `vibeengineer` schema with core customer tiering tables and mock data, strictly adhering to the project constitution.

## Architecture & Data Model
- **Schema:** `vibeengineer`
- **Tables:** (Both tables include mandatory `created_at` and `updated_at` timestamps per constitution)
  - `vibeengineer.customer_tier`
    - `id`: SERIAL (Primary Key)
    - `name`: VARCHAR(50) (e.g., Beginner, Fans, Lovers)
    - `discount_percentage`: DECIMAL(5, 2)
    - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    - `updated_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  - `vibeengineer.customer`
    - `id`: UUID (Primary Key)
    - `name`: VARCHAR(100)
    - `email`: VARCHAR(150) (Unique)
    - `tier_id`: INTEGER (Foreign Key referencing `customer_tier.id`)
    - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    - `updated_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

## Migration Strategy (Flyway)
1. **Migration 1 (`V1__init_schema.sql`):**
   - Create schema `vibeengineer`.
   - Create `customer_tier` and `customer` tables within the schema, including timestamp fields.
2. **Migration 2 (`V2__seed_data.sql`):**
   - Seed `customer_tier` with: Beginner (0.00), Fans (10.00), Lovers (30.00).
   - Inject mock customer data for testing.

## Implementation Details
- **Spring Boot Dependencies:**
  - `spring-boot-starter-data-jpa`
  - `flyway-core`, `flyway-database-postgresql`
- **Configuration:**
  - Set `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`
  - Configure Flyway to target the `vibeengineer` schema.
- **Entities & Structure (per constitution):**
  - Enable JPA Auditing to automatically populate `created_at` and `updated_at`.
  - Create `BaseEntity` mapped superclass in `wingorithm.ticketing.vibeengineering.common.model.entity`.
  - Create `CustomerTierEntity` and `CustomerEntity` in `wingorithm.ticketing.vibeengineering.customer.model.entity`.

## Testing & Validation
- Verify Flyway migrations run successfully.
- Verify timestamp fields are populated correctly via JPA.
