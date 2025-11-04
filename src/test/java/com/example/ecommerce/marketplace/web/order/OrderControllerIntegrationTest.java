package com.example.ecommerce.marketplace.web.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OrderController using real HTTP requests.
 * 
 * Test Strategy:
 * 1. Uses @SpringBootTest to start the full Spring Boot application with random port
 * 2. Makes real HTTP requests via TestRestTemplate to test complete request/response flow
 * 3. Uses in-memory H2 database (configured in application-test.properties)
 * 4. Sets up test data in @BeforeEach to ensure consistent state for each test
 * 5. Tests all controller endpoints: POST, GET, PUT, DELETE
 * 6. Validates both success and error scenarios
 * 
 * Test Data Setup:
 * - Creates a test supplier and retailer before each test with unique IDs
 * - These are required for order creation as foreign key constraints
 * - Unique business licenses prevent constraint violations between tests
 * - Database is cleaned after each test via @DirtiesContext
 * 
 * What We're Testing:
 * - HTTP request/response handling
 * - Request validation (Jakarta Bean Validation)
 * - JSON serialization/deserialization
 * - Controller → Use Case → Repository → Database flow
 * - Error handling and HTTP status codes
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDataSetupService testDataSetupService;

    private String baseUrl;
    private Long testSupplierId;
    private Long testRetailerId;
    private Long testProductId1;
    private Long testProductId2;

    /**
     * Setup test data before each test.
     * Uses a separate transactional service to create and commit test data
     * before the test runs, ensuring entities are visible to HTTP requests.
     */
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/orders";

        // Create test data in a separate transaction that gets committed
        TestDataSetupService.TestData testData = testDataSetupService.createTestData();
        testSupplierId = testData.supplierId();
        testRetailerId = testData.retailerId();
        testProductId1 = testData.productId1();
        testProductId2 = testData.productId2();
    }

    /**
     * Test Case: Successfully place a new order
     * 
     * Given: Valid order request with all required fields
     * When: POST request is made to /api/v1/orders
     * Then: 
     *   - Response status is 201 CREATED
     *   - Response body contains complete order details
     *   - Order has correct data from request
     *   - Order status is PENDING
     *   - Total amount is calculated correctly (10 * 29.99 = 299.90)
     *   - Order is persisted in database
     */
    @Test
    @DisplayName("POST /api/v1/orders - Should successfully place order with valid data")
    void shouldPlaceOrder() {
        // given
        String jsonRequest = """
            {
                "orderNumber": "ORD-TEST-001",
                "retailerId": %d,
                "supplierId": %d,
                "orderItems": [
                    {
                        "productId": %d,
                        "quantity": 10,
                        "price": 29.99,
                        "productName": "Test Product A"
                    }
                ],
                "shippingAddress": "789 Test Street, Test City, TC 12345",
                "orderDate": "2025-11-04T10:00:00"
            }
            """.formatted(testRetailerId, testSupplierId, testProductId1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("ORD-TEST-001");
        assertThat(response.getBody()).contains("PENDING");
        assertThat(response.getBody()).contains("299.9"); // 10 * 29.99
        assertThat(response.getBody()).contains("Test Product A");
        assertThat(response.getBody()).contains("789 Test Street");

        // Verify order was saved to database
        assertThat(orderRepository.findAll()).hasSize(1);
    }

    /**
     * Test Case: Reject order with missing required fields
     * 
     * Given: Order request missing required field (orderNumber)
     * When: POST request is made to /api/v1/orders
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Request is rejected by Jakarta Bean Validation
     *   - No order is created in database
     */
    @Test
    @DisplayName("POST /api/v1/orders - Should reject order with missing required fields")
    void shouldRejectOrderWithMissingFields() {
        // given - missing orderNumber
        String jsonRequest = """
            {
                "retailerId": %d,
                "supplierId": %d,
                "orderItems": [
                    {
                        "productId": %d,
                        "quantity": 10,
                        "price": 29.99,
                        "productName": "Test Product"
                    }
                ],
                "shippingAddress": "789 Test Street",
                "orderDate": "2025-11-04T10:00:00"
            }
            """.formatted(testRetailerId, testSupplierId, testProductId1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(orderRepository.findAll()).isEmpty();
    }

    /**
     * Test Case: Reject order with empty order items
     * 
     * Given: Order request with empty orderItems array
     * When: POST request is made to /api/v1/orders
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Validation fails on @NotEmpty constraint
     *   - No order is created in database
     */
    @Test
    @DisplayName("POST /api/v1/orders - Should reject order with empty order items")
    void shouldRejectOrderWithEmptyItems() {
        // given - empty orderItems
        String jsonRequest = """
            {
                "orderNumber": "ORD-TEST-002",
                "retailerId": %d,
                "supplierId": %d,
                "orderItems": [],
                "shippingAddress": "789 Test Street",
                "orderDate": "2025-11-04T10:00:00"
            }
            """.formatted(testRetailerId, testSupplierId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(orderRepository.findAll()).isEmpty();
    }

    /**
     * Test Case: Successfully retrieve an existing order
     * 
     * Given: An order exists in the database
     * When: GET request is made to /api/v1/orders/{id}
     * Then: 
     *   - Response status is 200 OK
     *   - Response body contains complete order details
     *   - All order data matches what was saved
     */
    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should retrieve existing order")
    void shouldGetOrderById() {
        // given - create an order first
        Order order = createTestOrder("ORD-GET-001");
        Long orderId = orderRepository.save(order).getId();

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/" + orderId,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("ORD-GET-001");
        assertThat(response.getBody()).contains("PENDING");
        assertThat(response.getBody()).contains(orderId.toString());
    }

    /**
     * Test Case: Return 404 for non-existent order
     * 
     * Given: Order ID does not exist in database
     * When: GET request is made to /api/v1/orders/{id}
     * Then: 
     *   - Response status is 404 NOT_FOUND
     */
    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should return 404 for non-existent order")
    void shouldReturn404ForNonExistentOrder() {
        // given - non-existent ID
        Long nonExistentId = 99999L;

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/" + nonExistentId,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Test Case: Successfully update order status
     * 
     * Given: An order exists with PENDING status
     * When: PUT request is made to /api/v1/orders/{id}/status with new status
     * Then: 
     *   - Response status is 200 OK
     *   - Response body contains updated order
     *   - Order status is changed to PROCESSING
     *   - Order is updated in database
     */
    @Test
    @DisplayName("PUT /api/v1/orders/{id}/status - Should update order status")
    void shouldUpdateOrderStatus() {
        // given - create a pending order
        Order order = createTestOrder("ORD-UPDATE-001");
        Long orderId = orderRepository.save(order).getId();

        String jsonRequest = """
            {
                "newStatus": "PROCESSING"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + orderId + "/status",
            HttpMethod.PUT,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("PROCESSING");

        // Verify database update
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    /**
     * Test Case: Reject invalid status update
     * 
     * Given: Order with PENDING status
     * When: PUT request attempts to change to DELIVERED (skipping PROCESSING and SHIPPED)
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Status transition rules are enforced
     *   - Order status remains PENDING
     */
    @Test
    @DisplayName("PUT /api/v1/orders/{id}/status - Should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        // given - create a pending order
        Order order = createTestOrder("ORD-INVALID-001");
        Long orderId = orderRepository.save(order).getId();

        // Try to jump from PENDING to DELIVERED (invalid transition)
        String jsonRequest = """
            {
                "newStatus": "DELIVERED"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + orderId + "/status",
            HttpMethod.PUT,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify status unchanged
        Order unchangedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    /**
     * Test Case: Successfully cancel an order
     * 
     * Given: Order exists with PENDING status (can be cancelled)
     * When: DELETE request is made to /api/v1/orders/{id}
     * Then: 
     *   - Response status is 200 OK
     *   - Response body contains cancelled order
     *   - Order status is CANCELLED
     *   - Order is updated in database
     */
    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Should cancel order successfully")
    void shouldCancelOrder() {
        // given - create a pending order
        Order order = createTestOrder("ORD-CANCEL-001");
        Long orderId = orderRepository.save(order).getId();

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + orderId,
            HttpMethod.DELETE,
            null,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("CANCELLED");

        // Verify database update
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    /**
     * Test Case: Reject cancellation of shipped order
     * 
     * Given: Order with SHIPPED status (cannot be cancelled)
     * When: DELETE request is made to /api/v1/orders/{id}
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Cancellation business rules are enforced
     *   - Order status remains SHIPPED
     */
    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Should reject cancellation of shipped order")
    void shouldRejectCancellationOfShippedOrder() {
        // given - create a shipped order (cannot be cancelled)
        Order order = createTestOrder("ORD-SHIPPED-001");
        order.setStatus(OrderStatus.SHIPPED);
        Long orderId = orderRepository.save(order).getId();

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + orderId,
            HttpMethod.DELETE,
            null,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify status unchanged
        Order unchangedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    /**
     * Test Case: Place order with multiple items and verify total calculation
     * 
     * Given: Order request with multiple different items
     * When: POST request is made to /api/v1/orders
     * Then: 
     *   - Response status is 201 CREATED
     *   - Total amount is correctly calculated (sum of all item subtotals)
     *   - All items are included in response
     */
    @Test
    @DisplayName("POST /api/v1/orders - Should correctly calculate total for multiple items")
    void shouldCalculateTotalForMultipleItems() {
        // given - order with multiple items
        // Item 1: 5 * 10.00 = 50.00
        // Item 2: 3 * 25.50 = 76.50
        // Total: 126.50
        String jsonRequest = """
            {
                "orderNumber": "ORD-MULTI-001",
                "retailerId": %d,
                "supplierId": %d,
                "orderItems": [
                    {
                        "productId": %d,
                        "quantity": 5,
                        "price": 10.00,
                        "productName": "Product A"
                    },
                    {
                        "productId": %d,
                        "quantity": 3,
                        "price": 25.50,
                        "productName": "Product B"
                    }
                ],
                "shippingAddress": "789 Test Street",
                "orderDate": "2025-11-04T10:00:00"
            }
            """.formatted(testRetailerId, testSupplierId, testProductId1, testProductId2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("126.5"); // Total amount
        assertThat(response.getBody()).contains("Product A");
        assertThat(response.getBody()).contains("Product B");
    }

    /**
     * Helper method to create a test order with minimal required data.
     * Used by multiple test methods to set up test fixtures.
     * 
     * @param orderNumber unique order number for the test
     * @return Order entity with test data
     */
    private Order createTestOrder(String orderNumber) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setRetailerId(testRetailerId);
        order.setSupplierId(testSupplierId);
        order.setShippingAddress("Test Address");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(100.0);
        order.setOrderDate(java.time.LocalDateTime.now());
        return order;
    }
}
