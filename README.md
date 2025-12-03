# Vendora - B2B E-Commerce Marketplace System

A comprehensive B2B e-commerce platform connecting suppliers and retailers, built with Spring Boot and Clean Architecture principles.

## 🎯 Overview

Vendora is a marketplace system that facilitates business-to-business transactions between suppliers and retailers. It manages products, inventory, orders, quotations, and enables efficient bulk purchasing with tiered pricing and loyalty programs.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
├── domain/          # Business logic and entities (framework-agnostic)
├── application/     # Use cases and business workflows
├── infrastructure/  # JPA entities, repositories, database adapters
├── web/            # REST controllers, DTOs, HTTP layer
└── config/         # Spring configuration and dependency wiring
```

### Key Design Patterns
- **Hexagonal Architecture**: Domain logic isolated from frameworks
- **Repository Pattern**: Domain repositories with infrastructure adapters
- **Use Case Pattern**: Single-responsibility business operations
- **DTO Pattern**: Request/Response models separate from domain entities

## 🚀 Tech Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.5.6** - Application framework
- **Spring Data JPA** - Database abstraction
- **MySQL 8.0** - Relational database
- **Gradle** - Build automation
- **Lombok** - Boilerplate reduction
- **JUnit 5** - Testing framework
- **Springdoc OpenAPI** - API documentation

### AI Service (Python)
- **FastAPI** - Modern async web framework
- **Qdrant** - Vector database for semantic search
- **Sentence Transformers** - Embedding generation (all-MiniLM-L6-v2)
- **OpenAI/Gemini** - LLM for chat and recommendations

## 📦 Domain Models

### Core Entities
- **Supplier** - Product providers with ratings and verification
- **Retailer** - Buyers with loyalty tiers (Bronze/Silver/Gold/Platinum)
- **Product** - Items with SKU, pricing tiers, and variants
- **Inventory** - Stock management with reorder points
- **Order** - Purchase transactions with multiple items
- **Quotation** - RFQ system for bulk pricing negotiations

## 🔌 API Endpoints

### Supplier Management
- `POST /api/v1/suppliers` - Register new supplier
- `GET /api/v1/suppliers/{id}` - Get supplier details
- `PUT /api/v1/suppliers/{id}` - Update supplier profile
- `DELETE /api/v1/suppliers/{id}` - Remove supplier

### Product Management
- `POST /api/v1/products` - Create product
- `GET /api/v1/products/{id}` - Get product by ID
- `GET /api/v1/products/sku/{sku}` - Get product by SKU
- `GET /api/v1/products/supplier/{id}` - List supplier's products
- `PUT /api/v1/products/{id}` - Update product
- `POST /api/v1/products/{id}/activate` - Activate product
- `POST /api/v1/products/{id}/deactivate` - Deactivate product
- `DELETE /api/v1/products/{id}` - Delete product

### Retailer Management
- `POST /api/v1/retailers` - Register retailer
- `GET /api/v1/retailers/{id}` - Get retailer details
- `PUT /api/v1/retailers/{id}` - Update retailer profile

### Order Management
- `POST /api/v1/orders` - Place order
- `GET /api/v1/orders/{id}` - Get order details
- `GET /api/v1/orders/retailer/{id}` - List retailer's orders
- `PUT /api/v1/orders/{id}/status` - Update order status

### Quotation System
- `POST /api/v1/quotations` - Create quotation request
- `POST /api/v1/quotations/{id}/offer` - Submit quotation offer
- `GET /api/v1/quotations/{id}` - Get quotation details

### AI-Powered Search
- `GET /api/v1/search/products` - Semantic product search (returns full Product objects with similarity scores)
- `GET /api/v1/search/suppliers` - Semantic supplier search (returns full Supplier objects with similarity scores)
- `GET /api/v1/search/combined` - Combined search (products + suppliers with scores)

### AI Recommendations
- `GET /api/v1/products/{id}/recommendations` - Get similar products
- `GET /api/v1/recommendations/homepage` - Homepage recommendations
- `GET /api/v1/recommendations/user` - Personalized user recommendations

### AI Chat
- `POST /api/v1/chat/sessions` - Create chat session
- `GET /api/v1/chat/sessions` - List user's chat sessions
- `GET /api/v1/chat/sessions/{id}/messages` - Get messages in session
- `POST /api/v1/chat/sessions/{id}/messages` - Ask question (AI-powered)

## 🗄️ Database Setup

### Using Docker Compose (Recommended)

```bash
# Start MySQL container
docker-compose up -d

# Stop container
docker-compose down
```

### Manual Setup

```sql
-- Create database
CREATE DATABASE vendora_db;

