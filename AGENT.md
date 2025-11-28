# AGENT.md - AI Agent Prompting Guide for Vendora B2B E-Commerce System

This document provides comprehensive guidance for AI agents working with the Vendora B2B E-Commerce marketplace codebase. It contains essential context, conventions, and best practices to ensure consistent and high-quality contributions.

---

## 📋 Project Overview

**Vendora** is a B2B e-commerce marketplace platform connecting suppliers and retailers. It is built with **Spring Boot 3.5.6** and **Java 21**, following **Clean Architecture** and **Hexagonal Architecture** principles.

### Key Business Concepts
- **Suppliers**: Product providers who list and manage inventory
- **Retailers**: Business buyers with loyalty tiers (Bronze/Silver/Gold/Platinum)
- **Products & Variants**: Products are containers; variants are the actual sellable items
- **Orders**: Purchase transactions placed by retailers
- **Quotations**: RFQ (Request for Quote) system for bulk pricing negotiations
- **Inventory**: Stock management with reservation system

---

## 🏗️ Architecture & Layer Organization

The project follows **Clean Architecture** with strict layer separation:

```
src/main/java/com/example/ecommerce/marketplace/
├── domain/           # Core business logic (NO framework dependencies)
├── application/      # Use cases (business workflows)
├── infrastructure/   # JPA entities, repository implementations
├── web/              # REST controllers, DTOs, HTTP handling
├── config/           # Spring configuration
└── service/          # Additional services
```

### Layer Rules

| Layer | Allowed Dependencies | Purpose |
|-------|---------------------|---------|
| `domain/` | Pure Java only | Business entities, repository interfaces, domain logic |
| `application/` | Domain layer only | Use cases, commands, results |
| `infrastructure/` | Domain, Spring Data JPA | JPA entities, repository implementations |
| `web/` | Application, Domain, Spring Web | Controllers, DTOs, validation |
| `config/` | All layers, Spring | Dependency injection configuration |

### ⚠️ CRITICAL: Never import infrastructure or web classes into domain layer

---

## 📁 Key Directories & File Patterns

### Domain Layer (`domain/`)
- `{Entity}.java` - Domain entity with business logic
- `{Entity}Repository.java` - Repository interface (domain contract)
- `{Entity}Status.java` - Enum for entity states

### Application Layer (`application/`)
- `{Action}{Entity}UseCase.java` - Use case implementation
- `{Action}{Entity}Command.java` - Input DTO for use case
- `{Action}{Entity}Result.java` - Output DTO from use case

### Infrastructure Layer (`infrastructure/`)
- `{Entity}Entity.java` - JPA entity (database mapping)
- `Jpa{Entity}Repository.java` - Spring Data JPA interface
- `{Entity}RepositoryImpl.java` - Domain repository adapter

### Web Layer (`web/`)
- `controller/{Entity}Controller.java` - REST controller
- `model/{entity}/{Action}{Entity}Request.java` - HTTP request DTO
- `model/{entity}/{Entity}Response.java` - HTTP response DTO

---

## 🔧 Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Language (LTS) |
| Spring Boot | 3.5.6 | Application framework |
| Spring Data JPA | - | Database abstraction |
| Spring Security | - | Authentication & authorization |
| MySQL | 8.0 | Database |
| Gradle | 8.x | Build tool |
| Lombok | - | Boilerplate reduction |
| JUnit 5 | - | Testing framework |
| Mockito | - | Mocking framework |
| Springdoc OpenAPI | 2.8.5 | API documentation |
| JWT (jjwt) | 0.12.6 | Token-based auth |

---

## 📐 Coding Conventions

### Naming Conventions

