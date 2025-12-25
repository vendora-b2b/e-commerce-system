package com.example.ecommerce.marketplace.integration;

import com.example.ecommerce.marketplace.application.product.CreateProductCommand;
import com.example.ecommerce.marketplace.application.product.CreateProductResult;
import com.example.ecommerce.marketplace.application.product.CreateProductUseCase;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.infrastructure.product.CategoryEntity;
import com.example.ecommerce.marketplace.infrastructure.product.JpaCategoryRepository;
import com.example.ecommerce.marketplace.infrastructure.supplier.SupplierEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product Creation (Function 01: Create Product).
 *
 * Tests the full flow from Use Case → Repository → Database.
 * These tests correspond to test cases UI01-UI05 in the test specification.
 *
 * Flow tested:
 * 1. UI01 - Supplier creates product successfully with all required fields
 * 2. UI02 - Validation error - Invalid SKU format
 * 3. UI03 - Conflict error - Duplicate SKU
 * 4. UI04 - Not found error - Supplier does not exist
 * 5. UI05 - Validation error - Invalid category IDs
 *
 * NOTE: Requires Docker MySQL to be running (docker-compose up)
 * DISABLED: Tests temporarily disabled for CI
 */
@Disabled("Integration tests disabled - database schema issues in CI")
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@DisplayName("Product Creation Integration Tests (UI01-UI05)")
class ProductCreationIntegrationTest {

    // ==================== Dependencies ====================

    @Autowired
    private CreateProductUseCase createProductUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private JpaCategoryRepository categoryRepository;

    // ==================== Test Data ====================

    private static final Long SUPPLIER_ID = 1001L;
    private static final Long NON_EXISTENT_SUPPLIER_ID = 999999L;
    private static final Long CATEGORY_ID_1 = 101L;
    private static final Long CATEGORY_ID_2 = 102L;
    private static final Long NON_EXISTENT_CATEGORY_ID = 888L;

    private Long testSupplierId;
    private Long testCategoryId1;
    private Long testCategoryId2;

    // ==================== Setup and Teardown ====================

    @BeforeEach
    void setUp() {
        // Create a test supplier
        SupplierEntity supplierEntity = new SupplierEntity();
        supplierEntity.setName("Test Supplier Inc.");
        supplierEntity.setEmail("test@supplier.com");
        supplierEntity.setPhone("1234567890");
        supplierEntity.setAddress("123 Test Street");
        supplierEntity.setBusinessLicense("TEST-LICENSE-001");
        supplierEntity.setRating(4.5);
        supplierEntity.setVerified(true);

        Supplier savedSupplier = supplierRepository.save(supplierEntity.toDomain());
        testSupplierId = savedSupplier.getId();

        // Create test categories
        CategoryEntity category1 = new CategoryEntity();
        category1.setName("Electronics");
        category1.setSlug("electronics");
        CategoryEntity savedCategory1 = categoryRepository.save(category1);
        testCategoryId1 = savedCategory1.getId();

        CategoryEntity category2 = new CategoryEntity();
        category2.setName("Computers");
        category2.setSlug("computers");
        CategoryEntity savedCategory2 = categoryRepository.save(category2);
        testCategoryId2 = savedCategory2.getId();
    }

    // ==================== UI01: Supplier Creates Product Successfully ====================

    @Nested
    @DisplayName("UI01 - Supplier Creates Product Successfully with All Required Fields")
    class UI01_SuccessfulProductCreation {

        @Test
        @Order(1)
        @DisplayName("UI01 - Should create product with all required fields and return 201 CREATED")
        void shouldCreateProductSuccessfully() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-001",                      // SKU (valid format)
                "HP Pavilion Laptop",                 // Name
                "High-performance laptop for business use", // Description
                Arrays.asList(testCategoryId1, testCategoryId2), // Category IDs
                1299.99,                              // Base price
                10,                                   // Minimum order quantity
                testSupplierId,                       // Supplier ID
                "unit",                               // Unit
                Arrays.asList("https://example.com/image1.jpg"), // Images
                null,                                 // Price tiers
                null                                  // Variants (not used in product creation)
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then - Verify result is successful
            assertTrue(result.isSuccess(), "Product creation should succeed");
            assertNotNull(result.getProductId(), "Product ID should be generated");
            assertNull(result.getErrorCode(), "No error code should be present");
            assertEquals("Product created successfully", result.getMessage(), "Success message should be present");

