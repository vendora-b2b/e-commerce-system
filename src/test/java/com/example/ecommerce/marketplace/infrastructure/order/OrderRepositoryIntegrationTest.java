package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Order repository infrastructure layer.
 * Tests JPA entity configuration, database constraints, and entity-domain mapping.
 * Uses H2 in-memory database configured in application-test.properties.
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Order Repository Integration Tests")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(null, 101L, 5, 10.0, "Product A"));
        orderItems.add(new OrderItem(null, 102L, 3, 20.0, "Product B"));

        testOrder = new Order(
            null,
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

    // ===== Save and Retrieve Tests =====

    @Test
    @DisplayName("Should save order and generate ID")
    void testSave_GeneratesId() {
        // When
        Order saved = orderRepository.save(testOrder);

        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertTrue(saved.getId() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should save and retrieve order by ID")
    void testSaveAndFindById_Success() {
        // Given
        Order saved = orderRepository.save(testOrder);

        // When
        Optional<Order> found = orderRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent(), "Order should be found");
        Order foundOrder = found.get();
        
        assertEquals(saved.getId(), foundOrder.getId());
        assertEquals("ORD-2024-001", foundOrder.getOrderNumber());
        assertEquals(5L, foundOrder.getRetailerId());
        assertEquals(10L, foundOrder.getSupplierId());
        assertEquals(110.0, foundOrder.getTotalAmount());
        assertEquals(OrderStatus.PENDING, foundOrder.getStatus());
        assertEquals("123 Main St, City", foundOrder.getShippingAddress());
        assertNotNull(foundOrder.getOrderDate());
        assertNull(foundOrder.getDeliveryDate());
        
        // Verify order items
        assertEquals(2, foundOrder.getOrderItems().size());
    }

    @Test
    @DisplayName("Should return empty optional when order not found")
    void testFindById_NotFound() {
        // When
        Optional<Order> found = orderRepository.findById(999L);

        // Then
        assertFalse(found.isPresent(), "Should return empty optional");
    }

    @Test
    @DisplayName("Should save order items with order")
    void testSave_WithOrderItems() {
        // When
        Order saved = orderRepository.save(testOrder);

        // Then
        assertNotNull(saved.getId());
        assertEquals(2, saved.getOrderItems().size());
        
        OrderItem item1 = saved.getOrderItems().get(0);
        assertEquals(101L, item1.getProductId());
        assertEquals(5, item1.getQuantity());
        assertEquals(10.0, item1.getPrice());
        assertEquals("Product A", item1.getProductName());

        OrderItem item2 = saved.getOrderItems().get(1);
        assertEquals(102L, item2.getProductId());
        assertEquals(3, item2.getQuantity());
        assertEquals(20.0, item2.getPrice());
        assertEquals("Product B", item2.getProductName());
    }

    // ===== Unique Constraint Tests =====

    @Test
    @DisplayName("Should enforce unique order number constraint")
    void testSave_DuplicateOrderNumber_ThrowsException() {
        // Given - Save first order
        orderRepository.save(testOrder);

        // When - Try to save another order with same order number
        List<OrderItem> duplicateItems = new ArrayList<>();
        duplicateItems.add(new OrderItem(null, 201L, 2, 15.0, "Product C"));
        
        Order duplicate = new Order(
            null,
            "ORD-2024-001",  // Same order number
            6L,              // Different retailer
            11L,             // Different supplier
            duplicateItems,
            30.0,
            OrderStatus.PENDING,
            "Different Address",
            LocalDateTime.now(),
            null
        );

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.save(duplicate);
        });
    }

    @Test
    @DisplayName("Should allow different order numbers for different orders")
    void testSave_DifferentOrderNumbers_Success() {
        // Given
        orderRepository.save(testOrder);

        // When
        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(null, 201L, 2, 15.0, "Product C"));
        
        Order order2 = new Order(
            null,
            "ORD-2024-002",  // Different order number
            5L,
            10L,
            items2,
            30.0,
            OrderStatus.PENDING,
            "456 Different St",
            LocalDateTime.now(),
            null
        );
        Order saved2 = orderRepository.save(order2);

        // Then
        assertNotNull(saved2.getId());
        assertNotEquals(testOrder.getId(), saved2.getId());
    }

    // ===== Query Method Tests =====

    @Test
    @DisplayName("Should find order by order number")
    void testFindByOrderNumber_Success() {
        // Given
        orderRepository.save(testOrder);

        // When
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-2024-001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("ORD-2024-001", found.get().getOrderNumber());
    }

    @Test
    @DisplayName("Should return empty when order number not found")
    void testFindByOrderNumber_NotFound() {
        // When
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should check if order number exists")
    void testExistsByOrderNumber() {
        // Given
        orderRepository.save(testOrder);

        // When/Then
        assertTrue(orderRepository.existsByOrderNumber("ORD-2024-001"));
        assertFalse(orderRepository.existsByOrderNumber("ORD-NONEXISTENT"));
    }

    @Test
    @DisplayName("Should find orders by retailer ID")
    void testFindByRetailerId() {
        // Given
        orderRepository.save(testOrder);
        
        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(null, 201L, 1, 50.0, "Product C"));
        Order order2 = new Order(
            null, "ORD-2024-002", 5L, 11L, items2,
            50.0, OrderStatus.PROCESSING, "789 St", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        // When
        List<Order> orders = orderRepository.findByRetailerId(5L);

        // Then
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getRetailerId().equals(5L)));
    }

    @Test
    @DisplayName("Should find orders by supplier ID")
    void testFindBySupplierId() {
        // Given
        orderRepository.save(testOrder);
        
        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(null, 201L, 1, 50.0, "Product C"));
        Order order2 = new Order(
            null, "ORD-2024-002", 6L, 10L, items2,
            50.0, OrderStatus.PROCESSING, "789 St", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        // When
        List<Order> orders = orderRepository.findBySupplierId(10L);

        // Then
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getSupplierId().equals(10L)));
    }

    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus() {
        // Given
        orderRepository.save(testOrder);
        
        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(null, 201L, 1, 50.0, "Product C"));
        Order order2 = new Order(
            null, "ORD-2024-002", 6L, 11L, items2,
            50.0, OrderStatus.PROCESSING, "789 St", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> processingOrders = orderRepository.findByStatus(OrderStatus.PROCESSING);

        // Then
        assertEquals(1, pendingOrders.size());
        assertEquals(OrderStatus.PENDING, pendingOrders.get(0).getStatus());
        
        assertEquals(1, processingOrders.size());
        assertEquals(OrderStatus.PROCESSING, processingOrders.get(0).getStatus());
    }

    // ===== Update Tests =====

    @Test
    @DisplayName("Should update order status")
    void testUpdate_OrderStatus() {
        // Given
        Order saved = orderRepository.save(testOrder);
        assertEquals(OrderStatus.PENDING, saved.getStatus());

        // When
        saved.setStatus(OrderStatus.PROCESSING);
        Order updated = orderRepository.save(saved);

        // Then
        assertEquals(OrderStatus.PROCESSING, updated.getStatus());
        
        // Verify persistence
        Optional<Order> found = orderRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PROCESSING, found.get().getStatus());
    }

    @Test
    @DisplayName("Should update order delivery date")
    void testUpdate_DeliveryDate() {
        // Given
        Order saved = orderRepository.save(testOrder);
        assertNull(saved.getDeliveryDate());

        // When
        LocalDateTime deliveryDate = LocalDateTime.now().plusDays(5);
        saved.setDeliveryDate(deliveryDate);
        Order updated = orderRepository.save(saved);

        // Then
        assertNotNull(updated.getDeliveryDate());
        assertEquals(deliveryDate.toLocalDate(), updated.getDeliveryDate().toLocalDate());
    }

    @Test
    @DisplayName("Should update order shipping address")
    void testUpdate_ShippingAddress() {
        // Given
        Order saved = orderRepository.save(testOrder);

        // When
        saved.setShippingAddress("New Address 456");
        Order updated = orderRepository.save(saved);

        // Then
        assertEquals("New Address 456", updated.getShippingAddress());
    }

    // ===== Delete Tests =====

    @Test
    @DisplayName("Should delete order by ID")
    void testDelete_ById() {
        // Given
        Order saved = orderRepository.save(testOrder);
        Long orderId = saved.getId();
        assertTrue(orderRepository.findById(orderId).isPresent());

        // When
        orderRepository.deleteById(orderId);

        // Then
        assertFalse(orderRepository.findById(orderId).isPresent());
    }

    @Test
    @DisplayName("Should delete order and its items")
    void testDelete_CascadesToOrderItems() {
        // Given
        Order saved = orderRepository.save(testOrder);
        Long orderId = saved.getId();
        assertEquals(2, saved.getOrderItems().size());

        // When
        orderRepository.deleteById(orderId);

        // Then - Order and items should be deleted
        assertFalse(orderRepository.findById(orderId).isPresent());
    }

    // ===== Entity-Domain Mapping Tests =====

    @Test
    @DisplayName("Should correctly map OrderStatus enum")
    void testEntityMapping_OrderStatusEnum() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED);
        
        // When
        Order saved = orderRepository.save(testOrder);
        Optional<Order> found = orderRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.DELIVERED, found.get().getStatus());
    }

    @Test
    @DisplayName("Should preserve order date during save")
    void testEntityMapping_OrderDate() {
        // Given
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        testOrder.setOrderDate(orderDate);

        // When
        Order saved = orderRepository.save(testOrder);

        // Then
        assertNotNull(saved.getOrderDate());
        assertEquals(orderDate.toLocalDate(), saved.getOrderDate().toLocalDate());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle empty order items list")
    void testSave_EmptyOrderItems() {
        // Given
        testOrder.setOrderItems(new ArrayList<>());

        // When
        Order saved = orderRepository.save(testOrder);

        // Then
        assertNotNull(saved.getId());
        assertEquals(0, saved.getOrderItems().size());
    }

    @Test
    @DisplayName("Should handle large total amount")
    void testSave_LargeTotalAmount() {
        // Given
        testOrder.setTotalAmount(999999.99);

        // When
        Order saved = orderRepository.save(testOrder);

        // Then
        assertEquals(999999.99, saved.getTotalAmount());
    }
}