```java
// Classes
ProductController          // Controllers
CreateProductUseCase       // Use cases
CreateProductCommand       // Commands (input DTOs)
CreateProductResult        // Results (output DTOs)
ProductEntity              // JPA entities
ProductRepositoryImpl      // Repository implementations

// Methods
public Product save(Product product)           // Repository methods
public CreateProductResult execute(...)        // Use case methods
public ResponseEntity<...> createProduct(...)  // Controller methods

// Variables
private final ProductRepository productRepository;  // Dependencies
```

### Use Case Pattern

Every business operation follows this pattern:

```java
@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    
    @Transactional
    public CreateProductResult execute(CreateProductCommand command) {
        // 1. Validate inputs
        // 2. Check business rules
        // 3. Perform operation
        // 4. Return result (success or failure)
    }
}
```

### Result Pattern

Use cases return Result objects, NOT exceptions for business errors:

```java
public class CreateProductResult {
    private boolean success;
    private Product product;
    private String errorMessage;
    private String errorCode;
    
    public static CreateProductResult success(Product product) { ... }
    public static CreateProductResult failure(String message, String code) { ... }
}
```

### Entity Mapping

Domain entities map to/from JPA entities:

```java
// In ProductEntity.java
public static ProductEntity fromDomain(Product product) { ... }
public Product toDomain() { ... }
```

---

## 🗃️ Domain Entities

### Core Entities and Their Key Fields

| Entity | Key Fields | Repository |
|--------|------------|------------|
| `Product` | id, sku, name, supplierId, basePrice, minimumOrderQuantity | `ProductRepository` |
| `ProductVariant` | id, productId, sku, color, size, priceAdjustment | `ProductVariantRepository` |
| `Inventory` | id, productVariantId, supplierId, availableQuantity, reservedQuantity | `InventoryRepository` |
| `Order` | id, orderNumber, retailerId, supplierId, status, totalAmount | `OrderRepository` |
| `OrderItem` | id, orderId, variantId, quantity, price | (embedded in Order) |
| `Supplier` | id, name, email, businessLicense, isVerified | `SupplierRepository` |
| `Retailer` | id, name, email, loyaltyTier, loyaltyPoints | `RetailerRepository` |
| `QuotationRequest` | id, requestNumber, retailerId, supplierId, status | `QuotationRepository` |
| `QuotationOffer` | id, offerNumber, quotationRequestId, totalAmount, status | `QuotationRepository` |

### Important Business Rules

1. **Products must have variants** - A product cannot be ordered directly; orders reference variants
2. **Inventory is per variant** - Stock is tracked at the variant level, not product level
3. **Orders reserve inventory** - When an order is placed, inventory is reserved
4. **Order status transitions** - PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
5. **Loyalty tiers** - Bronze (0pts) → Silver (1000pts) → Gold (5000pts) → Platinum (10000pts)

---

## 🔌 API Conventions

### URL Patterns
```
Base: /api/v1/{resource}
Create: POST /api/v1/products
Read:   GET /api/v1/products/{id}
Update: PUT /api/v1/products/{id}
Delete: DELETE /api/v1/products/{id}
Action: POST /api/v1/products/{id}/activate
```

### HTTP Status Codes
| Code | Usage |
|------|-------|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (resource created) |
| 400 | Validation errors, invalid input |
| 401 | Unauthorized (not authenticated) |
| 403 | Forbidden (authenticated but not authorized) |
| 404 | Resource not found |
| 409 | Conflict (e.g., duplicate SKU) |
| 500 | Internal server error |

### Error Response Format
```json
{
  "error": "PRODUCT_NOT_FOUND",
  "message": "Product with ID 123 not found",
  "timestamp": "2025-11-28T10:00:00Z"
}
```

---

## 🧪 Testing Conventions

### Test Structure
```
src/test/java/com/example/ecommerce/marketplace/
├── domain/           # Domain unit tests
├── application/      # Use case tests (with mocks)
├── infrastructure/   # Integration tests (real DB)
└── web/              # Controller tests
```

