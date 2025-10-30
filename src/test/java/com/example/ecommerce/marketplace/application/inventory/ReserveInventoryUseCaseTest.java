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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReserveInventoryUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReserveInventoryUseCase Unit Tests")
class ReserveInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ReserveInventoryUseCase useCase;

    private ReserveInventoryCommand validCommand;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        validCommand = new ReserveInventoryCommand(100L, 30);

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
    @DisplayName("Should successfully reserve inventory with valid data")
    void testExecute_Success() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReserveInventoryResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Inventory reserved successfully", result.getMessage());
        assertNull(result.getErrorCode());
        assertEquals(70, testInventory.getAvailableQuantity());
        assertEquals(30, testInventory.getReservedQuantity());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should reserve all available stock")
    void testExecute_ReserveAllStock() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 100);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(0, testInventory.getAvailableQuantity());
        assertEquals(100, testInventory.getReservedQuantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, testInventory.getStatus());
    }

    @Test
    @DisplayName("Should update status to LOW_STOCK after reservation")
    void testExecute_UpdatesStatusToLowStock() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 85);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(15, testInventory.getAvailableQuantity());
        assertEquals(InventoryStatus.LOW_STOCK, testInventory.getStatus());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when product ID is null")
    void testExecute_NullProductId() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(null, 30);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Product ID is required", result.getMessage());
        assertEquals("INVALID_PRODUCT_ID", result.getErrorCode());

        verify(inventoryRepository, never()).findByProductId(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when quantity is null")
    void testExecute_NullQuantity() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, null);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Quantity must be greater than 0", result.getMessage());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());

        verify(inventoryRepository, never()).findByProductId(any());
    }

    @Test
    @DisplayName("Should fail when quantity is zero")
    void testExecute_ZeroQuantity() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 0);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when quantity is negative")
    void testExecute_NegativeQuantity() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, -10);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    // ===== Inventory Not Found Tests =====

    @Test
    @DisplayName("Should fail when inventory not found")
    void testExecute_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        // When
        ReserveInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Inventory not found for product", result.getMessage());
        assertEquals("INVENTORY_NOT_FOUND", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    // ===== Product Availability Tests =====

    @Test
    @DisplayName("Should fail when product is OUT_OF_STOCK")
    void testExecute_ProductOutOfStock() {
        // Given
        testInventory.setAvailableQuantity(0);
        testInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReserveInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not available for ordering"));
        assertEquals("PRODUCT_NOT_AVAILABLE", result.getErrorCode());

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when product is DISCONTINUED")
    void testExecute_ProductDiscontinued() {
        // Given
        testInventory.setStatus(InventoryStatus.DISCONTINUED);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReserveInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("PRODUCT_NOT_AVAILABLE", result.getErrorCode());
        assertTrue(result.getMessage().contains("DISCONTINUED"));
    }

    @Test
    @DisplayName("Should succeed when product is LOW_STOCK but has sufficient quantity")
    void testExecute_LowStockWithSufficientQuantity() {
        // Given
        testInventory.setAvailableQuantity(50);
        testInventory.setStatus(InventoryStatus.LOW_STOCK);
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 30);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(20, testInventory.getAvailableQuantity());
    }

    // ===== Insufficient Stock Tests =====

    @Test
    @DisplayName("Should fail when insufficient stock available")
    void testExecute_InsufficientStock() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 150);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Insufficient stock"));
        assertTrue(result.getMessage().contains("Available: 100"));
        assertTrue(result.getMessage().contains("Requested: 150"));
        assertEquals("INSUFFICIENT_STOCK", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when requesting one more than available")
    void testExecute_RequestOneMoreThanAvailable() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 101);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INSUFFICIENT_STOCK", result.getErrorCode());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should reserve minimum quantity of 1")
    void testExecute_MinimumQuantity() {
        // Given
        ReserveInventoryCommand command = new ReserveInventoryCommand(100L, 1);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReserveInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(99, testInventory.getAvailableQuantity());
        assertEquals(1, testInventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(inventoryRepository);
        inOrder.verify(inventoryRepository).findByProductId(100L);
        inOrder.verify(inventoryRepository).save(any(Inventory.class));
    }
}
