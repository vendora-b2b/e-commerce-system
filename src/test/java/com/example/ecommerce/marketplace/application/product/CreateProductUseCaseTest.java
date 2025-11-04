package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateProductUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProductUseCase Unit Tests")
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private CreateProductUseCase useCase;

    private CreateProductCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CreateProductCommand(
            "PRD-12345",
            "Wireless Mouse",
            "High-quality wireless mouse with ergonomic design",
            "Electronics",
            25.99,
            10,
            1L,
            Arrays.asList("image1.jpg", "image2.jpg"), null, null);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully create product with valid data")
    void testExecute_Success() {
        // Given
        Product savedProduct = new Product(
            1L,
            "PRD-12345",
            "Wireless Mouse",
            "High-quality wireless mouse with ergonomic design",
            "Electronics",
            1L,
            25.99,
            10,
            null,
            Arrays.asList("image1.jpg", "image2.jpg"),
            null,
            null,
            "ACTIVE",
            null,
            null
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku("PRD-12345")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
        assertEquals("Product created successfully", result.getMessage());
        assertNull(result.getErrorCode());

        verify(supplierRepository).findById(1L);
        verify(productRepository).existsBySku("PRD-12345");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should create product with initial state")
    void testExecute_InitialState() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            // Verify initial state
            assertNull(product.getId(), "ID should be null before save");
            assertEquals("ACTIVE", product.getStatus(), "Initial status should be ACTIVE");
            return new Product(1L, product.getSku(), product.getName(), product.getDescription(),
                product.getCategory(), product.getSupplierId(), product.getBasePrice(),
                product.getMinimumOrderQuantity(), product.getUnit(), product.getImages(),
                product.getVariants(), product.getPriceTiers(), "ACTIVE", null, null);
        });

        // When
        CreateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when SKU is null")
    void testExecute_NullSku() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            null,
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getProductId());
        assertEquals("Product SKU is required", result.getMessage());
        assertEquals("INVALID_SKU", result.getErrorCode());

        verify(productRepository, never()).save(any());
        verify(supplierRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fail when SKU is empty")
    void testExecute_EmptySku() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "   ",
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_SKU", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when name is null")
    void testExecute_NullName() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            null,
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Product name is required", result.getMessage());
        assertEquals("INVALID_NAME", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when name is empty")
    void testExecute_EmptyName() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "   ",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_NAME", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when base price is null")
    void testExecute_NullBasePrice() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            null,
            5,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Base price is required", result.getMessage());
        assertEquals("INVALID_PRICE", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when minimum order quantity is null")
    void testExecute_NullMinimumOrderQuantity() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            null,
            1L,
            null, null, null);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Minimum order quantity is required", result.getMessage());
        assertEquals("INVALID_MOQ", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when supplier ID is null")
    void testExecute_NullSupplierId() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            null,
            null,
            null,
            null
        );

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Supplier ID is required", result.getMessage());
        assertEquals("INVALID_SUPPLIER_ID", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when supplier not found")
    void testExecute_SupplierNotFound() {
        // Given
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            999L,
            null,
            null,
            null
        );

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Supplier not found", result.getMessage());
        assertEquals("SUPPLIER_NOT_FOUND", result.getErrorCode());

        verify(supplierRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when SKU format is invalid - too short")
    void testExecute_InvalidSkuFormat_TooShort() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD",  // Too short (less than 5 chars)
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null,
            null,
            null
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid SKU format (alphanumeric and hyphens only, max 50 characters)", result.getMessage());
        assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when SKU format is invalid - too long")
    void testExecute_InvalidSkuFormat_TooLong() {
        // Given
        String longSku = "A".repeat(51);  // 51 characters
        CreateProductCommand command = new CreateProductCommand(
            longSku,
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when SKU contains lowercase letters")
    void testExecute_InvalidSkuFormat_Lowercase() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "prd-12345",  // Lowercase not allowed
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_SKU_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when name is too short")
    void testExecute_InvalidNameFormat_TooShort() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "AB",  // Less than 3 characters
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid product name (minimum 3 characters, maximum 200 characters)", result.getMessage());
        assertEquals("INVALID_NAME_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when name is too long")
    void testExecute_InvalidNameFormat_TooLong() {
        // Given
        String longName = "A".repeat(201);  // 201 characters
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            longName,
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_NAME_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when base price is zero")
    void testExecute_InvalidBasePrice_Zero() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            0.0,  // Zero not allowed
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid base price (must be greater than 0)", result.getMessage());
        assertEquals("INVALID_BASE_PRICE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when base price is negative")
    void testExecute_InvalidBasePrice_Negative() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            -10.0,  // Negative not allowed
            5,
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_BASE_PRICE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when minimum order quantity is zero")
    void testExecute_InvalidMoq_Zero() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            0,  // Zero not allowed
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid minimum order quantity (must be at least 1)", result.getMessage());
        assertEquals("INVALID_MOQ_VALUE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when minimum order quantity is negative")
    void testExecute_InvalidMoq_Negative() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            -5,  // Negative not allowed
            1L,
            null, null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_MOQ_VALUE", result.getErrorCode());
    }

    // ===== Uniqueness Tests =====

    @Test
    @DisplayName("Should fail when SKU already exists")
    void testExecute_SkuExists() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku("PRD-12345")).thenReturn(true);

        // When
        CreateProductResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("SKU already exists", result.getMessage());
        assertEquals("SKU_EXISTS", result.getErrorCode());

        verify(supplierRepository).findById(1L);
        verify(productRepository).existsBySku("PRD-12345");
        verify(productRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle valid SKU with hyphens")
    void testExecute_SkuWithHyphens() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-123-456",  // Valid with hyphens
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null, null, null);

        Product savedProduct = new Product(1L, "PRD-123-456", "Test Product", "Description",
            "Category", 1L, 10.0, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle optional fields as null")
    void testExecute_OptionalFieldsNull() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            null,  // Optional
            null,  // Optional
            10.0,
            5,
            1L,
            null,  // Optional
            null,  // Optional
            null   // Optional
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", null,
            null, 1L, 10.0, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle product with images")
    void testExecute_WithImages() {
        // Given
        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", "Description",
            "Category", 1L, 10.0, 5, null, Arrays.asList("img1.jpg", "img2.jpg"), 
            null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        Product savedProduct = new Product(1L, "PRD-12345", "Test", "Desc",
            "Cat", 1L, 10.0, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(supplierRepository, productRepository);
        inOrder.verify(supplierRepository).findById(1L);
        inOrder.verify(productRepository).existsBySku(anyString());
        inOrder.verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle minimum valid price")
    void testExecute_MinimumValidPrice() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            0.01,  // Minimum valid price
            5,
            1L,
            null, null, null);

        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", "Description",
            "Category", 1L, 0.01, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle minimum valid MOQ")
    void testExecute_MinimumValidMoq() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            1,  // Minimum valid MOQ
            1L,
            null, null, null);

        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", "Description",
            "Category", 1L, 10.0, 1, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Price Tiers and Variants Tests =====

    @Test
    @DisplayName("Should successfully create product with price tiers")
    void testExecute_WithPriceTiers() {
        // Given
        List<CreateProductCommand.PriceTierDto> tiers = Arrays.asList(
            new CreateProductCommand.PriceTierDto(1, 49, 25.99, null),
            new CreateProductCommand.PriceTierDto(50, 99, 22.99, 11.5),
            new CreateProductCommand.PriceTierDto(100, null, 19.99, 23.1)
        );

        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Bulk Product",
            "Product with bulk pricing",
            "Category",
            25.99,
            1,
            1L,
            Arrays.asList("image1.jpg"),
            tiers,
            null
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Bulk Product", "Product with bulk pricing",
            "Category", 1L, 25.99, 1, null, Arrays.asList("image1.jpg"), null, 
            Arrays.asList(
                new Product.PriceTier(1L, 1, 49, 25.99, null),
                new Product.PriceTier(2L, 50, 99, 22.99, 11.5),
                new Product.PriceTier(3L, 100, null, 19.99, 23.1)
            ), 
            "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should successfully create product with variants")
    void testExecute_WithVariants() {
        // Given
        List<CreateProductCommand.ProductVariantDto> variants = Arrays.asList(
            new CreateProductCommand.ProductVariantDto("Color", "Red", 0.0, Arrays.asList("red1.jpg")),
            new CreateProductCommand.ProductVariantDto("Color", "Blue", 2.0, Arrays.asList("blue1.jpg")),
            new CreateProductCommand.ProductVariantDto("Size", "Large", 5.0, null)
        );

        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Configurable Product",
            "Product with variants",
            "Category",
            25.99,
            1,
            1L,
            Arrays.asList("image1.jpg"),
            null,
            variants
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Configurable Product", "Product with variants",
            "Category", 1L, 25.99, 1, null, Arrays.asList("image1.jpg"),
            Arrays.asList(
                new Product.ProductVariant(1L, "Color", "Red", 0.0, Arrays.asList("red1.jpg")),
                new Product.ProductVariant(2L, "Color", "Blue", 2.0, Arrays.asList("blue1.jpg")),
                new Product.ProductVariant(3L, "Size", "Large", 5.0, null)
            ),
            null,
            "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should successfully create product with both price tiers and variants")
    void testExecute_WithPriceTiersAndVariants() {
        // Given
        List<CreateProductCommand.PriceTierDto> tiers = Arrays.asList(
            new CreateProductCommand.PriceTierDto(1, 49, 25.99, null),
            new CreateProductCommand.PriceTierDto(50, null, 22.99, 11.5)
        );

        List<CreateProductCommand.ProductVariantDto> variants = Arrays.asList(
            new CreateProductCommand.ProductVariantDto("Color", "Red", 0.0, Arrays.asList("red1.jpg")),
            new CreateProductCommand.ProductVariantDto("Size", "Large", 5.0, null)
        );

        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Full Featured Product",
            "Product with tiers and variants",
            "Category",
            25.99,
            1,
            1L,
            Arrays.asList("image1.jpg"),
            tiers,
            variants
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Full Featured Product", "Product with tiers and variants",
            "Category", 1L, 25.99, 1, null, Arrays.asList("image1.jpg"),
            Arrays.asList(
                new Product.ProductVariant(1L, "Color", "Red", 0.0, Arrays.asList("red1.jpg")),
                new Product.ProductVariant(2L, "Size", "Large", 5.0, null)
            ),
            Arrays.asList(
                new Product.PriceTier(1L, 1, 49, 25.99, null),
                new Product.PriceTier(2L, 50, null, 22.99, 11.5)
            ),
            "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle empty price tiers list")
    void testExecute_EmptyPriceTiersList() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null,
            Arrays.asList(), // Empty list
            null
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", "Description",
            "Category", 1L, 10.0, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        // Empty list is treated as null per use case logic
    }

    @Test
    @DisplayName("Should handle empty variants list")
    void testExecute_EmptyVariantsList() {
        // Given
        CreateProductCommand command = new CreateProductCommand(
            "PRD-12345",
            "Test Product",
            "Description",
            "Category",
            10.0,
            5,
            1L,
            null,
            null,
            Arrays.asList() // Empty list
        );

        Product savedProduct = new Product(1L, "PRD-12345", "Test Product", "Description",
            "Category", 1L, 10.0, 5, null, null, null, null, "ACTIVE", null, null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mock(com.example.ecommerce.marketplace.domain.supplier.Supplier.class)));
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        CreateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        // Empty list is treated as null per use case logic
    }
}
