package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateOrderStatusUseCase.
 */
@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(1L, 101L, 5, 10.0, "Product A"));

        testOrder = new Order(
            1L,
            "ORD-2024-001",
            5L,
            10L,
            orderItems,
            50.0,
            OrderStatus.PENDING,
            "123 Main St",
            LocalDateTime.now(),
            null
        );
    }

    // ===== Successful Status Update Tests =====

    @Test
    @DisplayName("Should update status to PROCESSING from PENDING")
    void testExecute_PendingToProcessing_Success() {
        // Arrange
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getOrderId());
        assertEquals("Order status updated successfully", result.getMessage());
        assertNull(result.getErrorCode());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        assertEquals(OrderStatus.PROCESSING, testOrder.getStatus());
    }

    @Test
    @DisplayName("Should update status to SHIPPED from PROCESSING")
    void testExecute_ProcessingToShipped_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.PROCESSING);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.SHIPPED, testOrder.getStatus());
    }

    @Test
    @DisplayName("Should update status to DELIVERED from SHIPPED")
    void testExecute_ShippedToDelivered_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.DELIVERED, testOrder.getStatus());
        assertNotNull(testOrder.getDeliveryDate());
    }

    @Test
    @DisplayName("Should update status to CANCELLED when allowed")
    void testExecute_ToCancelled_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }

    // ===== Validation Failure Tests =====

    @Test
    @DisplayName("Should fail when order ID is null")
    void testExecute_NullOrderId_Failure() {
        // Arrange
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(null, OrderStatus.PROCESSING);

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order ID is required", result.getMessage());
        assertEquals("INVALID_ORDER_ID", result.getErrorCode());
        
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when new status is null")
    void testExecute_NullStatus_Failure() {
        // Arrange
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, null);

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("New status is required", result.getMessage());
        assertEquals("INVALID_STATUS", result.getErrorCode());
        
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when order not found")
    void testExecute_OrderNotFound_Failure() {
        // Arrange
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(999L, OrderStatus.PROCESSING);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getMessage());
        assertEquals("ORDER_NOT_FOUND", result.getErrorCode());
        
        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any());
    }

    // ===== Invalid State Transition Tests =====

    @Test
    @DisplayName("Should fail when transitioning to PROCESSING from non-PENDING status")
    void testExecute_InvalidTransitionToProcessing_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only pending orders can be marked as processing"));
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when transitioning to SHIPPED from non-PROCESSING status")
    void testExecute_InvalidTransitionToShipped_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only processing orders can be marked as shipped"));
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when transitioning to DELIVERED from non-SHIPPED status")
    void testExecute_InvalidTransitionToDelivered_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.PROCESSING);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only shipped orders can be marked as delivered"));
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when trying to cancel shipped order")
    void testExecute_CannotCancelShipped_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order cannot be cancelled in current status"));
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when trying to cancel delivered order")
    void testExecute_CannotCancelDelivered_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.DELIVERED);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order cannot be cancelled in current status"));
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should fail when transitioning to PENDING status")
    void testExecute_ToPendingStatus() {
        // Arrange
        testOrder.setStatus(OrderStatus.PROCESSING);
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Cannot change order status to PENDING", result.getMessage());
        assertEquals("INVALID_STATUS_TRANSITION", result.getErrorCode());
    }
}
