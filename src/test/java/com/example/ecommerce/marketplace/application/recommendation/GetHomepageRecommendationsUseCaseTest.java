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
 * Unit tests for GetHomepageRecommendationsUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetHomepageRecommendationsUseCase Unit Tests")
class GetHomepageRecommendationsUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private GetHomepageRecommendationsUseCase useCase;

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
    @DisplayName("Should successfully get homepage recommendations for anonymous user")
    void testExecute_AnonymousUser_Success() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forAnonymous();
        when(aiServiceClient.getHomepageRecommendations(isNull(), eq(12))).thenReturn(mockResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecommendations().size());
        assertFalse(result.isPersonalized());
        verify(aiServiceClient).getHomepageRecommendations(isNull(), eq(12));
    }

    @Test
    @DisplayName("Should successfully get personalized recommendations for authenticated user")
    void testExecute_AuthenticatedUser_Success() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12))).thenReturn(mockResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecommendations().size());
        assertTrue(result.isPersonalized());
        verify(aiServiceClient).getHomepageRecommendations(eq(100L), eq(12));
    }

    @Test
    @DisplayName("Should successfully get recommendations with custom limit")
    void testExecute_CustomLimit_Success() {
        // Given
        GetHomepageRecommendationsCommand command = new GetHomepageRecommendationsCommand(100L, 5);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(5))).thenReturn(mockResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        verify(aiServiceClient).getHomepageRecommendations(eq(100L), eq(5));
    }

    @Test
    @DisplayName("Should return empty list when no recommendations available")
    void testExecute_NoRecommendations_ReturnsEmptyList() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        RecommendationResponse emptyResponse = RecommendationResponse.builder()
            .recommendations(Collections.emptyList())
            .total(0)
            .build();
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12))).thenReturn(emptyResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getRecommendations().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("Should return empty list when response is null")
    void testExecute_NullResponse_ReturnsEmptyList() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12))).thenReturn(null);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getRecommendations().isEmpty());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12)))
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
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12)))
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
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12))).thenReturn(mockResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        List<GetHomepageRecommendationsResult.ProductRecommendation> recs = result.getRecommendations();
        assertEquals(2, recs.size());
        
        GetHomepageRecommendationsResult.ProductRecommendation first = recs.get(0);
        assertEquals(1L, first.getProductId());
        assertEquals("SKU-001", first.getSku());
        assertEquals("Product 1", first.getName());
        assertEquals(0.95, first.getScore());
    }

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forUser(100L);
        when(aiServiceClient.getHomepageRecommendations(eq(100L), eq(12))).thenReturn(mockResponse);

        // When
        GetHomepageRecommendationsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    // ===== Command Tests =====

    @Test
    @DisplayName("Should correctly identify anonymous command")
    void testCommand_IsAnonymous() {
        // Given
        GetHomepageRecommendationsCommand anonymousCmd = GetHomepageRecommendationsCommand.forAnonymous();
        GetHomepageRecommendationsCommand userCmd = GetHomepageRecommendationsCommand.forUser(100L);

        // Then
        assertTrue(anonymousCmd.isAnonymous());
        assertFalse(userCmd.isAnonymous());
    }

    @Test
    @DisplayName("Should use default limit when not specified")
    void testCommand_DefaultLimit() {
        // Given
        GetHomepageRecommendationsCommand command = GetHomepageRecommendationsCommand.forAnonymous();

        // Then
        assertEquals(12, command.getLimit());
    }
}
