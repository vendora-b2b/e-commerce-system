package com.example.ecommerce.marketplace.application.recommendation;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.RecommendationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetSimilarProductsUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetSimilarProductsUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private GetSimilarProductsUseCase getSimilarProductsUseCase;

    private static final Long PRODUCT_ID = 100L;
    private static final Integer DEFAULT_LIMIT = 6;

    private RecommendationResponse createResponse(List<RecommendationResponse.ProductRecommendation> recommendations) {
        return RecommendationResponse.builder()
            .recommendations(recommendations)
            .total(recommendations != null ? recommendations.size() : 0)
            .build();
    }

    private RecommendationResponse.ProductRecommendation createRecommendation(Long productId, String sku, String name, Double score) {
        return RecommendationResponse.ProductRecommendation.builder()
            .productId(productId)
            .sku(sku)
            .name(name)
            .score(score)
            .build();
    }

    @Nested
    @DisplayName("Success Cases")
    class SuccessCases {

        @Test
        @DisplayName("Should return similar products successfully")
        void shouldReturnSimilarProductsSuccessfully() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Arrays.asList(
                createRecommendation(201L, "SKU-201", "Similar Product 1", 0.95),
                createRecommendation(202L, "SKU-202", "Similar Product 2", 0.87)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(PRODUCT_ID, result.getSourceProductId());
            assertEquals(2, result.getSimilarProducts().size());
            assertEquals(2, result.getTotal());
            assertNull(result.getErrorCode());
            verify(aiServiceClient).getSimilarProducts(PRODUCT_ID, 5);
        }

        @Test
        @DisplayName("Should return similar products with default limit")
        void shouldReturnSimilarProductsWithDefaultLimit() {
            // Arrange
            GetSimilarProductsCommand command = GetSimilarProductsCommand.withDefaults(PRODUCT_ID);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Arrays.asList(
                createRecommendation(301L, "SKU-301", "Product A", 0.92),
                createRecommendation(302L, "SKU-302", "Product B", 0.88),
                createRecommendation(303L, "SKU-303", "Product C", 0.85)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, DEFAULT_LIMIT)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(3, result.getSimilarProducts().size());
            verify(aiServiceClient).getSimilarProducts(PRODUCT_ID, DEFAULT_LIMIT);
        }

        @Test
        @DisplayName("Should correctly map recommendation fields")
        void shouldCorrectlyMapRecommendationFields() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Collections.singletonList(
                createRecommendation(400L, "SKU-400", "Mapped Product", 0.93)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            GetSimilarProductsResult.ProductRecommendation similarProduct = result.getSimilarProducts().get(0);
            assertEquals(400L, similarProduct.getProductId());
            assertEquals("SKU-400", similarProduct.getSku());
            assertEquals("Mapped Product", similarProduct.getName());
            assertEquals(0.93, similarProduct.getSimilarityScore(), 0.001);
        }

        @Test
        @DisplayName("Should handle empty response from AI service")
        void shouldHandleEmptyResponseFromAiService() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            RecommendationResponse response = createResponse(Collections.emptyList());
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(PRODUCT_ID, result.getSourceProductId());
            assertTrue(result.getSimilarProducts().isEmpty());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("Should handle null recommendations list from AI service")
        void shouldHandleNullRecommendationsListFromAiService() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            RecommendationResponse response = RecommendationResponse.builder()
                .recommendations(null)
                .total(0)
                .build();
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertTrue(result.getSimilarProducts().isEmpty());
        }

        @Test
        @DisplayName("Should handle null response from AI service")
        void shouldHandleNullResponseFromAiService() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(null);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertTrue(result.getSimilarProducts().isEmpty());
        }

        @Test
        @DisplayName("Should return recommendations sorted by similarity score")
        void shouldReturnRecommendationsSortedBySimilarityScore() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 10);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Arrays.asList(
                createRecommendation(501L, "SKU-501", "High Score", 0.99),
                createRecommendation(502L, "SKU-502", "Medium Score", 0.85),
                createRecommendation(503L, "SKU-503", "Low Score", 0.72)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 10)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(3, result.getSimilarProducts().size());
            // Verify order is preserved from AI service
            assertEquals(0.99, result.getSimilarProducts().get(0).getSimilarityScore(), 0.001);
        }
    }

    @Nested
    @DisplayName("Validation Error Cases")
    class ValidationErrorCases {

        @Test
        @DisplayName("Should throw exception for null product ID")
        void shouldThrowExceptionForNullProductId() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(null, 5);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
            assertEquals("Product ID is required", exception.getMessage());
            verifyNoInteractions(aiServiceClient);
        }

        @Test
        @DisplayName("Should throw exception for null product ID with defaults factory")
        void shouldThrowExceptionForNullProductIdWithDefaultsFactory() {
            // Arrange
            GetSimilarProductsCommand command = GetSimilarProductsCommand.withDefaults(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
            verifyNoInteractions(aiServiceClient);
        }
    }

    @Nested
    @DisplayName("AI Service Error Cases")
    class AiServiceErrorCases {

        @Test
        @DisplayName("Should throw CustomBusinessException on AI service error")
        void shouldThrowCustomBusinessExceptionOnAiServiceError() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5))
                .thenThrow(new AiServiceException("Service unavailable"));

            // Act & Assert
            CustomBusinessException exception = assertThrows(
                CustomBusinessException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
            assertEquals("RECOMMENDATION_SERVICE_ERROR", exception.getErrorCode());
            assertTrue(exception.getMessage().contains("temporarily unavailable"));
        }

        @Test
        @DisplayName("Should throw CustomBusinessException on AI connection timeout")
        void shouldThrowCustomBusinessExceptionOnConnectionTimeout() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5))
                .thenThrow(new AiServiceException("Connection timeout"));

            // Act & Assert
            CustomBusinessException exception = assertThrows(
                CustomBusinessException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
            assertTrue(exception.getMessage().contains("Connection timeout"));
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException from AI service")
        void shouldPropagateIllegalArgumentExceptionFromAiService() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5))
                .thenThrow(new IllegalArgumentException("Invalid product"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
        }

        @Test
        @DisplayName("Should propagate IllegalStateException from AI service")
        void shouldPropagateIllegalStateExceptionFromAiService() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5))
                .thenThrow(new IllegalStateException("Client not initialized"));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
        }

        @Test
        @DisplayName("Should throw CustomBusinessException on unexpected error")
        void shouldThrowCustomBusinessExceptionOnUnexpectedError() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            CustomBusinessException exception = assertThrows(
                CustomBusinessException.class,
                () -> getSimilarProductsUseCase.execute(command)
            );
            assertEquals("RECOMMENDATION_SERVICE_ERROR", exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle limit of 1")
        void shouldHandleLimitOfOne() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 1);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Collections.singletonList(
                createRecommendation(601L, "SKU-601", "Single Product", 0.91)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 1)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getSimilarProducts().size());
        }

        @Test
        @DisplayName("Should handle large limit")
        void shouldHandleLargeLimit() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 100);
            
            RecommendationResponse response = createResponse(Collections.emptyList());
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 100)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            verify(aiServiceClient).getSimilarProducts(PRODUCT_ID, 100);
        }

        @Test
        @DisplayName("Should handle product with no similar items")
        void shouldHandleProductWithNoSimilarItems() {
            // Arrange
            Long uniqueProductId = 999L;
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(uniqueProductId, 5);
            
            RecommendationResponse response = createResponse(Collections.emptyList());
            when(aiServiceClient.getSimilarProducts(uniqueProductId, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(uniqueProductId, result.getSourceProductId());
            assertTrue(result.getSimilarProducts().isEmpty());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("Should handle recommendation with zero similarity score")
        void shouldHandleRecommendationWithZeroSimilarityScore() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            List<RecommendationResponse.ProductRecommendation> recommendations = Collections.singletonList(
                createRecommendation(701L, "SKU-701", "Zero Score Product", 0.0)
            );
            
            RecommendationResponse response = createResponse(recommendations);
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0.0, result.getSimilarProducts().get(0).getSimilarityScore(), 0.001);
        }

        @Test
        @DisplayName("Should correctly set success message")
        void shouldCorrectlySetSuccessMessage() {
            // Arrange
            GetSimilarProductsCommand command = new GetSimilarProductsCommand(PRODUCT_ID, 5);
            
            RecommendationResponse response = createResponse(Collections.emptyList());
            when(aiServiceClient.getSimilarProducts(PRODUCT_ID, 5)).thenReturn(response);

            // Act
            GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals("Similar products retrieved successfully", result.getMessage());
        }
    }
}
