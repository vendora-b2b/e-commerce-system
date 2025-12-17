package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.application.ai.IngestProductCommand;
import com.example.ecommerce.marketplace.application.ai.IngestProductUseCase;
import com.example.ecommerce.marketplace.domain.product.Category;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateProductUseCase AI integration.
 * Tests the conditional AI re-indexing feature:
 * - Re-index ONLY when semantic fields (name, description, categories) change
 * - DO NOT re-index when non-semantic fields (price, MOQ, priceTiers) change
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductUseCase AI Integration Tests")
class UpdateProductAiIntegrationTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.example.ecommerce.marketplace.infrastructure.product.JpaCategoryRepository categoryRepository;

    @Mock
    private IngestProductUseCase ingestProductUseCase;

    private UpdateProductUseCase useCase;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductUseCase(productRepository, categoryRepository, ingestProductUseCase);
        
        // Create a base product for testing
        existingProduct = new Product(
            1L,
            "SKU-001",
            "Original Name",
            "Original Description",
            List.of(new Category(1L, "Electronics", "electronics", null, null)),
            100L, // supplierId
            199.99, // basePrice
            10, // minimumOrderQuantity
            "piece",
            List.of("image1.jpg"),
            null, // priceTiers
            null, // createdAt
            null  // updatedAt
        );
    }

    // Helper method to create UpdateProductCommand
    private UpdateProductCommand createCommand(Long productId, String name, String description,
                                               List<Long> categoryIds,
                                               Double basePrice, Integer moq, String unit,
                                               List<String> images,
                                               List<UpdateProductCommand.PriceTierDto> priceTiers) {
        return new UpdateProductCommand(productId, name, description, categoryIds, basePrice, moq, unit,
                                        images, priceTiers);
    }

    // ===== Tests: AI Re-index SHOULD trigger =====
    
    @Nested
    @DisplayName("AI Re-index SHOULD trigger when semantic fields change")
    class SemanticFieldChanges {

        @Test
        @DisplayName("Should trigger AI re-index when product NAME changes")
        void testUpdate_NameChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, "Updated Name", null, null, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should trigger AI re-index when product DESCRIPTION changes")
        void testUpdate_DescriptionChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, null, "Updated Description", null, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should trigger AI re-index when product CATEGORIES change")
        void testUpdate_CategoriesChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            // Mock category repository to return a category entity
            List<Long> newCategoryIds = List.of(2L);
            com.example.ecommerce.marketplace.infrastructure.product.CategoryEntity categoryEntity =
                new com.example.ecommerce.marketplace.infrastructure.product.CategoryEntity();
            categoryEntity.setId(2L);
            categoryEntity.setName("Fashion");
            categoryEntity.setSlug("fashion");
            when(categoryRepository.findAllById(newCategoryIds)).thenReturn(List.of(categoryEntity));

            UpdateProductCommand command = createCommand(
                1L, null, null, newCategoryIds, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should trigger AI re-index when name AND description change")
        void testUpdate_NameAndDescriptionChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, "New Name", "New Description", null, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, times(1)).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should send correct data to AI service when semantic fields change")
        void testUpdate_SemanticChange_SendsCorrectData() {
            // Given
            ArgumentCaptor<IngestProductCommand> captor = ArgumentCaptor.forClass(IngestProductCommand.class);
            
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateProductCommand command = createCommand(
                1L, "Super Gaming Laptop", "Best gaming laptop ever", null, null, null, null, null, null);

            // When
            useCase.execute(command);

            // Then
            verify(ingestProductUseCase).executeAsync(captor.capture());
            IngestProductCommand captured = captor.getValue();
            
            assertEquals(1L, captured.getProductId());
            assertEquals("SKU-001", captured.getSku());
            assertEquals("Super Gaming Laptop", captured.getName());
            assertEquals("Best gaming laptop ever", captured.getDescription());
            assertEquals(100L, captured.getSupplierId());
        }
    }

    // ===== Tests: AI Re-index should NOT trigger =====
    
    @Nested
    @DisplayName("AI Re-index should NOT trigger when only non-semantic fields change")
    class NonSemanticFieldChanges {

        @Test
        @DisplayName("Should NOT trigger AI re-index when only BASE PRICE changes")
        void testUpdate_OnlyPriceChange_NoAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, null, null, null, 299.99, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, never()).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should NOT trigger AI re-index when only MINIMUM ORDER QUANTITY changes")
        void testUpdate_OnlyMoqChange_NoAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, null, null, null, null, 50, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, never()).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should NOT trigger AI re-index when only PRICE TIERS change")
        void testUpdate_OnlyPriceTiersChange_NoAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            List<UpdateProductCommand.PriceTierDto> priceTiers = List.of(
                new UpdateProductCommand.PriceTierDto(10, 50, 5.0),
                new UpdateProductCommand.PriceTierDto(51, 100, 10.0)
            );
            UpdateProductCommand command = createCommand(
                1L, null, null, null, null, null, null, null, priceTiers);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, never()).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should NOT trigger AI re-index when only IMAGES change")
        void testUpdate_OnlyImagesChange_NoAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, null, null, null, null, null, null,
                List.of("new-image1.jpg", "new-image2.jpg"), null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, never()).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should NOT trigger AI re-index when price AND MOQ AND priceTiers change together")
        void testUpdate_MultipleNonSemanticChanges_NoAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            List<UpdateProductCommand.PriceTierDto> priceTiers = List.of(
                new UpdateProductCommand.PriceTierDto(25, 100, 15.0)
            );
            UpdateProductCommand command = createCommand(
                1L, null, null, null, 399.99, 25, null, null, priceTiers);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase, never()).executeAsync(any(IngestProductCommand.class));
        }
    }

    // ===== Mixed Changes =====

    @Nested
    @DisplayName("Mixed semantic and non-semantic changes")
    class MixedChanges {

        @Test
        @DisplayName("Should trigger AI re-index when name changes along with price")
        void testUpdate_NameAndPriceChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, "Updated Product Name", null, null, 599.99, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            // Should trigger because name changed (even though price also changed)
            verify(ingestProductUseCase).executeAsync(any(IngestProductCommand.class));
        }

        @Test
        @DisplayName("Should trigger AI re-index when description changes along with MOQ")
        void testUpdate_DescriptionAndMoqChange_TriggersAiReindex() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            UpdateProductCommand command = createCommand(
                1L, null, "Brand new description", null, null, 100, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            verify(ingestProductUseCase).executeAsync(any(IngestProductCommand.class));
        }
    }

    // ===== Error Handling =====

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should not fail product update when AI service fails")
        void testUpdate_AiServiceFails_ProductStillUpdated() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
            doThrow(new RuntimeException("AI service down")).when(ingestProductUseCase).executeAsync(any());

            UpdateProductCommand command = createCommand(
                1L, "Updated Name", null, null, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then - Product update should still succeed
            assertTrue(result.isSuccess());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should return failure when product not found")
        void testUpdate_ProductNotFound_ReturnsFailure() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            UpdateProductCommand command = createCommand(
                999L, "Updated Name", null, null, null, null, null, null, null);

            // When
            UpdateProductResult result = useCase.execute(command);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("PRODUCT_NOT_FOUND", result.getErrorCode());
            verify(ingestProductUseCase, never()).executeAsync(any());
        }
    }
}
