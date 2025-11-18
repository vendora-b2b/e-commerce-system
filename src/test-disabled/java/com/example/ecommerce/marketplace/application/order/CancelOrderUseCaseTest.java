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
 * Unit tests for CancelOrderUseCase.
 */
@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CancelOrderUseCase cancelOrderUseCase;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(1L, 101L, null, 5, 10.0, "Product A"));

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

    // ===== Successful Cancellation Tests =====

    @Test
    @DisplayName("Should cancel PENDING order successfully")
    void testExecute_CancelPendingOrder_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getOrderId());
        assertEquals("Order cancelled successfully", result.getMessage());
        assertNull(result.getErrorCode());
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should cancel PROCESSING order successfully")
    void testExecute_CancelProcessingOrder_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.PROCESSING);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }

    // ===== Validation Failure Tests =====

    @Test
    @DisplayName("Should fail when order ID is null")
    void testExecute_NullOrderId_Failure() {
        // Arrange
        CancelOrderCommand command = new CancelOrderCommand(null);

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order ID is required", result.getMessage());
        assertEquals("INVALID_ORDER_ID", result.getErrorCode());
        
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when order not found")
    void testExecute_OrderNotFound_Failure() {
        // Arrange
        CancelOrderCommand command = new CancelOrderCommand(999L);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getMessage());
        assertEquals("ORDER_NOT_FOUND", result.getErrorCode());
        
        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any());
    }

    // ===== Cannot Cancel Tests =====

    @Test
    @DisplayName("Should fail when trying to cancel SHIPPED order")
    void testExecute_CannotCancelShipped_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order cannot be cancelled"));
        assertEquals("CANNOT_CANCEL_ORDER", result.getErrorCode());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when trying to cancel DELIVERED order")
    void testExecute_CannotCancelDelivered_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.DELIVERED);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order cannot be cancelled"));
        assertEquals("CANNOT_CANCEL_ORDER", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when trying to cancel already CANCELLED order")
    void testExecute_CannotCancelCancelled_Failure() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order cannot be cancelled"));
        assertEquals("CANNOT_CANCEL_ORDER", result.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should verify canBeCancelled is called before cancellation")
    void testExecute_VerifyCanBeCancelledCheck() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        CancelOrderCommand command = new CancelOrderCommand(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        // Verify the order was actually cancelled (canBeCancelled returned true)
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }
}
