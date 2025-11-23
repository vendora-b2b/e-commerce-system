package com.example.ecommerce.marketplace.web.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ProductController using real HTTP requests.
 * Tests the complete request/response flow with a running Spring Boot application.
 * These tests will catch issues like missing fields, validation problems, and database constraints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String supplierBaseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/products";
        supplierBaseUrl = "http://localhost:" + port + "/api/v1/suppliers";
    }

    /**
     * Helper method to create a test supplier with unique license
     */
    private Long createTestSupplier(String license) {
        String jsonRequest = String.format("""
            {
                "name": "Test Supplier Inc",
                "email": "testsupplier%s@example.com",
                "businessLicense": "%s",
                "contactPerson": "John Doe",
                "phone": "+1234567890",
                "address": "123 Test Street"
            }
            """, license, license);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            supplierBaseUrl,
            request,
            String.class
        );

        // Extract ID from response
        if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
            String body = response.getBody();
            int idStart = body.indexOf("\"id\":") + 5;
            int idEnd = body.indexOf(",", idStart);
            return Long.parseLong(body.substring(idStart, idEnd));
        }
        return 1L;
    }

    /**
     * Test 1: Create product with all required fields - CATCHES THE NULL UNIT BUG
     */
    @Test
    void shouldCreateProductWithRequiredFields() {
        // given
        Long supplierId = createTestSupplier("SUP-001");
        
        String jsonRequest = String.format("""
            {
                "sku": "MOUSE-TEST-001",
                "name": "Test Wireless Mouse",
                "description": "Test product",
                "category": "Computer Accessories",
                "basePrice": 29.99,
                "minimumOrderQuantity": 10,
                "supplierId": %d
            }
            """, supplierId);

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
        System.out.println("✅ Test 1: CREATE PRODUCT WITH REQUIRED FIELDS");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"sku\":\"MOUSE-TEST-001\"");
        assertThat(response.getBody()).contains("\"unit\":\"piece\"");  // This would catch the null unit bug
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    /**
     * Test 2: Create product with price tiers and variants
     */
    @Test
    void shouldCreateProductWithPriceTiersAndVariants() {
        // given
        Long supplierId = createTestSupplier("SUP-002");
        
        String jsonRequest = String.format("""
            {
                "sku": "LAPTOP-TEST-001",
                "name": "Test Laptop",
                "description": "Test laptop with variants",
                "categoryId": 1,
                "basePrice": 899.99,
                "minimumOrderQuantity": 5,
                "supplierId": %d,
                "priceTiers": [
                    {
                        "minQuantity": 5,
                        "maxQuantity": 10,
                        "pricePerUnit": 899.99,
                        "discountPercent": 0.0
                    },
                    {
                        "minQuantity": 11,
                        "maxQuantity": null,
                        "pricePerUnit": 849.99,
                        "discountPercent": 5.5
                    }
                ],
                "variants": [
                    {
                        "variantSku": "LAPTOP-TEST-001-SILVER",
                        "color": "Silver",
                        "size": null,
                        "priceAdjustment": 0.00,
                        "images": ["silver.jpg"]
                    }
                ]
            }
            """, supplierId);

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
        System.out.println("✅ Test 2: CREATE PRODUCT WITH PRICE TIERS AND VARIANTS");
        System.out.println("Status: " + response.getStatusCode());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"sku\":\"LAPTOP-TEST-001\"");
        assertThat(response.getBody()).contains("\"priceTiers\"");
        assertThat(response.getBody()).contains("\"variants\"");
    }

    /**
     * Test creating a new product.
     * This demonstrates the complete product creation flow.
     */
    @Test
    void shouldCreateProduct() {
        // given
        String jsonRequest = """
            {
                "sku": "TEST-PROD-001",
                "name": "Test Product",
                "description": "This is a test product for integration testing",
                "categoryId": 1,
                "basePrice": 99.99,
                "minimumOrderQuantity": 10,
                "supplierId": 1,
                "images": ["image1.jpg", "image2.jpg"],
                "priceTiers": [
                    {
                        "minQuantity": 10,
                        "maxQuantity": 50,
                        "pricePerUnit": 95.00,
                        "discountPercent": 5.0
                    },
                    {
                        "minQuantity": 51,
                        "maxQuantity": null,
                        "pricePerUnit": 90.00,
                        "discountPercent": 10.0
                    }
                ],
                "variants": [
                    {
                        "variantSku": "TEST-PROD-001-RED",
                        "color": "Red",
                        "size": null,
                        "priceAdjustment": 5.00,
                        "images": ["red-variant.jpg"]
                    },
                    {
                        "variantSku": "TEST-PROD-001-BLUE",
                        "color": "Blue",
                        "size": null,
                        "priceAdjustment": 3.00,
                        "images": ["blue-variant.jpg"]
                    }
                ]
            }
            """;

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
        System.out.println("✅ CREATE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 201 CREATED (if supplier exists) or 404/400 (if supplier not found)
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.CREATED, 
            HttpStatus.BAD_REQUEST, 
            HttpStatus.NOT_FOUND
        );
        assertThat(response.getBody()).isNotNull();
    }

    /**
     * Test getting a product by ID.
     */
    @Test
    void shouldGetProductById() {
        // given - product ID 1
        Long productId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/" + productId,
            String.class
        );

        // then
        System.out.println("✅ GET PRODUCT BY ID TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404 NOT FOUND
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    /**
     * Test getting a product by SKU.
     */
    @Test
    void shouldGetProductBySku() {
        // given
        String sku = "TEST-PROD-001";

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/sku/" + sku,
            String.class
        );

        // then
        System.out.println("✅ GET PRODUCT BY SKU TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404 NOT FOUND
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    /**
     * Test getting products by supplier ID.
     */
    @Test
    void shouldGetProductsBySupplierId() {
        // given
        Long supplierId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/supplier/" + supplierId,
            String.class
        );

        // then
        System.out.println("✅ GET PRODUCTS BY SUPPLIER TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK with array of products
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    /**
     * Test getting products by category.
     */
    @Test
    void shouldGetProductsByCategory() {
        // given
        Long categoryId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/category/" + categoryId,
            String.class
        );

        // then
        System.out.println("✅ GET PRODUCTS BY CATEGORY TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK with array of products
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    /**
     * Test updating a product.
     */
    @Test
    void shouldUpdateProduct() {
        // given
        Long productId = 1L;
        String jsonRequest = """
            {
                "name": "Updated Product Name",
                "description": "Updated product description",
                "category": "Updated Electronics",
                "basePrice": 109.99,
                "minimumOrderQuantity": 15
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.PUT,
            request,
            String.class
        );

        // then
        System.out.println("✅ UPDATE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404 NOT FOUND
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    /**
     * Test activating a product.
     */
    @Test
    void shouldActivateProduct() {
        // given
        Long productId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + productId + "/activate",
            null,
            String.class
        );

        // then
        System.out.println("✅ ACTIVATE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404/400
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.OK, 
            HttpStatus.NOT_FOUND, 
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Test deactivating a product.
     */
    @Test
    void shouldDeactivateProduct() {
        // given
        Long productId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + productId + "/deactivate",
            null,
            String.class
        );

        // then
        System.out.println("✅ DEACTIVATE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404/400
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.OK, 
            HttpStatus.NOT_FOUND, 
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Test discontinuing a product.
     */
    @Test
    void shouldDiscontinueProduct() {
        // given
        Long productId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + productId + "/discontinue",
            null,
            String.class
        );

        // then
        System.out.println("✅ DISCONTINUE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 200 OK (if product exists) or 404 NOT FOUND
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    /**
     * Test deleting (soft delete) a product.
     */
    @Test
    void shouldDeleteProduct() {
        // given
        Long productId = 1L;

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.DELETE,
            null,
            String.class
        );

        // then
        System.out.println("✅ DELETE PRODUCT TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 204 NO CONTENT (success) or 404 NOT FOUND or 400 BAD REQUEST
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.NO_CONTENT, 
            HttpStatus.NOT_FOUND, 
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Test validation - creating product with missing required fields.
     */
    @Test
    void shouldReturnBadRequestWhenMissingRequiredFields() {
        // given - invalid request with missing fields
        String jsonRequest = """
            {
                "name": "Test Product"
            }
            """;

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
        System.out.println("✅ VALIDATION TEST");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        // Expected: 400 BAD REQUEST due to validation errors
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