-- Create user
CREATE USER 'vendora_app'@'%' IDENTIFIED BY 'vendora_app_pass';
GRANT ALL PRIVILEGES ON vendora_db.* TO 'vendora_app'@'%';
FLUSH PRIVILEGES;
```

## 🏃 Running the Application

### Prerequisites
- Java 21 or higher
- MySQL 8.0
- Gradle 8.x (or use included wrapper)

### Steps

```bash
# 1. Start database (if using Docker)
docker-compose up -d

# 2. Run the application
./gradlew bootRun

# 3. Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "ProductControllerIntegrationTest"
```

### Test Coverage
- **Unit Tests**: Domain logic and use cases
- **Integration Tests**: Repository layer with MySQL test database
- **API Tests**: Full HTTP request/response testing with TestRestTemplate

**Total: 815+ tests** covering domain validation, repository operations, and API endpoints.

## 📝 Key Features

### 1. Tiered Pricing
Products support multiple price tiers based on order quantity with automatic discounts.

### 2. Product Variants
Support for product variations (size, color, etc.) with individual pricing adjustments.

### 3. Loyalty Program
Retailers earn points and achieve tiers (Bronze → Silver → Gold → Platinum) for benefits.

### 4. Inventory Management
- Real-time stock tracking
- Reorder point alerts
- Low stock notifications
- Status management (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)

### 5. Quotation System
Retailers can request custom quotes from suppliers for bulk purchases.

### 6. Order Tracking
Complete order lifecycle management with status updates (PENDING → CONFIRMED → SHIPPED → DELIVERED → CANCELLED).

### 7. AI-Powered Features
- **Semantic Search**: Search products and suppliers using natural language (powered by vector embeddings)
- **Smart Recommendations**: Get similar products, personalized recommendations, and homepage suggestions
- **AI Chat**: Conversational assistant for product queries and business inquiries (Agentic RAG)
- **Auto-Ingestion**: Products automatically indexed into vector database for AI features

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/example/ecommerce/marketplace/
│   │   ├── application/          # Use cases (business workflows)
│   │   │   ├── ai/               # AI ingestion use cases
│   │   │   ├── chat/             # Chat session use cases
│   │   │   ├── order/
│   │   │   ├── product/
│   │   │   ├── quotation/
│   │   │   ├── recommendation/   # AI recommendation use cases
│   │   │   ├── retailer/
│   │   │   ├── search/           # AI-powered search use cases (NEW)
│   │   │   └── supplier/
│   │   ├── config/               # Spring configuration
│   │   ├── domain/               # Core business logic (framework-agnostic)
│   │   │   ├── chat/             # Chat entities
│   │   │   ├── inventory/
│   │   │   ├── order/
│   │   │   ├── product/
│   │   │   ├── quotation/
│   │   │   ├── retailer/
│   │   │   └── supplier/
│   │   ├── infrastructure/       # Database & external adapters
│   │   │   ├── chat/
│   │   │   ├── inventory/
│   │   │   ├── order/
│   │   │   ├── product/
│   │   │   ├── quotation/
│   │   │   ├── retailer/
│   │   │   └── supplier/
│   │   ├── service/              # External service clients
│   │   │   └── ai/               # AI Service client (AiServiceClient.java)
│   │   └── web/                  # REST API layer
│   │       ├── common/           # Error handling, mappers
│   │       ├── controller/       # REST controllers (incl. SearchController)
│   │       └── model/            # Request/Response DTOs
│   │           ├── chat/
│   │           ├── search/       # Search response DTOs (NEW)
│   │           └── ...
│   └── resources/
│       └── application.properties
└── test/                         # Comprehensive test suite
```

## 🔧 Configuration

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/vendora_db
spring.datasource.username=vendora_app
spring.datasource.password=vendora_app_pass

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logging
logging.level.com.example.ecommerce=DEBUG
```

## 🛠️ Build & Package

```bash
# Build JAR
./gradlew build

# Skip tests
./gradlew build -x test

# Clean build
./gradlew clean build
```

## 📚 API Documentation

Interactive API documentation available at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🤝 Development Guidelines

### Code Organization
- **Domain Layer**: No framework dependencies, pure Java
- **Application Layer**: Use cases are framework-agnostic
- **Infrastructure Layer**: JPA entities, Spring Data repositories
- **Web Layer**: Controllers, DTOs, validation

### Testing Strategy
- Domain logic has comprehensive unit tests
- Repository layer has integration tests with MySQL
- API layer has end-to-end tests with TestRestTemplate
- All tests use isolated test database

## 📄 License

This project is developed for educational purposes as part of university coursework.

## 👥 Contributors

Vendora B2B E-Commerce System - University Third Year Project

---

**Project Status**: Active Development  
**Last Updated**: December 2025

