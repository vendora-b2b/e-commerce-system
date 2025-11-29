package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeleteProductFromAiUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteProductFromAiUseCase Unit Tests")
class DeleteProductFromAiUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private DeleteProductFromAiUseCase useCase;

    private DeleteProductFromAiCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new DeleteProductFromAiCommand(123L);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully delete product from AI service")
    void testExecute_Success() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(123L)).thenReturn(response);

        // When
        DeleteProductFromAiResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(123L, result.getProductId());
        verify(aiServiceClient).deleteProduct(123L);
    }

    @Test
    @DisplayName("Should call AI service with correct product ID")
    void testExecute_CallsAiServiceWithCorrectId() {
        // Given
        Long productId = 456L;
        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(productId)).thenReturn(response);

        // When
        useCase.execute(command);

        // Then
        verify(aiServiceClient).deleteProduct(productId);
        verify(aiServiceClient, times(1)).deleteProduct(anyLong());
    }

    @Test
    @DisplayName("Should successfully delete product with different ID")
    void testExecute_DifferentProductId_Success() {
        // Given
        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(789L);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(789L)).thenReturn(response);

        // When
        DeleteProductFromAiResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(789L, result.getProductId());
    }

    @Test
    @DisplayName("Should successfully delete product with large ID")
    void testExecute_LargeProductId_Success() {
        // Given
        Long largeId = Long.MAX_VALUE - 1;
        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(largeId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(largeId)).thenReturn(response);

        // When
        DeleteProductFromAiResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(largeId, result.getProductId());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when productId is null")
    void testExecute_NullProductId_ThrowsException() {
        // Given
        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Product ID is required", exception.getMessage());
        verify(aiServiceClient, never()).deleteProduct(anyLong());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        when(aiServiceClient.deleteProduct(123L))
            .thenThrow(new AiServiceException("Service unavailable"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("DELETION_FAILED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service times out")
    void testExecute_AiServiceTimeout_ThrowsException() {
        // Given
        when(aiServiceClient.deleteProduct(123L))
            .thenThrow(new AiServiceException("Request timeout"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("DELETION_FAILED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service returns 500")
    void testExecute_AiServiceInternalError_ThrowsException() {
        // Given
        when(aiServiceClient.deleteProduct(123L))
            .thenThrow(new AiServiceException("Internal server error"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("DELETION_FAILED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when product not found in AI service")
    void testExecute_ProductNotFoundInAi_ThrowsException() {
        // Given
        when(aiServiceClient.deleteProduct(999L))
            .thenThrow(new AiServiceException("Product not found"));

        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(999L);

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("DELETION_FAILED", exception.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle multiple sequential deletions")
    void testExecute_MultipleSequentialDeletions_Success() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(anyLong())).thenReturn(response);

        // When & Then
        for (long i = 1; i <= 5; i++) {
            DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(i);
            DeleteProductFromAiResult result = useCase.execute(command);
            assertTrue(result.isSuccess());
        }

        verify(aiServiceClient, times(5)).deleteProduct(anyLong());
    }

    @Test
    @DisplayName("Should call AI service exactly once per execution")
    void testExecute_CallsAiServiceOnce() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(123L)).thenReturn(response);

        // When
        useCase.execute(validCommand);

        // Then
        verify(aiServiceClient, times(1)).deleteProduct(123L);
    }

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(123L)).thenReturn(response);

        // When
        DeleteProductFromAiResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("Should handle product ID of 1")
    void testExecute_MinimumProductId_Success() {
        // Given
        DeleteProductFromAiCommand command = new DeleteProductFromAiCommand(1L);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.deleteProduct(1L)).thenReturn(response);

        // When
        DeleteProductFromAiResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getProductId());
    }

    @Test
    @DisplayName("Should handle generic exception from AI service")
    void testExecute_GenericException_ThrowsCustomBusinessException() {
        // Given
        when(aiServiceClient.deleteProduct(123L))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("DELETION_FAILED", exception.getErrorCode());
    }
}
