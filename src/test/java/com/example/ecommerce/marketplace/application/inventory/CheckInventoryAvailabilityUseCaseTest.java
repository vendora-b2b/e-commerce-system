package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CheckInventoryAvailabilityUseCase using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckInventoryAvailabilityUseCase Unit Tests")
class CheckInventoryAvailabilityUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CheckInventoryAvailabilityUseCase useCase;

    private CheckInventoryAvailabilityCommand validCommand;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        validCommand = new CheckInventoryAvailabilityCommand(100L, 30);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(100L);
        testInventory.setSupplierId(10L);
        testInventory.setAvailableQuantity(100);
        testInventory.setReservedQuantity(0);
        testInventory.setReorderLevel(20);
        testInventory.setStatus(InventoryStatus.AVAILABLE);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should return available with sufficient stock")
    void testExecute_AvailableWithSufficientStock() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertTrue(result.isSufficientStock());
        assertEquals(100, result.getAvailableQuantity());
        assertEquals("AVAILABLE", result.getStatus());
        assertEquals("Availability checked successfully", result.getMessage());
        assertNull(result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return available but insufficient stock")
    void testExecute_AvailableButInsufficientStock() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, 150);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertFalse(result.isSufficientStock());
        assertEquals(100, result.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should return LOW_STOCK status with sufficient stock")
    void testExecute_LowStockButSufficient() {
        // Given
        testInventory.setAvailableQuantity(25);
        testInventory.setStatus(InventoryStatus.LOW_STOCK);
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, 20);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertTrue(result.isSufficientStock());
        assertEquals(25, result.getAvailableQuantity());
        assertEquals("LOW_STOCK", result.getStatus());
    }

    @Test
    @DisplayName("Should check availability without requested quantity")
    void testExecute_NoRequestedQuantity() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, null);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertFalse(result.isSufficientStock());  // null quantity means not sufficient
        assertEquals(100, result.getAvailableQuantity());
    }

    // ===== OUT_OF_STOCK Tests =====

    @Test
    @DisplayName("Should return not available when OUT_OF_STOCK")
    void testExecute_OutOfStock() {
        // Given
        testInventory.setAvailableQuantity(0);
        testInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertFalse(result.isAvailable());
        assertFalse(result.isSufficientStock());
        assertEquals(0, result.getAvailableQuantity());
        assertEquals("OUT_OF_STOCK", result.getStatus());
    }

    // ===== DISCONTINUED Tests =====

    @Test
    @DisplayName("Should return not available when DISCONTINUED")
    void testExecute_Discontinued() {
        // Given
        testInventory.setStatus(InventoryStatus.DISCONTINUED);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertFalse(result.isAvailable());
        // Stock check is independent of availability - product has physical stock but can't be ordered
        assertTrue(result.isSufficientStock());
        assertEquals(100, result.getAvailableQuantity());
        assertEquals("DISCONTINUED", result.getStatus());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when product ID is null")
    void testExecute_NullProductId() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(null, 30);

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Product ID is required", result.getMessage());
        assertEquals("INVALID_PRODUCT_ID", result.getErrorCode());

        verify(inventoryRepository, never()).findByProductId(any());
    }

    // ===== Inventory Not Found Tests =====

    @Test
    @DisplayName("Should fail when inventory not found")
    void testExecute_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Inventory not found for product", result.getMessage());
        assertEquals("INVENTORY_NOT_FOUND", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle exact quantity match")
    void testExecute_ExactQuantityMatch() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, 100);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertTrue(result.isSufficientStock());
    }

    @Test
    @DisplayName("Should handle requested quantity of 1")
    void testExecute_RequestedQuantityOne() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, 1);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertTrue(result.isSufficientStock());
    }

    @Test
    @DisplayName("Should handle requested quantity one more than available")
    void testExecute_RequestedOneMoreThanAvailable() {
        // Given
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(100L, 101);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        CheckInventoryAvailabilityResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isAvailable());
        assertFalse(result.isSufficientStock());
    }

    @Test
    @DisplayName("Should be read-only operation - never save")
    void testExecute_ReadOnlyOperation() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        useCase.execute(validCommand);

        // Then
        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }
}
