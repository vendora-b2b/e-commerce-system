package com.example.ecommerce.marketplace.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain tests for Order relationships with Retailer and Supplier.
 * 
 * Relationship Analysis:
 * - Order has a retailerId (FK to Retailer) - represents who placed the order
 * - Order has a supplierId (FK to Supplier) - represents who fulfills the order
 * - These are aggregate references (IDs only), following DDD principles
 * - Order is the aggregate root for its own context
 * - Retailer and Supplier are separate aggregates referenced by ID
 * 
 * Business Rules Tested:
 * 1. Order must have a valid retailer ID (the buyer)
 * 2. Order must have a valid supplier ID (the seller)
 * 3. Order maintains referential integrity through IDs
 * 4. Order lifecycle is independent but constrained by these relationships
 */
@DisplayName("Order-Retailer-Supplier Relationship Domain Tests")
class OrderRetailerSupplierRelationshipTest {

    private Order order;
    private List<OrderItem> orderItems;
    private Long retailerId;
    private Long supplierId;

    @BeforeEach
    void setUp() {
        retailerId = 100L;
        supplierId = 200L;

        orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(1L, 101L, 5, 10.0, "Product A"));
        orderItems.add(new OrderItem(2L, 102L, 3, 20.0, "Product B"));

        order = new Order(
            1L,
            "ORD-REL-001",
            retailerId,
            supplierId,
            orderItems,
            110.0,
            OrderStatus.PENDING,
            "123 Main St, City",
            LocalDateTime.now(),
            null
        );
    }

    // ===== Retailer Relationship Tests =====

    @Test
    @DisplayName("Order should maintain retailer ID reference")
    void shouldMaintainRetailerIdReference() {
        // Then
        assertNotNull(order.getRetailerId(), "Order must have a retailer ID");
        assertEquals(retailerId, order.getRetailerId());
    }

    @Test
    @DisplayName("Order should allow updating retailer ID")
    void shouldAllowUpdatingRetailerId() {
        // Given
        Long newRetailerId = 999L;

        // When
        order.setRetailerId(newRetailerId);

        // Then
        assertEquals(newRetailerId, order.getRetailerId());
    }

    @Test
    @DisplayName("Order should be created with specific retailer")
    void shouldBeCreatedWithSpecificRetailer() {
        // Given/When
        Order newOrder = new Order(
            null,
            "ORD-REL-002",
            500L, // Specific retailer
            600L, // Specific supplier
            orderItems,
            100.0,
            OrderStatus.PENDING,
            "Test Address",
            LocalDateTime.now(),
            null
        );

        // Then
        assertEquals(500L, newOrder.getRetailerId());
        assertNotEquals(retailerId, newOrder.getRetailerId());
    }

    @Test
    @DisplayName("Multiple orders can belong to same retailer")
    void multipleOrdersCanBelongToSameRetailer() {
        // Given
        Order order1 = new Order(
            1L, "ORD-REL-003", retailerId, 201L, orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        Order order2 = new Order(
            2L, "ORD-REL-004", retailerId, 202L, orderItems,
            200.0, OrderStatus.PROCESSING, "Address 2", LocalDateTime.now(), null
        );

        // Then
        assertEquals(order1.getRetailerId(), order2.getRetailerId());
        assertEquals(retailerId, order1.getRetailerId());
        assertEquals(retailerId, order2.getRetailerId());
    }

    // ===== Supplier Relationship Tests =====

    @Test
    @DisplayName("Order should maintain supplier ID reference")
    void shouldMaintainSupplierIdReference() {
        // Then
        assertNotNull(order.getSupplierId(), "Order must have a supplier ID");
        assertEquals(supplierId, order.getSupplierId());
    }

    @Test
    @DisplayName("Order should allow updating supplier ID")
    void shouldAllowUpdatingSupplierId() {
        // Given
        Long newSupplierId = 888L;

        // When
        order.setSupplierId(newSupplierId);

        // Then
        assertEquals(newSupplierId, order.getSupplierId());
    }

    @Test
    @DisplayName("Order should be created with specific supplier")
    void shouldBeCreatedWithSpecificSupplier() {
        // Given/When
        Order newOrder = new Order(
            null,
            "ORD-REL-005",
            500L, // Specific retailer
            700L, // Specific supplier
            orderItems,
            100.0,
            OrderStatus.PENDING,
            "Test Address",
            LocalDateTime.now(),
            null
        );

        // Then
        assertEquals(700L, newOrder.getSupplierId());
        assertNotEquals(supplierId, newOrder.getSupplierId());
    }

    @Test
    @DisplayName("Multiple orders can belong to same supplier")
    void multipleOrdersCanBelongToSameSupplier() {
        // Given
        Order order1 = new Order(
            1L, "ORD-REL-006", 101L, supplierId, orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        Order order2 = new Order(
            2L, "ORD-REL-007", 102L, supplierId, orderItems,
            200.0, OrderStatus.PROCESSING, "Address 2", LocalDateTime.now(), null
        );

        // Then
        assertEquals(order1.getSupplierId(), order2.getSupplierId());
        assertEquals(supplierId, order1.getSupplierId());
        assertEquals(supplierId, order2.getSupplierId());
    }

    // ===== Both Relationships Together =====

    @Test
    @DisplayName("Order should maintain both retailer and supplier references")
    void shouldMaintainBothReferences() {
        // Then
        assertNotNull(order.getRetailerId());
        assertNotNull(order.getSupplierId());
        assertEquals(retailerId, order.getRetailerId());
        assertEquals(supplierId, order.getSupplierId());
        assertNotEquals(order.getRetailerId(), order.getSupplierId());
    }

    @Test
    @DisplayName("Order connects retailer to supplier in a transaction")
    void orderConnectsRetailerToSupplier() {
        // Given - Retailer 100 orders from Supplier 200
        Order businessTransaction = new Order(
            null,
            "ORD-REL-008",
            100L, // Buyer (Retailer)
            200L, // Seller (Supplier)
            orderItems,
            150.0,
            OrderStatus.PENDING,
            "Delivery Address",
            LocalDateTime.now(),
            null
        );

        // Then
        assertEquals(100L, businessTransaction.getRetailerId(), "Buyer should be retailer");
        assertEquals(200L, businessTransaction.getSupplierId(), "Seller should be supplier");
        assertNotEquals(businessTransaction.getRetailerId(), businessTransaction.getSupplierId(),
            "Buyer and seller must be different entities");
    }

    @Test
    @DisplayName("Different orders can have different retailer-supplier combinations")
    void differentOrdersCanHaveDifferentCombinations() {
        // Given
        Order order1 = new Order(
            1L, "ORD-REL-009", 101L, 201L, orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        Order order2 = new Order(
            2L, "ORD-REL-010", 102L, 202L, orderItems,
            200.0, OrderStatus.PENDING, "Address 2", LocalDateTime.now(), null
        );
        Order order3 = new Order(
            3L, "ORD-REL-011", 101L, 202L, orderItems,
            300.0, OrderStatus.PENDING, "Address 3", LocalDateTime.now(), null
        );

        // Then
        // Order 1: Retailer 101 -> Supplier 201
        assertEquals(101L, order1.getRetailerId());
        assertEquals(201L, order1.getSupplierId());

        // Order 2: Retailer 102 -> Supplier 202
        assertEquals(102L, order2.getRetailerId());
        assertEquals(202L, order2.getSupplierId());

        // Order 3: Retailer 101 -> Supplier 202 (same retailer, different supplier)
        assertEquals(101L, order3.getRetailerId());
        assertEquals(202L, order3.getSupplierId());
        
        // Verify relationships
        assertEquals(order1.getRetailerId(), order3.getRetailerId(), 
            "Same retailer can order from different suppliers");
        assertNotEquals(order1.getSupplierId(), order3.getSupplierId());
    }

    // ===== Business Scenario Tests =====

    @Test
    @DisplayName("Order lifecycle should preserve retailer and supplier relationships")
    void orderLifecycleShouldPreserveRelationships() {
        // Given - Initial order
        assertEquals(OrderStatus.PENDING, order.getStatus());
        Long originalRetailerId = order.getRetailerId();
        Long originalSupplierId = order.getSupplierId();

        // When - Order goes through lifecycle
        order.markAsProcessing();
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(originalRetailerId, order.getRetailerId());
        assertEquals(originalSupplierId, order.getSupplierId());

        order.markAsShipped();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(originalRetailerId, order.getRetailerId());
        assertEquals(originalSupplierId, order.getSupplierId());

        order.markAsDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        
        // Then - Relationships remain unchanged throughout lifecycle
        assertEquals(originalRetailerId, order.getRetailerId());
        assertEquals(originalSupplierId, order.getSupplierId());
    }

    @Test
    @DisplayName("Order cancellation should preserve retailer and supplier information")
    void orderCancellationShouldPreserveRelationshipInformation() {
        // Given
        Long originalRetailerId = order.getRetailerId();
        Long originalSupplierId = order.getSupplierId();

        // When
        order.markAsCancelled();

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(originalRetailerId, order.getRetailerId(), 
            "Retailer info must be preserved for audit trail");
        assertEquals(originalSupplierId, order.getSupplierId(), 
            "Supplier info must be preserved for audit trail");
    }

    @Test
    @DisplayName("Order data should be complete for reporting purposes")
    void orderDataShouldBeCompleteForReporting() {
        // Then - All key data present for business analytics
        assertNotNull(order.getId(), "Order ID for tracking");
        assertNotNull(order.getOrderNumber(), "Order number for reference");
        assertNotNull(order.getRetailerId(), "Retailer ID for buyer analysis");
        assertNotNull(order.getSupplierId(), "Supplier ID for seller analysis");
        assertNotNull(order.getTotalAmount(), "Amount for revenue reporting");
        assertNotNull(order.getStatus(), "Status for fulfillment tracking");
        assertNotNull(order.getOrderDate(), "Date for temporal analysis");
        assertNotNull(order.getOrderItems(), "Items for product analysis");
        assertFalse(order.getOrderItems().isEmpty(), "Must have at least one item");
    }

    @Test
    @DisplayName("Order should support queries by retailer or supplier")
    void orderShouldSupportQueriesByRetailerOrSupplier() {
        // This test verifies the domain model supports repository queries
        // Actual query implementation is tested in infrastructure layer
        
        // Given - Multiple orders
        List<Order> allOrders = List.of(
            new Order(1L, "ORD-1", 100L, 200L, orderItems, 100.0, 
                OrderStatus.PENDING, "Addr1", LocalDateTime.now(), null),
            new Order(2L, "ORD-2", 100L, 201L, orderItems, 200.0, 
                OrderStatus.PENDING, "Addr2", LocalDateTime.now(), null),
            new Order(3L, "ORD-3", 101L, 200L, orderItems, 300.0, 
                OrderStatus.PENDING, "Addr3", LocalDateTime.now(), null)
        );

        // When - Filter by retailer 100
        List<Order> retailer100Orders = allOrders.stream()
            .filter(o -> o.getRetailerId().equals(100L))
            .toList();

        // Then
        assertEquals(2, retailer100Orders.size());
        assertTrue(retailer100Orders.stream()
            .allMatch(o -> o.getRetailerId().equals(100L)));

        // When - Filter by supplier 200
        List<Order> supplier200Orders = allOrders.stream()
            .filter(o -> o.getSupplierId().equals(200L))
            .toList();

        // Then
        assertEquals(2, supplier200Orders.size());
        assertTrue(supplier200Orders.stream()
            .allMatch(o -> o.getSupplierId().equals(200L)));
    }

    // ===== Null/Edge Case Tests =====

    @Test
    @DisplayName("Order creation should handle null retailer ID")
    void orderCreationShouldHandleNullRetailerId() {
        // Given/When
        Order orderWithNullRetailer = new Order(
            null,
            "ORD-NULL-001",
            null, // Null retailer
            supplierId,
            orderItems,
            100.0,
            OrderStatus.PENDING,
            "Address",
            LocalDateTime.now(),
            null
        );

        // Then - Domain allows null (validation happens at application layer)
        assertNull(orderWithNullRetailer.getRetailerId());
        assertNotNull(orderWithNullRetailer.getSupplierId());
    }

    @Test
    @DisplayName("Order creation should handle null supplier ID")
    void orderCreationShouldHandleNullSupplierId() {
        // Given/When
        Order orderWithNullSupplier = new Order(
            null,
            "ORD-NULL-002",
            retailerId,
            null, // Null supplier
            orderItems,
            100.0,
            OrderStatus.PENDING,
            "Address",
            LocalDateTime.now(),
            null
        );

        // Then - Domain allows null (validation happens at application layer)
        assertNotNull(orderWithNullSupplier.getRetailerId());
        assertNull(orderWithNullSupplier.getSupplierId());
    }

    @Test
    @DisplayName("Order should allow same entity as both retailer and supplier for edge cases")
    void orderShouldAllowSameEntityAsBothRetailerAndSupplier() {
        // This might represent internal orders or special business cases
        
        // Given/When
        Long sameEntityId = 555L;
        Order internalOrder = new Order(
            null,
            "ORD-INTERNAL-001",
            sameEntityId, // Same entity
            sameEntityId, // Same entity
            orderItems,
            100.0,
            OrderStatus.PENDING,
            "Internal Transfer",
            LocalDateTime.now(),
            null
        );

        // Then - Domain allows it (business rules validation at application layer)
        assertEquals(internalOrder.getRetailerId(), internalOrder.getSupplierId());
    }
}
