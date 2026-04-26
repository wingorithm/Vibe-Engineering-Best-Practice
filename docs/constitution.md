# Project Constitution
This document contains the unbreakable rules for this repository. Every agent, sub-agent, and developer must strictly adhere to these conventions.

## 1. Core Stack & Versions
* **Language:** Java 21.
* **Framework:** Spring Boot.
* **Infrastructure:** Docker for containerization; Postgres for main CRUD.

## 2. Project Structure
```text
wingorithm.ticketing.vibeengineering
├── Application.java                   # Main Spring Boot entry point
├── config/                            # Global configurations (Security, Redis, Feign, etc.)
│   └── FeignConfig.java
├── exception/                         # Global error handling
│   └── GlobalExceptionHandler.java    # @ControllerAdvice
├── common/                            # Shared utilities, base classes, and cross-cutting constants
│   └── utils/
│
├── customer/                          # FEATURE: e.g Customer Module
│   ├── controller/                    # REST APIs
│   │   └── CustomerController.java
│   │
│   ├── service/                       # Business Logic Interfaces and Implementations
│   │   ├── CustomerService.java
│   │   └── impl/
│   │       └── CustomerServiceImpl.java
│   │
│   ├── repository/                    # Data Access Layer
│   │   └── CustomerRepository.java
│   │
│   ├── model/                         # Domain Models, DTOs, and Mappers
│   │   ├── entity/                    # JPA Entities
│   │   │   └── CustomerEntity.java
│   │   ├── dto/                       # Request/Response objects
│   │   │   ├── CustomerRequest.java
│   │   │   └── CustomerResponse.java
│   │   └── mapper/                    # MapStruct or manual mapping interfaces
│   │       └── CustomerMapper.java
│   │
│   └── integration/                   # External communications (Feign Clients)
│       ├── LoyaltyProgramClient.java  # e.g. loyalty 3rd party API or client API to other service
│       └── dto/                       # DTOs strictly for the external API contracts
│           ├── LoyaltyCheckRequest.java
│           └── LoyaltyCheckResponse.java
│
└── event/                             # FEATURE: Event Module
    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    └── integration/
```

## 3. Code Style & Git Conventions
* **API docs:** each endpoint should have proper description and common expected status code that will enrich information in swagger-ui.
* **Commit Standard:** Use conventional commits (`feature :`, `fix : `, `refactor :`).

## 4. Database Conventions
* always add `created_at` and `updated_at` fields on master table