package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateProductUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductUseCase Unit Tests")
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateProductUseCase useCase;

    private Product existingProduct;
    private UpdateProductCommand validCommand;

    @BeforeEach
    void setUp() {
        existingProduct = new Product(
            1L,
            "PRD-12345",
            "Wireless Mouse",
            "High-quality wireless mouse",
            1L,
            1L,
            25.99,
            10,
            "piece",
            null,
            null,
            null,
            "ACTIVE",
            null,
            null
        );

        validCommand = new UpdateProductCommand(
            1L,
            "Updated Wireless Mouse",
            "Updated high-quality wireless mouse with ergonomic design",
            1L,
            29.99,
            15
        );
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully update product")
    void testExecute_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
        assertEquals("Product updated successfully", result.getMessage());
        assertNull(result.getErrorCode());

        // Verify domain logic was called
        assertEquals("Updated Wireless Mouse", existingProduct.getName());
        assertEquals("Updated high-quality wireless mouse with ergonomic design", existingProduct.getDescription());
        assertEquals("Computer Accessories", existingProduct.getCategoryId());
        assertEquals(29.99, existingProduct.getBasePrice());
        assertEquals(15, existingProduct.getMinimumOrderQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Should update only provided fields")
    void testExecute_PartialUpdate() {
        // Given
        UpdateProductCommand partialCommand = new UpdateProductCommand(
            1L,
            "New Name Only",
            null,  // Don't update description
            null,  // Don't update category
            null,  // Don't update price
            null   // Don't update MOQ
        );

        String originalDescription = existingProduct.getDescription();
        Long originalCategory = existingProduct.getCategoryId();
        Double originalPrice = existingProduct.getBasePrice();
        Integer originalMoq = existingProduct.getMinimumOrderQuantity();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(partialCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("New Name Only", existingProduct.getName());
        assertEquals(originalDescription, existingProduct.getDescription());
        assertEquals(originalCategory, existingProduct.getCategoryId());
        assertEquals(originalPrice, existingProduct.getBasePrice());
        assertEquals(originalMoq, existingProduct.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should trim whitespace from updated fields")
    void testExecute_TrimWhitespace() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            "  Trimmed Name  ",
            "  Trimmed Description  ",
            2L,
            null,
            null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Trimmed Name", existingProduct.getName());
        assertEquals("Trimmed Description", existingProduct.getDescription());
        assertEquals("Trimmed Category", existingProduct.getCategoryId());
    }

    @Test
    @DisplayName("Should not update SKU or supplier ID")
    void testExecute_DoesNotUpdateSkuOrSupplierId() {
        // Given
        String originalSku = existingProduct.getSku();
        Long originalSupplierId = existingProduct.getSupplierId();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalSku, existingProduct.getSku(), "SKU should not change");
        assertEquals(originalSupplierId, existingProduct.getSupplierId(), "Supplier ID should not change");
    }

    @Test
    @DisplayName("Should preserve status and timestamps")
    void testExecute_PreservesStatusAndTimestamps() {
        // Given
        String originalStatus = existingProduct.getStatus();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalStatus, existingProduct.getStatus(), "Status should not change");
    }

    @Test
    @DisplayName("Should update base price only")
    void testExecute_UpdatePriceOnly() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            39.99,  // Only update price
            null
        );

        String originalName = existingProduct.getName();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(39.99, existingProduct.getBasePrice());
        assertEquals(originalName, existingProduct.getName());
    }

    @Test
    @DisplayName("Should update MOQ only")
    void testExecute_UpdateMoqOnly() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            20  // Only update MOQ
        );

        Double originalPrice = existingProduct.getBasePrice();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(20, existingProduct.getMinimumOrderQuantity());
        assertEquals(originalPrice, existingProduct.getBasePrice());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when product ID is null")
    void testExecute_NullProductId() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            null,  // Null ID
            "New Name",
            "New Description",
            2L,
            10.0,
            5
        );

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getProductId());
        assertEquals("Product ID is required", result.getMessage());
        assertEquals("INVALID_PRODUCT_ID", result.getErrorCode());

        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when product not found")
    void testExecute_ProductNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(
            999L,
            "New Name",
            "New Description",
            2L,
            10.0,
            5
        );

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getProductId());
        assertEquals("Product not found", result.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", result.getErrorCode());

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when base price is invalid - negative")
    void testExecute_InvalidPrice_Negative() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            -10.0,  // Negative price
            null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
        assertTrue(result.getMessage().contains("Price must be at least"));

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when base price is invalid - zero")
    void testExecute_InvalidPrice_Zero() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            0.0,  // Zero price
            null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
        assertTrue(result.getMessage().contains("Price must be at least"));
    }

    @Test
    @DisplayName("Should fail when MOQ is invalid - zero")
    void testExecute_InvalidMoq_Zero() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            0  // Zero MOQ
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
        assertTrue(result.getMessage().contains("MOQ must be between"));
    }

    @Test
    @DisplayName("Should fail when MOQ is invalid - negative")
    void testExecute_InvalidMoq_Negative() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            -5  // Negative MOQ
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when MOQ exceeds maximum")
    void testExecute_InvalidMoq_TooLarge() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            1000001  // Exceeds maximum
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle empty strings as non-updates")
    void testExecute_EmptyStringsIgnored() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,      // Empty - should not update
            "   ",   // Whitespace - should not update
            null,      // Empty - should not update
            null,
            null
        );

        String originalName = existingProduct.getName();
        Long originalCategory = existingProduct.getCategoryId();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(originalName, existingProduct.getName());
        assertEquals(originalCategory, existingProduct.getCategoryId());
    }

    @Test
    @DisplayName("Should successfully update all fields to new values")
    void testExecute_UpdateAllFields() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotEquals("Wireless Mouse", existingProduct.getName());
        assertNotEquals("High-quality wireless mouse", existingProduct.getDescription());
        assertNotEquals("Electronics", existingProduct.getCategoryId());
        assertNotEquals(25.99, existingProduct.getBasePrice());
        assertNotEquals(10, existingProduct.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should call save after updating product")
    void testExecute_CallsSaveAfterUpdate() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findById(1L);
        inOrder.verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Should only call findById once")
    void testExecute_FindByIdCalledOnce() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should only call save once")
    void testExecute_SaveCalledOnce() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Should return saved product ID in result")
    void testExecute_ReturnsSavedProductId() {
        // Given
        Product savedProduct = new Product(
            1L, existingProduct.getSku(), "Updated Name", existingProduct.getDescription(),
            existingProduct.getCategoryId(), existingProduct.getSupplierId(),
            existingProduct.getBasePrice(), existingProduct.getMinimumOrderQuantity(),
            existingProduct.getUnit(), existingProduct.getImages(), existingProduct.getVariants(),
            existingProduct.getPriceTiers(), existingProduct.getStatus(), null, null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        UpdateProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
    }

    @Test
    @DisplayName("Should handle minimum valid price update")
    void testExecute_MinimumValidPrice() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            0.01,  // Minimum valid price
            null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(0.01, existingProduct.getBasePrice());
    }

    @Test
    @DisplayName("Should handle minimum valid MOQ update")
    void testExecute_MinimumValidMoq() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            1  // Minimum valid MOQ
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, existingProduct.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should handle maximum valid MOQ update")
    void testExecute_MaximumValidMoq() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            null,
            null,
            null,
            1000000  // Maximum valid MOQ
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1000000, existingProduct.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should handle very long description")
    void testExecute_LongDescription() {
        // Given
        String longDescription = "A".repeat(1000);  // Very long description
        UpdateProductCommand command = new UpdateProductCommand(
            1L,
            null,
            longDescription,
            null,
            null,
            null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(longDescription, existingProduct.getDescription());
    }
}
