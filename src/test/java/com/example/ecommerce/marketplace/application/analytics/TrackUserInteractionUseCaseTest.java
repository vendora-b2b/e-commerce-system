package com.example.ecommerce.marketplace.application.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import com.example.ecommerce.marketplace.domain.analytics.UserInteraction;
import com.example.ecommerce.marketplace.domain.analytics.UserInteractionRepository;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackUserInteractionUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrackUserInteractionUseCase Unit Tests")
class TrackUserInteractionUseCaseTest {

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private TrackUserInteractionUseCase useCase;

    private TrackUserInteractionCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = TrackUserInteractionCommand.view(100L, 200L);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully track product view interaction")
    void testExecute_ProductView_Success() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(1L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getInteractionId());
        assertEquals(InteractionType.VIEW, result.getInteractionType());
        verify(userInteractionRepository).save(any(UserInteraction.class));
    }

    @Test
    @DisplayName("Should successfully track click interaction")
    void testExecute_Click_Success() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.click(100L, 200L);
        
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.click(100L, 200L);
        savedInteraction.setId(2L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(InteractionType.CLICK, result.getInteractionType());
    }

    @Test
    @DisplayName("Should successfully track add to cart interaction")
    void testExecute_AddToCart_Success() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.addToCart(100L, 200L, 300L);
        
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.addToCart(100L, 200L, 300L);
        savedInteraction.setId(3L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(InteractionType.ADD_TO_CART, result.getInteractionType());
    }

    @Test
    @DisplayName("Should successfully track purchase interaction")
    void testExecute_Purchase_Success() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.purchase(100L, 200L, 300L);
        
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.purchase(100L, 200L, 300L);
        savedInteraction.setId(4L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(InteractionType.PURCHASE, result.getInteractionType());
    }

    @Test
    @DisplayName("Should save interaction with correct user and product IDs")
    void testExecute_SavesCorrectFields() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        ArgumentCaptor<UserInteraction> captor = ArgumentCaptor.forClass(UserInteraction.class);
        
        when(userInteractionRepository.save(any(UserInteraction.class)))
            .thenAnswer(inv -> {
                UserInteraction arg = inv.getArgument(0);
                arg.setId(1L);
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

        // When
        useCase.execute(validCommand);

        // Then
        verify(userInteractionRepository).save(captor.capture());
        UserInteraction captured = captor.getValue();
        assertEquals(100L, captured.getUserId());
        assertEquals(200L, captured.getProductId());
        assertEquals(InteractionType.VIEW, captured.getInteractionType());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void testExecute_NullUserId_ThrowsException() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.builder()
            .userId(null)
            .productId(200L)
            .interactionType(InteractionType.VIEW)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when productId is null")
    void testExecute_NullProductId_ThrowsException() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.builder()
            .userId(100L)
            .productId(null)
            .interactionType(InteractionType.VIEW)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when interactionType is null")
    void testExecute_NullInteractionType_ThrowsException() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.builder()
            .userId(100L)
            .productId(200L)
            .interactionType(null)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Interaction type is required", exception.getMessage());
    }

    // ===== Business Rule Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when product not found")
    void testExecute_ProductNotFound_ThrowsException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);
        
        TrackUserInteractionCommand command = TrackUserInteractionCommand.view(100L, 999L);

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
    }

    // ===== Repository Interaction Tests =====

    @Test
    @DisplayName("Should verify product exists before saving")
    void testExecute_VerifiesProductFirst() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(1L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        useCase.execute(validCommand);

        // Then
        verify(productRepository).existsById(200L);
        verify(userInteractionRepository).save(any(UserInteraction.class));
    }

    @Test
    @DisplayName("Should not save interaction if product not found")
    void testExecute_DoesNotSaveIfProductNotFound() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(false);

        // When & Then
        assertThrows(CustomBusinessException.class, () -> useCase.execute(validCommand));
        verify(userInteractionRepository, never()).save(any(UserInteraction.class));
    }

    // ===== Result Tests =====

    @Test
    @DisplayName("Should return interaction ID in result")
    void testExecute_ReturnsInteractionId() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(42L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(validCommand);

        // Then
        assertEquals(42L, result.getInteractionId());
    }

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(1L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle add to cart without variant ID")
    void testExecute_AddToCartWithoutVariantId_Success() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.addToCart(100L, 200L, null);
        
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.addToCart(100L, 200L, null);
        savedInteraction.setId(1L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle command with session ID")
    void testExecute_WithSessionId_Success() {
        // Given
        TrackUserInteractionCommand command = TrackUserInteractionCommand.builder()
            .userId(100L)
            .productId(200L)
            .interactionType(InteractionType.VIEW)
            .sessionId("session-123")
            .build();
        
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(1L);
        savedInteraction.setSessionId("session-123");
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        TrackUserInteractionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should call repository save exactly once")
    void testExecute_CallsSaveOnce() {
        // Given
        when(productRepository.existsById(200L)).thenReturn(true);
        
        UserInteraction savedInteraction = UserInteraction.view(100L, 200L);
        savedInteraction.setId(1L);
        savedInteraction.setCreatedAt(LocalDateTime.now());
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(savedInteraction);

        // When
        useCase.execute(validCommand);

        // Then
        verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
    }
}
