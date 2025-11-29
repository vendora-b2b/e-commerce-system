package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ProductIngestRequest;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IngestProductUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IngestProductUseCase Unit Tests")
class IngestProductUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private IngestProductUseCase useCase;

    private IngestProductCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = IngestProductCommand.builder()
            .productId(123L)
            .sku("SKU-001")
            .name("Gaming Laptop")
            .description("High-performance gaming laptop with RTX 4080")
            .categoryName("Electronics")
            .basePrice(1999.99)
            .tags(Arrays.asList("gaming", "laptop", "nvidia"))
            .build();
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully ingest product with all fields")
    void testExecute_Success() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(123L, result.getProductId());
        assertEquals("SKU-001", result.getSku());
        verify(aiServiceClient).ingestProduct(any(ProductIngestRequest.class));
    }

    @Test
    @DisplayName("Should send correct data to AI service")
    void testExecute_SendsCorrectData() {
        // Given
        ArgumentCaptor<ProductIngestRequest> captor = ArgumentCaptor.forClass(ProductIngestRequest.class);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        useCase.execute(validCommand);

        // Then
        verify(aiServiceClient).ingestProduct(captor.capture());
        ProductIngestRequest captured = captor.getValue();
        assertEquals(123L, captured.getProductId());
        assertEquals("SKU-001", captured.getSku());
        assertEquals("Gaming Laptop", captured.getName());
        assertEquals("High-performance gaming laptop with RTX 4080", captured.getDescription());
        assertEquals("Electronics", captured.getCategoryName());
        assertEquals(1999.99, captured.getBasePrice());
        assertEquals(3, captured.getTags().size());
    }

    @Test
    @DisplayName("Should successfully ingest product without optional tags")
    void testExecute_WithoutTags_Success() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(456L)
            .sku("SKU-002")
            .name("Simple Product")
            .description("A simple product description")
            .categoryName("General")
            .basePrice(99.99)
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(456L, result.getProductId());
    }

    @Test
    @DisplayName("Should successfully ingest product without description")
    void testExecute_WithoutDescription_Success() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(789L)
            .sku("SKU-003")
            .name("Basic Product")
            .categoryName("Basics")
            .basePrice(49.99)
            .tags(Arrays.asList("basic"))
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when productId is null")
    void testExecute_NullProductId_ThrowsException() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(null)
            .sku("SKU-001")
            .name("Test Product")
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when sku is null")
    void testExecute_NullSku_ThrowsException() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku(null)
            .name("Test Product")
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("SKU is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when sku is empty")
    void testExecute_EmptySku_ThrowsException() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku("   ")
            .name("Test Product")
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("SKU is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is null")
    void testExecute_NullName_ThrowsException() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku("SKU-001")
            .name(null)
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Product name is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is empty")
    void testExecute_EmptyName_ThrowsException() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku("SKU-001")
            .name("")
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Product name is required", exception.getMessage());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class)))
            .thenThrow(new AiServiceException("Connection refused"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service times out")
    void testExecute_AiServiceTimeout_ThrowsException() {
        // Given
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class)))
            .thenThrow(new AiServiceException("Request timeout"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service returns 500")
    void testExecute_AiServiceInternalError_ThrowsException() {
        // Given
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class)))
            .thenThrow(new AiServiceException("Internal server error"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle product with empty tags list")
    void testExecute_EmptyTagsList_Success() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku("SKU-001")
            .name("Test Product")
            .description("Description")
            .categoryName("Test")
            .basePrice(10.00)
            .tags(List.of())
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle product with zero price")
    void testExecute_ZeroPrice_Success() {
        // Given
        IngestProductCommand command = IngestProductCommand.builder()
            .productId(123L)
            .sku("SKU-FREE")
            .name("Free Product")
            .description("This is a free product")
            .categoryName("Free")
            .basePrice(0.0)
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should call AI service exactly once")
    void testExecute_CallsAiServiceOnce() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        useCase.execute(validCommand);

        // Then
        verify(aiServiceClient, times(1)).ingestProduct(any(ProductIngestRequest.class));
    }

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class))).thenReturn(response);

        // When
        IngestProductResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("Should handle generic exception from AI service")
    void testExecute_GenericException_ThrowsCustomBusinessException() {
        // Given
        when(aiServiceClient.ingestProduct(any(ProductIngestRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
    }
}