### Test Naming
```java
@Test
void execute_withValidCommand_returnsSuccessResult() { ... }

@Test
void execute_withInvalidSupplierId_returnsNotFoundError() { ... }

@Test
void execute_withDuplicateSku_returnsConflictError() { ... }
```

### Test Patterns

```java
// Unit test (use case)
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {
    @Mock private ProductRepository productRepository;
    @InjectMocks private CreateProductUseCase useCase;
    
    @Test
    void execute_withValidCommand_returnsSuccessResult() {
        // Given
        CreateProductCommand command = ...;
        when(productRepository.save(any())).thenReturn(product);
        
        // When
        CreateProductResult result = useCase.execute(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(productRepository).save(any());
    }
}

// Integration test (repository)
@SpringBootTest
@Transactional
class ProductRepositoryIntegrationTest {
    @Autowired private ProductRepository productRepository;
    
    @Test
    void save_withValidProduct_persistsToDatabase() { ... }
}
```

---

## 📝 Common Tasks & Patterns

### Adding a New Entity

1. Create domain entity in `domain/{entity}/`
2. Create repository interface in `domain/{entity}/`
3. Create JPA entity in `infrastructure/{entity}/`
4. Create JPA repository in `infrastructure/{entity}/`
5. Create repository implementation in `infrastructure/{entity}/`
6. Register in `config/UseCaseConfig.java` if needed

### Adding a New Use Case

1. Create command class: `application/{entity}/{Action}{Entity}Command.java`
2. Create result class: `application/{entity}/{Action}{Entity}Result.java`
3. Create use case class: `application/{entity}/{Action}{Entity}UseCase.java`
4. Add tests in `test/.../application/{entity}/`
5. Wire into controller

### Adding a New API Endpoint

1. Create request DTO in `web/model/{entity}/`
2. Create response DTO in `web/model/{entity}/`
3. Add method in controller
4. Add OpenAPI annotations (`@Operation`, `@ApiResponses`)
5. Add validation annotations (`@Valid`, `@NotNull`, etc.)

---

## ⚠️ Important Warnings

### DO NOT:
- ❌ Import JPA/Spring classes in domain layer
- ❌ Throw exceptions for business validation failures (use Result pattern)
- ❌ Put business logic in controllers
- ❌ Access repositories directly from controllers (use use cases)
- ❌ Create circular dependencies between layers

### ALWAYS:
- ✅ Follow the layer separation strictly
- ✅ Write tests for new use cases
- ✅ Use `@Transactional` for operations that modify multiple entities
- ✅ Validate input at the controller level AND in use cases
- ✅ Return proper HTTP status codes
- ✅ Document APIs with OpenAPI annotations

---

## 🔍 Quick Reference

### Build Commands
```bash
./gradlew build              # Full build
./gradlew bootRun            # Run application
./gradlew test               # Run all tests
./gradlew test --tests "..."  # Run specific test
./gradlew clean build -x test # Build without tests
```

### Docker Commands
```bash
docker-compose up -d         # Start MySQL
docker-compose down          # Stop MySQL
```

### URLs (when running locally)
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

---

## 📚 Additional Documentation

- `docs/entity_design.md` - Detailed entity specifications
- `docs/workflow_tables.md` - Business workflow documentation
- `docs/api/` - API specification and HTTP status codes

---

## 🎯 Prompting Tips for AI Agents

When working with this codebase:

1. **Specify the layer** when asking for changes (domain, application, infrastructure, web)
2. **Follow existing patterns** - Look at similar files for reference
3. **Consider all affected layers** - A new feature may require changes across multiple layers
4. **Include tests** - Every use case should have corresponding tests
5. **Respect the architecture** - Don't shortcut by putting logic in wrong layers
6. **Use proper error handling** - Result pattern for business errors, exceptions for unexpected errors
7. **Maintain transactional integrity** - Use `@Transactional` appropriately
8. **Document APIs** - Add OpenAPI annotations for all endpoints

---

*Last updated: November 2025*