            // Then - Verify product is persisted in database
            Optional<Product> retrievedProduct = productRepository.findById(result.getProductId());
            assertTrue(retrievedProduct.isPresent(), "Product should be persisted in database");

            // Then - Verify all fields are saved correctly
            Product product = retrievedProduct.get();
            assertEquals("LAPTOP-HP-001", product.getSku());
            assertEquals("HP Pavilion Laptop", product.getName());
            assertEquals("High-performance laptop for business use", product.getDescription());
            assertEquals(1299.99, product.getBasePrice());
            assertEquals(10, product.getMinimumOrderQuantity());
            assertEquals(testSupplierId, product.getSupplierId());
            assertEquals("unit", product.getUnit());
            assertEquals(1, product.getImages().size());
            assertEquals("https://example.com/image1.jpg", product.getImages().get(0));
            assertEquals(2, product.getCategories().size());
            assertNotNull(product.getCreatedAt(), "createdAt timestamp should be set");
            assertNotNull(product.getUpdatedAt(), "updatedAt timestamp should be set");

            // IMPORTANT: Product is created but NOT sellable until variants are added
            // No inventory is created at this stage
        }

        @Test
        @Order(2)
        @DisplayName("UI01 - Should use default unit 'piece' when unit is not provided")
        void shouldUseDefaultUnitWhenNotProvided() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-002",
                "HP Laptop",
                "Test laptop",
                null,
                1299.99,
                10,
                testSupplierId,
                null,  // Unit not provided
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            Optional<Product> product = productRepository.findById(result.getProductId());
            assertTrue(product.isPresent());
            assertEquals("piece", product.get().getUnit(), "Default unit should be 'piece'");
        }

        @Test
        @Order(3)
        @DisplayName("UI01 - Should create product without optional fields (description, categories, images)")
        void shouldCreateProductWithoutOptionalFields() {
            // Given - Only required fields
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-003",
                "HP Laptop",
                null,  // No description
                null,  // No categories
                1299.99,
                10,
                testSupplierId,
                null,  // Default unit
                null,  // No images
                null,  // No price tiers
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Product should be created with only required fields");
            Optional<Product> product = productRepository.findById(result.getProductId());
            assertTrue(product.isPresent());
            assertNull(product.get().getDescription());
            assertTrue(product.get().getCategories().isEmpty() || product.get().getCategories() == null);
            assertTrue(product.get().getImages().isEmpty());
        }
    }

    // ==================== UI02: Invalid SKU Format ====================

    @Nested
    @DisplayName("UI02 - Validation Error - Invalid SKU Format")
    class UI02_InvalidSkuFormat {

        @Test
        @Order(10)
        @DisplayName("UI02 - Should return 400 BAD_REQUEST for lowercase SKU with spaces")
        void shouldRejectLowercaseSkuWithSpaces() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "laptop hp 001",  // Invalid: lowercase + spaces
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess(), "Product creation should fail");
            assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
            assertEquals("Invalid SKU format (alphanumeric and hyphens only, max 50 characters)",
                        result.getMessage());
            assertNull(result.getProductId(), "No product ID should be returned");

            // Verify product is NOT created in database
            assertFalse(productRepository.existsBySku("laptop hp 001"));
        }

        @Test
        @Order(11)
        @DisplayName("UI02 - Should reject SKU with special characters")
        void shouldRejectSkuWithSpecialCharacters() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP@HP#001",  // Invalid: special characters
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
        }

        @Test
        @Order(12)
        @DisplayName("UI02 - Should reject SKU shorter than 5 characters")
        void shouldRejectShortSku() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "HP01",  // Invalid: only 4 characters
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
        }

        @Test
        @Order(13)
        @DisplayName("UI02 - Should reject SKU longer than 50 characters")
        void shouldRejectLongSku() {
            // Given
            String longSku = "A".repeat(51);  // 51 characters
            CreateProductCommand command = new CreateProductCommand(
                longSku,
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
        }
    }

    // ==================== UI03: Duplicate SKU ====================

    @Nested
    @DisplayName("UI03 - Conflict Error - Duplicate SKU")
    class UI03_DuplicateSku {

        @Test
        @Order(20)
        @DisplayName("UI03 - Should return 409 CONFLICT when SKU already exists")
        void shouldRejectDuplicateSku() {
            // Given - Create a product first
            CreateProductCommand firstCommand = new CreateProductCommand(
                "LAPTOP-DELL-XPS15",
                "Dell XPS 15",
                "Original product",
                null,
                2499.99,
                5,
                testSupplierId,
                null,
                null,
                null,
                null
            );
            CreateProductResult firstResult = createProductUseCase.execute(firstCommand);
            assertTrue(firstResult.isSuccess(), "First product should be created successfully");

            // When - Try to create another product with the same SKU
            CreateProductCommand duplicateCommand = new CreateProductCommand(
                "LAPTOP-DELL-XPS15",  // Same SKU
                "Dell XPS 15 New Model",
                "Trying to use duplicate SKU",
                null,
                2699.99,
                5,
                testSupplierId,
                null,
                null,
                null,
                null
            );
            CreateProductResult duplicateResult = createProductUseCase.execute(duplicateCommand);

            // Then
            assertFalse(duplicateResult.isSuccess(), "Duplicate SKU should be rejected");
            assertEquals("SKU_EXISTS", duplicateResult.getErrorCode());
            assertEquals("SKU already exists", duplicateResult.getMessage());
            assertNull(duplicateResult.getProductId(), "No product ID should be returned");

            // Verify only one product exists with this SKU
            assertTrue(productRepository.existsBySku("LAPTOP-DELL-XPS15"));
        }

        @Test
        @Order(21)
        @DisplayName("UI03 - SKU uniqueness is enforced globally across all suppliers")
        void shouldEnforceGlobalSkuUniqueness() {
            // Given - Create product with first supplier
            CreateProductCommand firstCommand = new CreateProductCommand(
                "GLOBAL-SKU-001",
                "Product by Supplier 1",
                null,
                null,
                100.00,
                1,
                testSupplierId,
                null,
                null,
                null,
                null
            );
            CreateProductResult firstResult = createProductUseCase.execute(firstCommand);
            assertTrue(firstResult.isSuccess());

            // When - Try to create product with same SKU but different supplier
            // (In real scenario, this would be a different supplier ID)
            CreateProductCommand duplicateCommand = new CreateProductCommand(
                "GLOBAL-SKU-001",  // Same SKU
                "Product by Supplier 2",
                null,
                null,
                200.00,
                1,
                testSupplierId,  // Even with same supplier for this test
                null,
                null,
                null,
                null
            );
            CreateProductResult duplicateResult = createProductUseCase.execute(duplicateCommand);

            // Then
            assertFalse(duplicateResult.isSuccess());
            assertEquals("SKU_EXISTS", duplicateResult.getErrorCode());
        }
    }

    // ==================== UI04: Supplier Not Found ====================

    @Nested
    @DisplayName("UI04 - Not Found Error - Supplier Does Not Exist")
    class UI04_SupplierNotFound {

        @Test
        @Order(30)
        @DisplayName("UI04 - Should return 404 NOT_FOUND when supplier ID does not exist")
        void shouldRejectNonExistentSupplier() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-999",
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                NON_EXISTENT_SUPPLIER_ID,  // Non-existent supplier
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess(), "Product creation should fail");
            assertEquals("SUPPLIER_NOT_FOUND", result.getErrorCode());
            assertEquals("Supplier not found", result.getMessage());
            assertNull(result.getProductId(), "No product ID should be returned");

            // Verify product is NOT created in database
            assertFalse(productRepository.existsBySku("LAPTOP-HP-999"));
        }

        @Test
        @Order(31)
        @DisplayName("UI04 - Should validate supplier exists before attempting to save product")
        void shouldValidateSupplierBeforeSave() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-998",
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                999998L,  // Another non-existent supplier
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("SUPPLIER_NOT_FOUND", result.getErrorCode());

            // Verify no product was created (validation happened before save)
            assertFalse(productRepository.existsBySku("LAPTOP-HP-998"));
        }
    }

    // ==================== UI05: Invalid Category IDs ====================

    @Nested
    @DisplayName("UI05 - Validation Error - Invalid Category IDs")
    class UI05_InvalidCategoryIds {

        @Test
        @Order(40)
        @DisplayName("UI05 - Should return 400 BAD_REQUEST when one or more category IDs do not exist")
        void shouldRejectInvalidCategoryIds() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-997",
                "HP Laptop",
                null,
                Arrays.asList(testCategoryId1, NON_EXISTENT_CATEGORY_ID, 777L),  // Mix of valid and invalid
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess(), "Product creation should fail");
            assertEquals("INVALID_CATEGORY_IDS", result.getErrorCode());
            assertEquals("One or more category IDs not found", result.getMessage());
            assertNull(result.getProductId(), "No product ID should be returned");

            // Verify product is NOT created in database
            assertFalse(productRepository.existsBySku("LAPTOP-HP-997"));
        }

        @Test
        @Order(41)
        @DisplayName("UI05 - Should reject when all category IDs are invalid")
        void shouldRejectAllInvalidCategoryIds() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-996",
                "HP Laptop",
                null,
                Arrays.asList(888L, 999L),  // All invalid
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_CATEGORY_IDS", result.getErrorCode());
        }

        @Test
        @Order(42)
        @DisplayName("UI05 - Should succeed when all category IDs are valid")
        void shouldSucceedWithValidCategoryIds() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-995",
                "HP Laptop",
                null,
                Arrays.asList(testCategoryId1, testCategoryId2),  // All valid
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Product should be created with valid categories");

            Optional<Product> product = productRepository.findById(result.getProductId());
            assertTrue(product.isPresent());
            assertEquals(2, product.get().getCategories().size());
        }

        @Test
        @Order(43)
        @DisplayName("UI05 - Should succeed when no category IDs are provided")
        void shouldSucceedWithoutCategories() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-HP-994",
                "HP Laptop",
                null,
                null,  // No categories
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Product should be created without categories");
        }
    }

    // ==================== Additional Validation Tests (Bonus) ====================

    @Nested
    @DisplayName("Additional Validation Tests")
    class AdditionalValidationTests {

        @Test
        @Order(50)
        @DisplayName("Should reject empty SKU")
        void shouldRejectEmptySku() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "",  // Empty SKU
                "HP Laptop",
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_SKU", result.getErrorCode());
        }

        @Test
        @Order(51)
        @DisplayName("Should reject product name shorter than 3 characters")
        void shouldRejectShortName() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-001",
                "HP",  // Only 2 characters
                null,
                null,
                1299.99,
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_NAME_FORMAT", result.getErrorCode());
        }

        @Test
        @Order(52)
        @DisplayName("Should reject base price of zero")
        void shouldRejectZeroPrice() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-001",
                "HP Laptop",
                null,
                null,
                0.0,  // Invalid: zero price
                10,
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_BASE_PRICE", result.getErrorCode());
        }

        @Test
        @Order(53)
        @DisplayName("Should reject minimum order quantity of zero")
        void shouldRejectZeroMoq() {
            // Given
            CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-001",
                "HP Laptop",
                null,
                null,
                1299.99,
                0,  // Invalid: zero MOQ
                testSupplierId,
                null,
                null,
                null,
                null
            );

            // When
            CreateProductResult result = createProductUseCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("INVALID_MOQ_VALUE", result.getErrorCode());
        }
    }
}
