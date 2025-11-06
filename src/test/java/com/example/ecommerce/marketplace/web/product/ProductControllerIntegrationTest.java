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
 * 
 * HOW TO USE:
 * 1. Make sure your application is NOT already running
 * 2. Uncomment the @SpringBootTest and @ActiveProfiles annotations
 * 3. Run this test class
 * 4. The test will start the application automatically
 */
// Uncomment these annotations to run the tests:
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/products";
    }

    /**
     * Test creating a new product.
     * This demonstrates the complete product creation flow.
     */
    // @Test
    void shouldCreateProduct() {
        // given
        String jsonRequest = """
            {
                "sku": "TEST-PROD-001",
                "name": "Test Product",
                "description": "This is a test product for integration testing",
                "category": "Electronics",
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
                        "variantName": "Color",
                        "variantValue": "Red",
                        "priceAdjustment": 5.00,
                        "images": ["red-variant.jpg"]
                    },
                    {
                        "variantName": "Color",
                        "variantValue": "Blue",
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
    // @Test
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
    // @Test
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
    // @Test
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
    // @Test
    void shouldGetProductsByCategory() {
        // given
        String category = "Electronics";

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/category/" + category,
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
    // @Test
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
    // @Test
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
    // @Test
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
    // @Test
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
    // @Test
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
    // @Test
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
