# Shopizer — Tech Onboarding & Architecture Overview

> Version: **3.2.5** | Stack: **Java 11 · Spring Boot 2.5.12 · Maven**

---

## Table of Contents

1. [What is Shopizer?](#1-what-is-shopizer)
2. [System Overview](#2-system-overview)
3. [Repository Structure](#3-repository-structure)
4. [Module Architecture](#4-module-architecture)
5. [Request Flow](#5-request-flow)
6. [Domain Model](#6-domain-model)
7. [Security & Auth](#7-security--auth)
8. [API Surface](#8-api-surface)
9. [Database & Profiles](#9-database--profiles)
10. [Key Configuration Files](#10-key-configuration-files)
11. [Local Dev Setup](#11-local-dev-setup)
12. [Docker Setup](#12-docker-setup)
13. [Adding New Features](#13-adding-new-features)
14. [Gotchas & Tips](#14-gotchas--tips)

---

## 1. What is Shopizer?

Shopizer is an open-source **headless e-commerce platform**. The backend exposes a REST API; the frontends are separate repos.

| Component | Tech | Repo |
|---|---|---|
| Backend API | Java / Spring Boot | `shopizer/` |
| Admin Panel | Angular | `shopizer-admin/` |
| Storefront | React | `shopizer-shop-reactjs/` |

---

## 2. System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Clients                              │
│                                                             │
│   ┌──────────────┐   ┌──────────────┐   ┌───────────────┐  │
│   │  React Shop  │   │ Angular Admin│   │  3rd-party /  │  │
│   │  (port 80)   │   │  (port 82)   │   │  Mobile App   │  │
│   └──────┬───────┘   └──────┬───────┘   └───────┬───────┘  │
└──────────┼─────────────────┼───────────────────┼───────────┘
           │                 │                   │
           └─────────────────▼───────────────────┘
                             │  REST / JSON
                    ┌────────▼────────┐
                    │  Shopizer API   │
                    │  Spring Boot    │
                    │  (port 8080)    │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
       ┌──────▼──────┐ ┌─────▼──────┐ ┌────▼──────┐
       │  MySQL /    │ │Elasticsearch│ │  AWS S3 / │
       │  H2 (dev)   │ │ (optional) │ │  GCS /    │
       └─────────────┘ └────────────┘ │  Local FS │
                                      └───────────┘
```

---

## 3. Repository Structure

```
Documents/
├── shopizer/                  ← Backend (this repo)
│   ├── sm-core-model/         ← JPA entities / domain model
│   ├── sm-core-modules/       ← Pluggable integrations (payment, shipping, search)
│   ├── sm-core/               ← Business services, DAOs, Spring context
│   ├── sm-shop-model/         ← REST DTOs (ReadableXxx / PersistableXxx)
│   ├── sm-shop/               ← Spring Boot app (controllers, security, config)
│   ├── pom.xml                ← Root multi-module POM
│   └── docker-compose.yml     ← MySQL + backend stack
│
├── shopizer-admin/            ← Angular admin panel
└── shopizer-shop-reactjs/     ← React storefront
```

---

## 4. Module Architecture

### Dependency Graph

```
sm-core-model
      │
      ▼
sm-core-modules
      │
      ▼
sm-core
      │
      ▼
sm-shop-model
      │
      ▼
sm-shop  ◄── Spring Boot entry point
```

### What each module owns

```
┌─────────────────────────────────────────────────────────────────┐
│  sm-core-model                                                  │
│  ─────────────                                                  │
│  Pure JPA entities. No Spring. No business logic.               │
│  Product, Order, Customer, MerchantStore, Category, etc.        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  sm-core-modules                                                │
│  ───────────────                                                │
│  Pluggable integration adapters.                                │
│  Payment (Stripe, PayPal, Braintree)                            │
│  Shipping (Canada Post, flat rate)                              │
│  Search (Elasticsearch)                                         │
│  Storage (AWS S3, GCS, local)                                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  sm-core                                                        │
│  ────────                                                       │
│  Spring @Service beans. Business logic. JPA repositories.       │
│  ProductService, OrderService, CustomerService, etc.            │
│  Email templates, Drools rules engine, Infinispan cache         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  sm-shop-model                                                  │
│  ─────────────                                                  │
│  REST API DTOs only.                                            │
│  PersistableProduct, ReadableProduct, ReadableOrder, etc.       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  sm-shop                                                        │
│  ────────                                                       │
│  Spring Boot app. REST controllers, facades, security, config.  │
│  store/api/v1, store/api/v2, store/facade, store/security       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Request Flow

```
HTTP Request
     │
     ▼
┌────────────────────────────┐
│  Spring Security Filter    │  ← JWT validation
│  AuthenticationTokenFilter │    /private/** → admin JWT
│                            │    /auth/**    → customer JWT
└────────────┬───────────────┘
             │
             ▼
┌────────────────────────────┐
│  REST Controller           │  ← store/api/v1/ or store/api/v2/
│  e.g. ProductApi.java      │    @RestController, @RequestMapping
└────────────┬───────────────┘
             │
             ▼
┌────────────────────────────┐
│  Facade                    │  ← Orchestration layer
│  e.g. ProductFacade        │    Converts DTO ↔ entity
│                            │    Calls multiple services
└────────────┬───────────────┘
             │
             ▼
┌────────────────────────────┐
│  Core Service              │  ← Business logic (sm-core)
│  e.g. ProductService       │    @Service, @Transactional
└────────────┬───────────────┘
             │
             ▼
┌────────────────────────────┐
│  JPA Repository            │  ← Data access
│  e.g. ProductRepository    │    Spring Data / Hibernate
└────────────┬───────────────┘
             │
             ▼
         Database
       (H2 / MySQL)
```

### DTO Conversion Pattern

```
Incoming JSON
     │
     ▼  (Jackson deserialization)
PersistableXxx  ──[Populator / MapStruct Mapper]──►  Entity
                                                        │
                                                        ▼
                                                    Database

Database
     │
     ▼
Entity  ──[Populator / MapStruct Mapper]──►  ReadableXxx
                                                  │
                                                  ▼  (Jackson serialization)
                                             Outgoing JSON
```

---

## 6. Domain Model

### Core Aggregate: Product

```
Product
├── ProductDescription[]       (i18n: name, description, SEO url)
├── ProductAvailability[]      (stock qty, price, region)
│   └── ProductPrice[]         (regular + special price with dates)
├── ProductImage[]             (image file references)
├── ProductAttribute[]         (links to ProductOptionValue)
│   └── ProductOption          (e.g. "Color", "Size")
│       └── ProductOptionValue (e.g. "Red", "XL")
├── ProductRelationship[]      (related / featured products)
├── ProductReview[]            (customer reviews + ratings)
├── ProductType                (GENERAL, DIGITAL, etc.)
├── Manufacturer               (brand)
├── Category[]                 (hierarchical tree)
└── TaxClass
```

### Core Aggregate: Order

```
Order
├── OrderProduct[]             (line items)
│   ├── OrderProductAttribute  (selected options snapshot)
│   ├── OrderProductPrice      (price snapshot at time of order)
│   └── OrderProductDownload   (for digital products)
├── OrderTotal[]               (subtotal, tax, shipping, discount, grand total)
├── OrderStatusHistory[]       (status change audit log)
├── OrderAttribute[]           (custom key-value metadata)
├── CreditCard                 (masked card info)
└── Transaction[]              (AUTHORIZE, CAPTURE, REFUND, etc.)
```

### Core Aggregate: Customer

```
Customer
├── Billing                    (embedded billing address)
├── Delivery                   (embedded delivery address)
├── CustomerAttribute[]        (custom profile fields)
├── CustomerReview[]           (reviews written by this customer)
└── CustomerOptin[]            (newsletter subscriptions)
```

### Shopping Cart

```
ShoppingCart  (anonymous by code, or linked to Customer)
└── ShoppingCartItem[]
    └── ShoppingCartAttributeItem[]  (selected options)
```

### Store / Merchant

```
MerchantStore
├── MerchantConfig[]           (key-value store settings)
├── MerchantConfiguration[]    (typed: payment, shipping configs)
└── MerchantLog[]              (audit log)
```

---

## 7. Security & Auth

### Two Security Realms

```
┌──────────────────────────────────────────────────────────┐
│  ADMIN realm                                             │
│  URL pattern: /api/v1/private/**                         │
│                                                          │
│  Login:  POST /api/v1/private/login                      │
│  Body:   { "username": "admin@shopizer.com",             │
│            "password": "password" }                      │
│  Returns: { "token": "Bearer <jwt>" }                    │
│  Header:  Authorization: Bearer <token>                  │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  CUSTOMER realm                                          │
│  URL pattern: /api/v1/auth/**                            │
│                                                          │
│  Login:  POST /api/v1/customer/login                     │
│  Body:   { "username": "customer@email.com",             │
│            "password": "password" }                      │
│  Returns: { "token": "Bearer <jwt>" }                    │
└──────────────────────────────────────────────────────────┘

Public (no auth): /api/v1/** (non-private, non-auth paths)
```

### Key Security Classes

| Class | Location | Purpose |
|---|---|---|
| `MultipleEntryPointsSecurityConfig` | `store/security/` | Main Spring Security config |
| `AuthenticationTokenFilter` | `store/security/` | JWT validation filter |
| `JWTTokenUtil` | `store/security/` | JWT create/validate |

### Default Credentials (seeded on first run)

```
Admin:    admin@shopizer.com / password
```

---

## 8. API Surface

### URL Versioning

```
/services/**        ← v0 legacy (minimal, avoid)
/api/v1/**          ← v1 main API (most endpoints)
/api/v2/**          ← v2 newer product variant endpoints
```

### Key Endpoint Groups

| Group | Base Path | Auth |
|---|---|---|
| Auth | `/api/v1/private/login`, `/api/v1/customer/login` | Public |
| Products | `/api/v1/product`, `/api/v2/product` | Public (read) / Admin (write) |
| Categories | `/api/v1/category` | Public (read) / Admin (write) |
| Cart | `/api/v1/cart` | Public |
| Orders | `/api/v1/cart/{code}/order` | Public / Customer |
| Customers | `/api/v1/private/customer`, `/api/v1/auth/customer` | Admin / Customer |
| Shipping | `/api/v1/private/shipping` | Admin |
| Payment | `/api/v1/private/modules/payment` | Admin |
| Tax | `/api/v1/private/tax` | Admin |
| Content (CMS) | `/api/v1/content` | Public (read) / Admin (write) |
| Search | `/api/v1/search` | Public |
| References | `/api/v1/languages`, `/api/v1/country`, `/api/v1/currency` | Public |

> Full endpoint list: **http://localhost:8080/swagger-ui.html**

### Multi-store & i18n Query Params

Most endpoints accept:
- `?store=DEFAULT` — target store (defaults to `DEFAULT`)
- `?lang=en` — language for i18n descriptions

---

## 9. Database & Profiles

### Default: H2 (embedded, zero config)

```
Schema:   SALESMANAGER
File:     sm-shop/SALESMANAGER.h2.db
DDL:      hibernate.hbm2ddl.auto=update  (auto-creates tables)
Seeding:  InitializationLoader runs on startup
```

### Spring Profiles → Database Config

```
Profile       Config file location
──────────    ──────────────────────────────────────────────────
(default)     sm-shop/src/main/resources/database.properties  ← H2
local         profiles/local/database.properties              ← MySQL local
mysql         profiles/mysql/database.properties              ← MySQL generic
docker        profiles/docker/database.properties             ← MySQL in Docker
gcp           profiles/gcp/database.properties                ← Cloud SQL
cloud         profiles/cloud/database.properties              ← Generic cloud
```

### Switching to MySQL

```sql
-- 1. Create DB
CREATE DATABASE SALESMANAGER;
CREATE USER shopizer IDENTIFIED BY 'your-password';
GRANT ALL ON SALESMANAGER.* TO shopizer;
```

```properties
# 2. Edit profiles/local/database.properties
db.jdbcUrl=jdbc:mysql://127.0.0.1:3306/SALESMANAGER?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
db.user=shopizer
db.password=your-password
db.driverClass=com.mysql.cj.jdbc.Driver
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.hbm2ddl.auto=update
```

```bash
# 3. Run with profile
cd sm-shop && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 10. Key Configuration Files

```
sm-shop/src/main/resources/
├── application.properties          ← server port, multipart limits, actuator
├── shopizer-properties.properties  ← feature flags, image sizes, search
├── vault.properties                ← secrets placeholder
└── profiles/
    ├── local/database.properties   ← MySQL local
    ├── mysql/database.properties   ← MySQL generic
    ├── docker/database.properties  ← Docker MySQL
    └── gcp/database.properties     ← GCP Cloud SQL

sm-core/src/main/resources/
├── spring/                         ← Spring XML context files
├── templates/email/                ← Email templates (Freemarker)
├── rules/                          ← Drools decision tables
└── reference/                      ← Countries, zones, currencies seed data
```

### Notable `shopizer-properties.properties` flags

| Flag | Default | Effect |
|---|---|---|
| `POPULATE_TEST_DATA` | `false` | Seed demo products on startup |
| `INDEX_PRODUCTS` | `true` | Enable Elasticsearch indexing |
| `CROP_UPLOADED_IMAGES` | `false` | Auto-crop product images |
| `ORDER_EMAIL_API` | `true` | Send order confirmation emails |
| `VALIDATE_CREDIT_CARD` | `false` | Enable card validation |

---

## 11. Local Dev Setup

### Prerequisites

- Java 11 or 17
- Maven 3.6+ (or use `./mvnw` wrapper)
- Git

### Steps

```bash
# 1. Clone
git clone https://github.com/shopizer-ecommerce/shopizer.git
cd shopizer

# 2. Build all modules
./mvnw clean install -DskipTests

# 3. Run (H2 embedded, no DB setup needed)
cd sm-shop && ../mvnw spring-boot:run
```

### Verify

| URL | What |
|---|---|
| `http://localhost:8080/swagger-ui.html` | API explorer |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/api/v1/products?store=DEFAULT&lang=en` | Products list |

### Quick API test

```bash
# Admin login
curl -X POST http://localhost:8080/api/v1/private/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@shopizer.com","password":"password"}'

# Get products (public)
curl "http://localhost:8080/api/v1/products?store=DEFAULT&lang=en"
```

---

## 12. Docker Setup

### Full stack (MySQL + backend)

```bash
# From shopizer/ root
docker-compose up
```

```
# docker-compose.yml spins up:
shopizer-mysql   → port 3306  (MySQL 8.0)
shopizer-app     → port 8080  (Spring Boot, waits for MySQL healthy)
```

### Individual containers

```bash
# Backend only (H2)
docker run -p 8080:8080 shopizerecomm/shopizer:latest

# Admin panel
docker run -e "APP_BASE_URL=http://localhost:8080/api" \
           -p 82:80 shopizerecomm/shopizer-admin

# React shop
docker run -e "APP_MERCHANT=DEFAULT" \
           -e "APP_BASE_URL=http://localhost:8080" \
           -p 80:80 shopizerecomm/shopizer-shop-reactjs
```

### Full local stack ports

```
Port 80   → React storefront
Port 82   → Angular admin panel
Port 8080 → Spring Boot API
Port 3306 → MySQL
```

---

## 13. Adding New Features

Pattern for adding a new domain feature (e.g. "Wishlist"):

```
Step 1: sm-core-model
        └── Create entity: Wishlist extends SalesManagerEntity<Long, Wishlist>
            Add @Entity, @Table, JPA relationships

Step 2: sm-core
        ├── Repository: WishlistRepository extends SalesManagerRepository<Wishlist, Long>
        └── Service:    WishlistService (interface) + WishlistServiceImpl (@Service)

Step 3: sm-shop-model (or sm-shop)
        ├── PersistableWishlist  (request DTO)
        └── ReadableWishlist     (response DTO)

Step 4: sm-shop
        ├── Mapper/Populator: entity ↔ DTO conversion
        ├── Facade: WishlistFacade (interface) + WishlistFacadeImpl
        └── Controller: WishlistApi.java in store/api/v1/
                        @RestController @RequestMapping("/api/v1")
```

```bash
# Rebuild after changes
./mvnw clean install -DskipTests
cd sm-shop && ../mvnw spring-boot:run
```

---

## 14. Gotchas & Tips

| # | Topic | Detail |
|---|---|---|
| 1 | **Multi-store** | Most endpoints accept `?store=DEFAULT`. The `MerchantStoreArgumentResolver` resolves the store automatically. |
| 2 | **i18n** | Entities have a `*Description` companion for translated text. Use `?lang=en` to control language. |
| 3 | **Auth URL pattern** | `/private/**` = admin JWT, `/auth/**` = customer JWT, everything else = public. |
| 4 | **H2 console** | Not enabled by default. Add `spring.h2.console.enabled=true` + `spring.h2.console.path=/h2-console` to `application.properties`. |
| 5 | **Elasticsearch** | Optional. Set `INDEX_PRODUCTS=false` in `shopizer-properties.properties` to disable. |
| 6 | **CORS** | `CorsFilter.java` allows all origins by default — tighten for production. |
| 7 | **Bean overriding** | `spring.main.allow-bean-definition-overriding=true` — modules can override default implementations. |
| 8 | **Image storage** | Uploaded images go to `sm-shop/files/` by default. Configurable to S3 or GCS. |
| 9 | **Multipart limits** | Default: 4MB file / 10MB request. Increase in `application.properties` if needed. |
| 10 | **XSS** | `XssFilter.java` sanitizes all incoming requests. |
| 11 | **API docs** | Swagger UI at `http://localhost:8080/swagger-ui.html` is the fastest way to explore all endpoints. |
| 12 | **Default store code** | `DEFAULT` — used in all seed data and most examples. |

---

*Generated: 2026-03-24*
