package com.example.ecommerce.marketplace.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Order domain entity.
 */
class OrderTest {

    private Order order;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(1L, 101L, 5, 10.0, "Product A"));
        orderItems.add(new OrderItem(2L, 102L, 3, 20.0, "Product B"));

        order = new Order(
            1L,
            "ORD-2024-001",
            5L,
            10L,
            orderItems,
            110.0,
            OrderStatus.PENDING,
            "123 Main St, City",
            LocalDateTime.now(),
            null
        );
    }

    // ===== Order Number Validation Tests =====

    @Test
    @DisplayName("Should validate correct order number")
    void testValidateOrderNumber_ValidOrderNumber() {
        assertTrue(order.validateOrderNumber());
    }

    @Test
    @DisplayName("Should reject null order number")
    void testValidateOrderNumber_NullOrderNumber() {
        order.setOrderNumber(null);
        assertFalse(order.validateOrderNumber());
    }

    @Test
    @DisplayName("Should reject empty order number")
    void testValidateOrderNumber_EmptyOrderNumber() {
        order.setOrderNumber("");
        assertFalse(order.validateOrderNumber());
    }

    @Test
    @DisplayName("Should reject order number shorter than 5 characters")
    void testValidateOrderNumber_TooShort() {
        order.setOrderNumber("ORD1");
        assertFalse(order.validateOrderNumber());
    }

    @Test
    @DisplayName("Should reject order number with special characters")
    void testValidateOrderNumber_SpecialCharacters() {
        order.setOrderNumber("ORD@12345");
        assertFalse(order.validateOrderNumber());
    }

    @Test
    @DisplayName("Should validate order number with hyphens")
    void testValidateOrderNumber_WithHyphens() {
        order.setOrderNumber("ORD-2024-001");
        assertTrue(order.validateOrderNumber());
    }

    // ===== Calculate Total Amount Tests =====

    @Test
    @DisplayName("Should calculate total amount correctly")
    void testCalculateTotalAmount_ValidItems() {
        // 5 * 10.0 + 3 * 20.0 = 50.0 + 60.0 = 110.0
        assertEquals(110.0, order.calculateTotalAmount());
    }

    @Test
    @DisplayName("Should return 0.0 for empty order items")
    void testCalculateTotalAmount_EmptyItems() {
        order.setOrderItems(new ArrayList<>());
        assertEquals(0.0, order.calculateTotalAmount());
    }

    @Test
    @DisplayName("Should return 0.0 for null order items")
    void testCalculateTotalAmount_NullItems() {
        order.setOrderItems(null);
        assertEquals(0.0, order.calculateTotalAmount());
    }

    @Test
    @DisplayName("Should skip items with null quantity or price")
    void testCalculateTotalAmount_SkipInvalidItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(1L, 101L, null, 10.0, "Product A"));
        items.add(new OrderItem(2L, 102L, 5, null, "Product B"));
        items.add(new OrderItem(3L, 103L, 2, 15.0, "Product C"));
        order.setOrderItems(items);
        
        // Only Product C: 2 * 15.0 = 30.0
        assertEquals(30.0, order.calculateTotalAmount());
    }

    @Test
    @DisplayName("Should round total amount to 2 decimal places")
    void testCalculateTotalAmount_Rounding() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(1L, 101L, 3, 10.333, "Product A"));
        order.setOrderItems(items);
        
        // 3 * 10.333 = 30.999, rounded to 31.0
        assertEquals(31.0, order.calculateTotalAmount());
    }

    // ===== Apply Discount Tests =====

    @Test
    @DisplayName("Should apply discount correctly")
    void testApplyDiscount_ValidDiscount() {
        order.setTotalAmount(100.0);
        order.applyDiscount(20.0);
        assertEquals(80.0, order.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw exception for null discount")
    void testApplyDiscount_NullDiscount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.applyDiscount(null);
        });
        assertEquals("Discount amount cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative discount")
    void testApplyDiscount_NegativeDiscount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.applyDiscount(-10.0);
        });
        assertEquals("Discount amount cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for discount exceeding total")
    void testApplyDiscount_ExceedsTotal() {
        order.setTotalAmount(50.0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.applyDiscount(60.0);
        });
        assertEquals("Discount amount cannot exceed total amount", exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate total if null before applying discount")
    void testApplyDiscount_CalculateTotalIfNull() {
        order.setTotalAmount(null);
        order.applyDiscount(10.0);
        assertEquals(100.0, order.getTotalAmount()); // 110.0 - 10.0 = 100.0
    }

    @Test
    @DisplayName("Should round total after discount")
    void testApplyDiscount_Rounding() {
        order.setTotalAmount(100.333);
        order.applyDiscount(0.333);
        assertEquals(100.0, order.getTotalAmount());
    }

    // ===== Minimum Order Quantity Tests =====

    @Test
    @DisplayName("Should validate minimum order quantity is met")
    void testValidateMinimumOrderQuantity_Valid() {
        assertTrue(order.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject empty order items")
    void testValidateMinimumOrderQuantity_EmptyItems() {
        order.setOrderItems(new ArrayList<>());
        assertFalse(order.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject null order items")
    void testValidateMinimumOrderQuantity_NullItems() {
        order.setOrderItems(null);
        assertFalse(order.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should count total quantity across all items")
    void testValidateMinimumOrderQuantity_MultipleItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(1L, 101L, 1, 10.0, "Product A"));
        order.setOrderItems(items);
        assertTrue(order.validateMinimumOrderQuantity());
    }

    // ===== Status Check Tests =====

    @Test
    @DisplayName("Should return true when order is pending")
    void testIsPending_True() {
        order.setStatus(OrderStatus.PENDING);
        assertTrue(order.isPending());
    }

    @Test
    @DisplayName("Should return false when order is not pending")
    void testIsPending_False() {
        order.setStatus(OrderStatus.PROCESSING);
        assertFalse(order.isPending());
    }

    @Test
    @DisplayName("Should return true when order is delivered")
    void testIsDelivered_True() {
        order.setStatus(OrderStatus.DELIVERED);
        assertTrue(order.isDelivered());
    }

    @Test
    @DisplayName("Should return false when order is not delivered")
    void testIsDelivered_False() {
        order.setStatus(OrderStatus.PENDING);
        assertFalse(order.isDelivered());
    }

    // ===== Can Be Cancelled Tests =====

    @Test
    @DisplayName("Should allow cancellation when status is PENDING")
    void testCanBeCancelled_Pending() {
        order.setStatus(OrderStatus.PENDING);
        assertTrue(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should allow cancellation when status is PROCESSING")
    void testCanBeCancelled_Processing() {
        order.setStatus(OrderStatus.PROCESSING);
        assertTrue(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should not allow cancellation when status is SHIPPED")
    void testCanBeCancelled_Shipped() {
        order.setStatus(OrderStatus.SHIPPED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should not allow cancellation when status is DELIVERED")
    void testCanBeCancelled_Delivered() {
        order.setStatus(OrderStatus.DELIVERED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should not allow cancellation when status is CANCELLED")
    void testCanBeCancelled_Cancelled() {
        order.setStatus(OrderStatus.CANCELLED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    @DisplayName("Should return false when status is null")
    void testCanBeCancelled_NullStatus() {
        order.setStatus(null);
        assertFalse(order.canBeCancelled());
    }

    // ===== State Transition Tests =====

    @Test
    @DisplayName("Should mark order as processing from pending")
    void testMarkAsProcessing_FromPending() {
        order.setStatus(OrderStatus.PENDING);
        order.markAsProcessing();
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when marking as processing from non-pending status")
    void testMarkAsProcessing_FromNonPending() {
        order.setStatus(OrderStatus.SHIPPED);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.markAsProcessing();
        });
        assertEquals("Only pending orders can be marked as processing", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark order as shipped from processing")
    void testMarkAsShipped_FromProcessing() {
        order.setStatus(OrderStatus.PROCESSING);
        order.markAsShipped();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when marking as shipped from non-processing status")
    void testMarkAsShipped_FromNonProcessing() {
        order.setStatus(OrderStatus.PENDING);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.markAsShipped();
        });
        assertEquals("Only processing orders can be marked as shipped", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark order as delivered from shipped")
    void testMarkAsDelivered_FromShipped() {
        order.setStatus(OrderStatus.SHIPPED);
        order.setDeliveryDate(null);
        order.markAsDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getDeliveryDate());
    }

    @Test
    @DisplayName("Should throw exception when marking as delivered from non-shipped status")
    void testMarkAsDelivered_FromNonShipped() {
        order.setStatus(OrderStatus.PROCESSING);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.markAsDelivered();
        });
        assertEquals("Only shipped orders can be marked as delivered", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark order as cancelled when allowed")
    void testMarkAsCancelled_Allowed() {
        order.setStatus(OrderStatus.PENDING);
        order.markAsCancelled();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling not allowed")
    void testMarkAsCancelled_NotAllowed() {
        order.setStatus(OrderStatus.SHIPPED);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.markAsCancelled();
        });
        assertTrue(exception.getMessage().contains("Order cannot be cancelled in current status"));
    }

    // ===== Update Order Details Tests =====

    @Test
    @DisplayName("Should update shipping address")
    void testUpdateOrderDetails_ShippingAddress() {
        String newAddress = "456 New St, New City";
        order.updateOrderDetails(newAddress, null);
        assertEquals(newAddress, order.getShippingAddress());
    }

    @Test
    @DisplayName("Should update delivery date")
    void testUpdateOrderDetails_DeliveryDate() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(5);
        order.updateOrderDetails(null, newDate);
        assertEquals(newDate, order.getDeliveryDate());
    }

    @Test
    @DisplayName("Should update both shipping address and delivery date")
    void testUpdateOrderDetails_Both() {
        String newAddress = "456 New St, New City";
        LocalDateTime newDate = LocalDateTime.now().plusDays(5);
        order.updateOrderDetails(newAddress, newDate);
        assertEquals(newAddress, order.getShippingAddress());
        assertEquals(newDate, order.getDeliveryDate());
    }

    @Test
    @DisplayName("Should not update shipping address when null")
    void testUpdateOrderDetails_NullAddress() {
        String originalAddress = order.getShippingAddress();
        order.updateOrderDetails(null, null);
        assertEquals(originalAddress, order.getShippingAddress());
    }

    @Test
    @DisplayName("Should not update shipping address when empty")
    void testUpdateOrderDetails_EmptyAddress() {
        String originalAddress = order.getShippingAddress();
        order.updateOrderDetails("   ", null);
        assertEquals(originalAddress, order.getShippingAddress());
    }

    @Test
    @DisplayName("Should trim whitespace from shipping address")
    void testUpdateOrderDetails_TrimAddress() {
        order.updateOrderDetails("  456 New St  ", null);
        assertEquals("456 New St", order.getShippingAddress());
    }

    // ===== Add Order Item Tests =====

    @Test
    @DisplayName("Should add order item and recalculate total")
    void testAddOrderItem_Success() {
        Order newOrder = new Order();
        newOrder.setOrderItems(new ArrayList<>());
        newOrder.setTotalAmount(0.0);
        
        OrderItem item = new OrderItem(null, 103L, 2, 25.0, "Product C");
        newOrder.addOrderItem(item);
        
        assertEquals(1, newOrder.getOrderItems().size());
        assertEquals(50.0, newOrder.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw exception when adding null item")
    void testAddOrderItem_NullItem() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.addOrderItem(null);
        });
        assertEquals("Order item cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should initialize order items list if null")
    void testAddOrderItem_NullList() {
        order.setOrderItems(null);
        OrderItem item = new OrderItem(null, 103L, 2, 25.0, "Product C");
        order.addOrderItem(item);
        
        assertNotNull(order.getOrderItems());
        assertEquals(1, order.getOrderItems().size());
    }

    // ===== Constructor Tests =====

    @Test
    @DisplayName("Should create order with all fields")
    void testConstructor_AllFields() {
        LocalDateTime orderDate = LocalDateTime.now();
        Order newOrder = new Order(
            2L,
            "ORD-2024-002",
            6L,
            11L,
            orderItems,
            150.0,
            OrderStatus.PROCESSING,
            "789 Test St",
            orderDate,
            null
        );

        assertEquals(2L, newOrder.getId());
        assertEquals("ORD-2024-002", newOrder.getOrderNumber());
        assertEquals(6L, newOrder.getRetailerId());
        assertEquals(11L, newOrder.getSupplierId());
        assertEquals(2, newOrder.getOrderItems().size());
        assertEquals(150.0, newOrder.getTotalAmount());
        assertEquals(OrderStatus.PROCESSING, newOrder.getStatus());
        assertEquals("789 Test St", newOrder.getShippingAddress());
        assertEquals(orderDate, newOrder.getOrderDate());
        assertNull(newOrder.getDeliveryDate());
    }

    @Test
    @DisplayName("Should create order with default constructor")
    void testConstructor_Default() {
        Order newOrder = new Order();
        assertNotNull(newOrder);
        assertNotNull(newOrder.getOrderItems());
        assertTrue(newOrder.getOrderItems().isEmpty());
    }

    @Test
    @DisplayName("Should create defensive copy of order items")
    void testConstructor_DefensiveCopy() {
        List<OrderItem> originalItems = new ArrayList<>();
        originalItems.add(new OrderItem(1L, 101L, 5, 10.0, "Product A"));
        
        Order newOrder = new Order(
            1L, "ORD-001", 5L, 10L, originalItems,
            50.0, OrderStatus.PENDING, "123 St",
            LocalDateTime.now(), null
        );
        
        // Modify original list
        originalItems.add(new OrderItem(2L, 102L, 3, 20.0, "Product B"));
        
        // Order should still have only 1 item
        assertEquals(1, newOrder.getOrderItems().size());
    }
}
