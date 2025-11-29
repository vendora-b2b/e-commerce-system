package com.example.ecommerce.marketplace.application.recommendation;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.RecommendationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Unit tests for GetProductRecommendationsUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductRecommendationsUseCase Unit Tests")
class GetProductRecommendationsUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private GetProductRecommendationsUseCase useCase;

    private RecommendationResponse mockResponse;

    @BeforeEach
    void setUp() {
        List<RecommendationResponse.ProductRecommendation> recommendations = Arrays.asList(
            RecommendationResponse.ProductRecommendation.builder()
                .productId(1L)
                .sku("SKU-001")
                .name("Product 1")
                .score(0.95)
                .build(),
            RecommendationResponse.ProductRecommendation.builder()
                .productId(2L)
                .sku("SKU-002")
                .name("Product 2")
                .score(0.85)
                .build()
        );

        mockResponse = RecommendationResponse.builder()
            .recommendations(recommendations)
            .total(2)
            .build();
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully get product recommendations")
    void testExecute_Success() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10))).thenReturn(mockResponse);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecommendations().size());
        assertEquals(2, result.getTotal());
        verify(aiServiceClient).getUserRecommendations(eq(100L), eq(10));
    }

    @Test
    @DisplayName("Should successfully get recommendations with custom limit")
    void testExecute_CustomLimit_Success() {
        // Given
        GetProductRecommendationsCommand command = new GetProductRecommendationsCommand(100L, 5);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(5))).thenReturn(mockResponse);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        verify(aiServiceClient).getUserRecommendations(eq(100L), eq(5));
    }

    @Test
    @DisplayName("Should return empty list when no recommendations available")
    void testExecute_NoRecommendations_ReturnsEmptyList() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        RecommendationResponse emptyResponse = RecommendationResponse.builder()
            .recommendations(Collections.emptyList())
            .total(0)
            .build();
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10))).thenReturn(emptyResponse);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getRecommendations().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("Should return empty list when response is null")
    void testExecute_NullResponse_ReturnsEmptyList() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10))).thenReturn(null);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getRecommendations().isEmpty());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void testExecute_NullUserId_ThrowsException() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10)))
            .thenThrow(new AiServiceException("Service unavailable"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("RECOMMENDATION_SERVICE_ERROR", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service times out")
    void testExecute_AiServiceTimeout_ThrowsException() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10)))
            .thenThrow(new AiServiceException("Request timeout"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("RECOMMENDATION_SERVICE_ERROR", exception.getErrorCode());
    }

    // ===== Result Tests =====

    @Test
    @DisplayName("Should correctly map AI response to result format")
    void testExecute_MapsResponseCorrectly() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10))).thenReturn(mockResponse);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        List<GetProductRecommendationsResult.ProductRecommendation> recs = result.getRecommendations();
        assertEquals(2, recs.size());
        
        GetProductRecommendationsResult.ProductRecommendation first = recs.get(0);
        assertEquals(1L, first.getProductId());
        assertEquals("SKU-001", first.getSku());
        assertEquals("Product 1", first.getName());
        assertEquals(0.95, first.getScore());
    }

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);
        when(aiServiceClient.getUserRecommendations(eq(100L), eq(10))).thenReturn(mockResponse);

        // When
        GetProductRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    // ===== Command Tests =====

    @Test
    @DisplayName("Should use default limit when not specified")
    void testCommand_DefaultLimit() {
        // Given
        GetProductRecommendationsCommand command = GetProductRecommendationsCommand.withDefaults(100L);

        // Then
        assertEquals(10, command.getLimit());
        assertEquals(100L, command.getUserId());
    }
}
