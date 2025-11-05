package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Infrastructure tests for Order relationships with Retailer and Supplier.
 * 
 * Tests the persistence layer implementation of the relationships between:
 * - Order and Retailer (Many-to-One: many orders can belong to one retailer)
 * - Order and Supplier (Many-to-One: many orders can be fulfilled by one supplier)
 * 
 * Relationship Database Structure:
 * - Order table has retailer_id column (FK to Retailer)
 * - Order table has supplier_id column (FK to Supplier)
 * - Foreign key constraints ensure referential integrity
 * - Indexes on retailer_id and supplier_id for query performance
 * 
 * What We're Testing:
 * 1. Foreign key relationships persist correctly
 * 2. Cascade behaviors (if any)
 * 3. Query methods by retailer and supplier
 * 4. Orphan record handling
 * 5. Multiple orders per retailer/supplier
 * 6. Cross-entity queries and filters
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Order-Retailer-Supplier Infrastructure Relationship Tests")
class OrderRetailerSupplierInfrastructureTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private Retailer retailer1;
    private Retailer retailer2;
    private Supplier supplier1;
    private Supplier supplier2;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        // Create test retailers
        retailer1 = new Retailer(
            null, "Retailer One", "retailer1@test.com", "555-1001", "100 Retailer St",
            null, "First test retailer", "RET-LIC-001", RetailerLoyaltyTier.BRONZE,
            10000.0, 0.0, 0
        );
        retailer1 = retailerRepository.save(retailer1);

        retailer2 = new Retailer(
            null, "Retailer Two", "retailer2@test.com", "555-1002", "200 Retailer Ave",
            null, "Second test retailer", "RET-LIC-002", RetailerLoyaltyTier.SILVER,
            20000.0, 0.0, 0
        );
        retailer2 = retailerRepository.save(retailer2);

        // Create test suppliers
        supplier1 = new Supplier(
            null, "Supplier One", "supplier1@test.com", "555-2001", "100 Supplier Rd",
            null, "First test supplier", "SUP-LIC-001", 4.5, true
        );
        supplier1 = supplierRepository.save(supplier1);

        supplier2 = new Supplier(
            null, "Supplier Two", "supplier2@test.com", "555-2002", "200 Supplier Blvd",
            null, "Second test supplier", "SUP-LIC-002", 4.8, true
        );
        supplier2 = supplierRepository.save(supplier2);

        // Create test order items
        orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(null, 101L, 5, 10.0, "Product A"));
        orderItems.add(new OrderItem(null, 102L, 3, 20.0, "Product B"));
    }

    // ===== Retailer Relationship Persistence Tests =====

    @Test
    @DisplayName("Should persist and retrieve order with retailer relationship")
    void shouldPersistAndRetrieveOrderWithRetailer() {
        // Given
        Order order = new Order(
            null, "ORD-RET-001", retailer1.getId(), supplier1.getId(), orderItems,
            110.0, OrderStatus.PENDING, "123 Main St", LocalDateTime.now(), null
        );

        // When
        Order saved = orderRepository.save(order);
        Order retrieved = orderRepository.findById(saved.getId()).orElseThrow();

        // Then
        assertEquals(retailer1.getId(), retrieved.getRetailerId());
        
        // Verify retailer still exists
        assertTrue(retailerRepository.findById(retailer1.getId()).isPresent());
    }

    @Test
    @DisplayName("Should find all orders for a specific retailer")
    void shouldFindAllOrdersForRetailer() {
        // Given - Create 3 orders, 2 for retailer1, 1 for retailer2
        Order order1 = new Order(
            null, "ORD-RET-002", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        orderRepository.save(order1);

        Order order2 = new Order(
            null, "ORD-RET-003", retailer1.getId(), supplier2.getId(), orderItems,
            200.0, OrderStatus.PROCESSING, "Address 2", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        Order order3 = new Order(
            null, "ORD-RET-004", retailer2.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.SHIPPED, "Address 3", LocalDateTime.now(), null
        );
        orderRepository.save(order3);

        // When
        List<Order> retailer1Orders = orderRepository.findByRetailerId(retailer1.getId());
        List<Order> retailer2Orders = orderRepository.findByRetailerId(retailer2.getId());

        // Then
        assertEquals(2, retailer1Orders.size());
        assertTrue(retailer1Orders.stream().allMatch(o -> o.getRetailerId().equals(retailer1.getId())));

        assertEquals(1, retailer2Orders.size());
        assertTrue(retailer2Orders.stream().allMatch(o -> o.getRetailerId().equals(retailer2.getId())));
    }

    @Test
    @DisplayName("Should find orders by retailer and status")
    void shouldFindOrdersByRetailerAndStatus() {
        // Given - Create multiple orders with different statuses
        Order order1 = new Order(
            null, "ORD-RET-005", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        orderRepository.save(order1);

        Order order2 = new Order(
            null, "ORD-RET-006", retailer1.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.PENDING, "Address 2", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        Order order3 = new Order(
            null, "ORD-RET-007", retailer1.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.PROCESSING, "Address 3", LocalDateTime.now(), null
        );
        orderRepository.save(order3);

        // When
        List<Order> pendingOrders = orderRepository.findByRetailerIdAndStatus(
            retailer1.getId(), OrderStatus.PENDING
        );
        List<Order> processingOrders = orderRepository.findByRetailerIdAndStatus(
            retailer1.getId(), OrderStatus.PROCESSING
        );

        // Then
        assertEquals(2, pendingOrders.size());
        assertTrue(pendingOrders.stream()
            .allMatch(o -> o.getRetailerId().equals(retailer1.getId()) && 
                          o.getStatus() == OrderStatus.PENDING));

        assertEquals(1, processingOrders.size());
        assertTrue(processingOrders.stream()
            .allMatch(o -> o.getRetailerId().equals(retailer1.getId()) && 
                          o.getStatus() == OrderStatus.PROCESSING));
    }

    @Test
    @DisplayName("Should count orders by retailer")
    void shouldCountOrdersByRetailer() {
        // Given - Create orders for different retailers
        orderRepository.save(new Order(
            null, "ORD-RET-008", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Addr 1", LocalDateTime.now(), null
        ));
        orderRepository.save(new Order(
            null, "ORD-RET-009", retailer1.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.PENDING, "Addr 2", LocalDateTime.now(), null
        ));
        orderRepository.save(new Order(
            null, "ORD-RET-010", retailer2.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.PENDING, "Addr 3", LocalDateTime.now(), null
        ));

        // When
        long retailer1Count = orderRepository.countByRetailerId(retailer1.getId());
        long retailer2Count = orderRepository.countByRetailerId(retailer2.getId());

        // Then
        assertEquals(2, retailer1Count);
        assertEquals(1, retailer2Count);
    }

    // ===== Supplier Relationship Persistence Tests =====

    @Test
    @DisplayName("Should persist and retrieve order with supplier relationship")
    void shouldPersistAndRetrieveOrderWithSupplier() {
        // Given
        Order order = new Order(
            null, "ORD-SUP-001", retailer1.getId(), supplier1.getId(), orderItems,
            110.0, OrderStatus.PENDING, "123 Main St", LocalDateTime.now(), null
        );

        // When
        Order saved = orderRepository.save(order);
        Order retrieved = orderRepository.findById(saved.getId()).orElseThrow();

        // Then
        assertEquals(supplier1.getId(), retrieved.getSupplierId());
        
        // Verify supplier still exists
        assertTrue(supplierRepository.findById(supplier1.getId()).isPresent());
    }

    @Test
    @DisplayName("Should find all orders for a specific supplier")
    void shouldFindAllOrdersForSupplier() {
        // Given - Create 3 orders, 2 for supplier1, 1 for supplier2
        Order order1 = new Order(
            null, "ORD-SUP-002", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        orderRepository.save(order1);

        Order order2 = new Order(
            null, "ORD-SUP-003", retailer2.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.PROCESSING, "Address 2", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        Order order3 = new Order(
            null, "ORD-SUP-004", retailer1.getId(), supplier2.getId(), orderItems,
            300.0, OrderStatus.SHIPPED, "Address 3", LocalDateTime.now(), null
        );
        orderRepository.save(order3);

        // When
        List<Order> supplier1Orders = orderRepository.findBySupplierId(supplier1.getId());
        List<Order> supplier2Orders = orderRepository.findBySupplierId(supplier2.getId());

        // Then
        assertEquals(2, supplier1Orders.size());
        assertTrue(supplier1Orders.stream().allMatch(o -> o.getSupplierId().equals(supplier1.getId())));

        assertEquals(1, supplier2Orders.size());
        assertTrue(supplier2Orders.stream().allMatch(o -> o.getSupplierId().equals(supplier2.getId())));
    }

    @Test
    @DisplayName("Should find orders by supplier and status")
    void shouldFindOrdersBySupplierAndStatus() {
        // Given - Create multiple orders with different statuses
        Order order1 = new Order(
            null, "ORD-SUP-005", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Address 1", LocalDateTime.now(), null
        );
        orderRepository.save(order1);

        Order order2 = new Order(
            null, "ORD-SUP-006", retailer1.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.PENDING, "Address 2", LocalDateTime.now(), null
        );
        orderRepository.save(order2);

        Order order3 = new Order(
            null, "ORD-SUP-007", retailer1.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.PROCESSING, "Address 3", LocalDateTime.now(), null
        );
        orderRepository.save(order3);

        // When
        List<Order> pendingOrders = orderRepository.findBySupplierIdAndStatus(
            supplier1.getId(), OrderStatus.PENDING
        );
        List<Order> processingOrders = orderRepository.findBySupplierIdAndStatus(
            supplier1.getId(), OrderStatus.PROCESSING
        );

        // Then
        assertEquals(2, pendingOrders.size());
        assertTrue(pendingOrders.stream()
            .allMatch(o -> o.getSupplierId().equals(supplier1.getId()) && 
                          o.getStatus() == OrderStatus.PENDING));

        assertEquals(1, processingOrders.size());
        assertTrue(processingOrders.stream()
            .allMatch(o -> o.getSupplierId().equals(supplier1.getId()) && 
                          o.getStatus() == OrderStatus.PROCESSING));
    }

    @Test
    @DisplayName("Should count orders by supplier")
    void shouldCountOrdersBySupplier() {
        // Given - Create orders for different suppliers
        orderRepository.save(new Order(
            null, "ORD-SUP-008", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Addr 1", LocalDateTime.now(), null
        ));
        orderRepository.save(new Order(
            null, "ORD-SUP-009", retailer1.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.PENDING, "Addr 2", LocalDateTime.now(), null
        ));
        orderRepository.save(new Order(
            null, "ORD-SUP-010", retailer1.getId(), supplier2.getId(), orderItems,
            300.0, OrderStatus.PENDING, "Addr 3", LocalDateTime.now(), null
        ));

        // When
        long supplier1Count = orderRepository.countBySupplierId(supplier1.getId());
        long supplier2Count = orderRepository.countBySupplierId(supplier2.getId());

        // Then
        assertEquals(2, supplier1Count);
        assertEquals(1, supplier2Count);
    }

    // ===== Combined Relationship Tests =====

    @Test
    @DisplayName("Should support complex queries across both relationships")
    void shouldSupportComplexQueriesAcrossBothRelationships() {
        // Given - Create orders with various combinations
        // Retailer1 -> Supplier1
        orderRepository.save(new Order(
            null, "ORD-BOTH-001", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.PENDING, "Addr 1", LocalDateTime.now(), null
        ));
        // Retailer1 -> Supplier2
        orderRepository.save(new Order(
            null, "ORD-BOTH-002", retailer1.getId(), supplier2.getId(), orderItems,
            200.0, OrderStatus.PENDING, "Addr 2", LocalDateTime.now(), null
        ));
        // Retailer2 -> Supplier1
        orderRepository.save(new Order(
            null, "ORD-BOTH-003", retailer2.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.PENDING, "Addr 3", LocalDateTime.now(), null
        ));
        // Retailer2 -> Supplier2
        orderRepository.save(new Order(
            null, "ORD-BOTH-004", retailer2.getId(), supplier2.getId(), orderItems,
            400.0, OrderStatus.PENDING, "Addr 4", LocalDateTime.now(), null
        ));

        // When - Query by retailer1
        List<Order> retailer1Orders = orderRepository.findByRetailerId(retailer1.getId());
        
        // Then - Should get orders from both suppliers
        assertEquals(2, retailer1Orders.size());
        assertTrue(retailer1Orders.stream().anyMatch(o -> o.getSupplierId().equals(supplier1.getId())));
        assertTrue(retailer1Orders.stream().anyMatch(o -> o.getSupplierId().equals(supplier2.getId())));

        // When - Query by supplier1
        List<Order> supplier1Orders = orderRepository.findBySupplierId(supplier1.getId());
        
        // Then - Should get orders from both retailers
        assertEquals(2, supplier1Orders.size());
        assertTrue(supplier1Orders.stream().anyMatch(o -> o.getRetailerId().equals(retailer1.getId())));
        assertTrue(supplier1Orders.stream().anyMatch(o -> o.getRetailerId().equals(retailer2.getId())));
    }

    @Test
    @DisplayName("Should maintain relationships through order lifecycle")
    void shouldMaintainRelationshipsThroughOrderLifecycle() {
        // Given
        Order order = new Order(
            null, "ORD-LIFE-001", retailer1.getId(), supplier1.getId(), orderItems,
            110.0, OrderStatus.PENDING, "123 Main St", LocalDateTime.now(), null
        );
        Order saved = orderRepository.save(order);
        Long orderId = saved.getId();

        // When - Update order status
        saved.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(saved);

        // Then - Relationships preserved
        Order processing = orderRepository.findById(orderId).orElseThrow();
        assertEquals(retailer1.getId(), processing.getRetailerId());
        assertEquals(supplier1.getId(), processing.getSupplierId());

        // When - Mark as shipped
        processing.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(processing);

        // Then - Relationships still preserved
        Order shipped = orderRepository.findById(orderId).orElseThrow();
        assertEquals(retailer1.getId(), shipped.getRetailerId());
        assertEquals(supplier1.getId(), shipped.getSupplierId());

        // When - Mark as delivered
        shipped.setStatus(OrderStatus.DELIVERED);
        shipped.setDeliveryDate(LocalDateTime.now());
        orderRepository.save(shipped);

        // Then - Relationships still preserved
        Order delivered = orderRepository.findById(orderId).orElseThrow();
        assertEquals(retailer1.getId(), delivered.getRetailerId());
        assertEquals(supplier1.getId(), delivered.getSupplierId());
        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());
    }

    @Test
    @DisplayName("Should preserve relationships when order is cancelled")
    void shouldPreserveRelationshipsWhenOrderCancelled() {
        // Given
        Order order = new Order(
            null, "ORD-CANCEL-001", retailer1.getId(), supplier1.getId(), orderItems,
            110.0, OrderStatus.PENDING, "123 Main St", LocalDateTime.now(), null
        );
        Order saved = orderRepository.save(order);

        // When - Cancel order
        saved.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(saved);

        // Then - Relationships preserved for audit trail
        Order cancelled = orderRepository.findById(saved.getId()).orElseThrow();
        assertEquals(retailer1.getId(), cancelled.getRetailerId());
        assertEquals(supplier1.getId(), cancelled.getSupplierId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    // ===== Business Analytics Scenarios =====

    @Test
    @DisplayName("Should support retailer purchase history analysis")
    void shouldSupportRetailerPurchaseHistoryAnalysis() {
        // Given - Retailer1 has multiple orders across time
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(new Order(
            null, "ORD-HIST-001", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.DELIVERED, "Addr", now.minusDays(30), now.minusDays(25)
        ));
        orderRepository.save(new Order(
            null, "ORD-HIST-002", retailer1.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.DELIVERED, "Addr", now.minusDays(15), now.minusDays(10)
        ));
        orderRepository.save(new Order(
            null, "ORD-HIST-003", retailer1.getId(), supplier2.getId(), orderItems,
            300.0, OrderStatus.PENDING, "Addr", now, null
        ));

        // When - Analyze retailer's orders
        List<Order> allOrders = orderRepository.findByRetailerId(retailer1.getId());
        List<Order> deliveredOrders = orderRepository.findByRetailerIdAndStatus(
            retailer1.getId(), OrderStatus.DELIVERED
        );

        // Then - Can calculate metrics
        assertEquals(3, allOrders.size());
        assertEquals(2, deliveredOrders.size());
        
        double totalSpent = allOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        assertEquals(600.0, totalSpent);

        double deliveredValue = deliveredOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        assertEquals(300.0, deliveredValue);
    }

    @Test
    @DisplayName("Should support supplier sales analysis")
    void shouldSupportSupplierSalesAnalysis() {
        // Given - Supplier1 has orders from multiple retailers
        orderRepository.save(new Order(
            null, "ORD-SALES-001", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.DELIVERED, "Addr", LocalDateTime.now(), LocalDateTime.now()
        ));
        orderRepository.save(new Order(
            null, "ORD-SALES-002", retailer2.getId(), supplier1.getId(), orderItems,
            200.0, OrderStatus.DELIVERED, "Addr", LocalDateTime.now(), LocalDateTime.now()
        ));
        orderRepository.save(new Order(
            null, "ORD-SALES-003", retailer1.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.PROCESSING, "Addr", LocalDateTime.now(), null
        ));

        // When - Analyze supplier's orders
        List<Order> allOrders = orderRepository.findBySupplierId(supplier1.getId());
        List<Order> completedOrders = orderRepository.findBySupplierIdAndStatus(
            supplier1.getId(), OrderStatus.DELIVERED
        );

        // Then - Can calculate metrics
        assertEquals(3, allOrders.size());
        assertEquals(2, completedOrders.size());
        
        double totalRevenue = allOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        assertEquals(600.0, totalRevenue);

        double completedRevenue = completedOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        assertEquals(300.0, completedRevenue);

        // Count unique retailers
        long uniqueRetailers = allOrders.stream()
            .map(Order::getRetailerId)
            .distinct()
            .count();
        assertEquals(2, uniqueRetailers);
    }

    @Test
    @DisplayName("Should support marketplace-wide analytics")
    void shouldSupportMarketplaceWideAnalytics() {
        // Given - Multiple orders across retailers and suppliers
        orderRepository.save(new Order(
            null, "ORD-MKT-001", retailer1.getId(), supplier1.getId(), orderItems,
            100.0, OrderStatus.DELIVERED, "Addr", LocalDateTime.now(), LocalDateTime.now()
        ));
        orderRepository.save(new Order(
            null, "ORD-MKT-002", retailer1.getId(), supplier2.getId(), orderItems,
            200.0, OrderStatus.DELIVERED, "Addr", LocalDateTime.now(), LocalDateTime.now()
        ));
        orderRepository.save(new Order(
            null, "ORD-MKT-003", retailer2.getId(), supplier1.getId(), orderItems,
            300.0, OrderStatus.DELIVERED, "Addr", LocalDateTime.now(), LocalDateTime.now()
        ));
        orderRepository.save(new Order(
            null, "ORD-MKT-004", retailer2.getId(), supplier2.getId(), orderItems,
            400.0, OrderStatus.PENDING, "Addr", LocalDateTime.now(), null
        ));

        // When - Analyze entire marketplace
        List<Order> allOrders = orderRepository.findAll();

        // Then - Can calculate marketplace metrics
        assertEquals(4, allOrders.size());

        // Total marketplace volume
        double totalVolume = allOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        assertEquals(1000.0, totalVolume);

        // Unique retailers count
        long uniqueRetailers = allOrders.stream()
            .map(Order::getRetailerId)
            .distinct()
            .count();
        assertEquals(2, uniqueRetailers);

        // Unique suppliers count
        long uniqueSuppliers = allOrders.stream()
            .map(Order::getSupplierId)
            .distinct()
            .count();
        assertEquals(2, uniqueSuppliers);

        // Completion rate
        long completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();
        double completionRate = (double) completedOrders / allOrders.size();
        assertEquals(0.75, completionRate); // 3 out of 4
    }
}
