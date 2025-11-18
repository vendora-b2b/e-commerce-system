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
 * Unit tests for ReleaseInventoryUseCase using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReleaseInventoryUseCase Unit Tests")
class ReleaseInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ReleaseInventoryUseCase useCase;

    private ReleaseInventoryCommand validCommand;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        validCommand = new ReleaseInventoryCommand(100L, 30);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(100L);
        testInventory.setSupplierId(10L);
        testInventory.setAvailableQuantity(70);
        testInventory.setReservedQuantity(50);
        testInventory.setReorderLevel(20);
        testInventory.setStatus(InventoryStatus.AVAILABLE);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully release inventory with valid data")
    void testExecute_Success() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReleaseInventoryResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Inventory released successfully", result.getMessage());
        assertNull(result.getErrorCode());
        assertEquals(100, testInventory.getAvailableQuantity());
        assertEquals(20, testInventory.getReservedQuantity());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should release all reserved stock")
    void testExecute_ReleaseAllReserved() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 50);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(120, testInventory.getAvailableQuantity());
        assertEquals(0, testInventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should release minimum quantity of 1")
    void testExecute_MinimumQuantity() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 1);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(71, testInventory.getAvailableQuantity());
        assertEquals(49, testInventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should update status after releasing stock")
    void testExecute_UpdatesStatus() {
        // Given
        testInventory.setAvailableQuantity(10);
        testInventory.setReservedQuantity(50);
        testInventory.setStatus(InventoryStatus.LOW_STOCK);

        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 50);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(60, testInventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, testInventory.getStatus());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when product ID is null")
    void testExecute_NullProductId() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(null, 30);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

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
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, null);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Quantity must be greater than 0", result.getMessage());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when quantity is zero")
    void testExecute_ZeroQuantity() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 0);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when quantity is negative")
    void testExecute_NegativeQuantity() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, -10);

        // When
        ReleaseInventoryResult result = useCase.execute(command);

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
        ReleaseInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Inventory not found for product", result.getMessage());
        assertEquals("INVENTORY_NOT_FOUND", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    // ===== Insufficient Reserved Stock Tests =====

    @Test
    @DisplayName("Should fail when insufficient reserved stock to release")
    void testExecute_InsufficientReservedStock() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 60);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Not enough reserved stock to release"));
        assertEquals("RELEASE_FAILED", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when no reserved stock")
    void testExecute_NoReservedStock() {
        // Given
        testInventory.setReservedQuantity(0);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReleaseInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("RELEASE_FAILED", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when requesting one more than reserved")
    void testExecute_RequestOneMoreThanReserved() {
        // Given
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(100L, 51);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // When
        ReleaseInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("RELEASE_FAILED", result.getErrorCode());
    }

    // ===== Repository Call Order Tests =====

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
