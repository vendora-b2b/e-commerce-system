package com.example.ecommerce.marketplace.web.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RetailerController using real HTTP requests.
 * 
 * Test Strategy:
 * 1. Uses @SpringBootTest to start the full Spring Boot application with random port
 * 2. Makes real HTTP requests via TestRestTemplate to test complete request/response flow
 * 3. Uses MySQL database (configured in application-test.properties)
 * 4. Sets up test data in @BeforeEach to ensure consistent state for each test
 * 5. Tests all controller endpoints: POST (register), GET, PUT, POST (loyalty-points)
 * 6. Validates both success and error scenarios
 * 
 * Test Data Setup:
 * - Each test may create its own retailer as needed
 * - Unique business licenses prevent constraint violations between tests
 * - Database is cleaned after each test via @DirtiesContext
 * 
 * What We're Testing:
 * - HTTP request/response handling
 * - Request validation (Jakarta Bean Validation)
 * - JSON serialization/deserialization
 * - Controller → Use Case → Repository → Database flow
 * - Error handling and HTTP status codes
 * - Loyalty tier management and points operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RetailerControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RetailerRepository retailerRepository;

    private String baseUrl;

    /**
     * Setup base URL before each test.
     */
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/retailers";
    }

    /**
     * Test Case: Successfully register a new retailer
     * 
     * Given: Valid retailer registration request with all required fields
     * When: POST request is made to /api/v1/retailers
     * Then: 
     *   - Response status is 201 CREATED
     *   - Response body contains complete retailer details
     *   - Retailer has correct data from request
     *   - Loyalty tier is BRONZE by default
     *   - Loyalty points start at 0
     *   - Total purchase amount is 0.00
     *   - Retailer is persisted in database
     */
    @Test
    @DisplayName("POST /api/v1/retailers - Should successfully register retailer with valid data")
    void shouldRegisterRetailer() {
        // given
        String jsonRequest = """
            {
                "name": "Tech Retail Store",
                "email": "tech@retailstore.com",
                "phone": "+1234567890",
                "address": "123 Main Street, Tech City, TC 12345",
                "businessLicense": "RETLIC001",
                "creditLimit": 50000.00
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Tech Retail Store");
        assertThat(response.getBody()).contains("tech@retailstore.com");
        assertThat(response.getBody()).contains("+1234567890");
        assertThat(response.getBody()).contains("123 Main Street");
        assertThat(response.getBody()).contains("RETLIC001");
        assertThat(response.getBody()).contains("BRONZE"); // Default tier
        assertThat(response.getBody()).contains("\"loyaltyPoints\":0");
        assertThat(response.getBody()).contains("50000");

        // Verify retailer was saved to database
        assertThat(retailerRepository.findAll()).hasSize(1);
        Retailer savedRetailer = retailerRepository.findAll().get(0);
        assertThat(savedRetailer.getName()).isEqualTo("Tech Retail Store");
        assertThat(savedRetailer.getLoyaltyTier()).isEqualTo(RetailerLoyaltyTier.BRONZE);
        assertThat(savedRetailer.getLoyaltyPoints()).isEqualTo(0);
    }

    /**
     * Test Case: Reject retailer registration with missing required fields
     * 
     * Given: Registration request missing required field (name)
     * When: POST request is made to /api/v1/retailers
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Request is rejected by Jakarta Bean Validation
     *   - No retailer is created in database
     */
    @Test
    @DisplayName("POST /api/v1/retailers - Should reject registration with missing name")
    void shouldRejectRegistrationWithMissingName() {
        // given - missing name
        String jsonRequest = """
            {
                "email": "missing@name.com",
                "businessLicense": "RETLIC002",
                "creditLimit": 30000.00
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(retailerRepository.findAll()).isEmpty();
    }

    /**
     * Test Case: Reject retailer registration with invalid email format
     * 
     * Given: Registration request with invalid email format
     * When: POST request is made to /api/v1/retailers
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Request is rejected by @Email validation
     *   - No retailer is created in database
     */
    @Test
    @DisplayName("POST /api/v1/retailers - Should reject registration with invalid email")
    void shouldRejectRegistrationWithInvalidEmail() {
        // given - invalid email format
        String jsonRequest = """
            {
                "name": "Invalid Email Store",
                "email": "not-an-email",
                "businessLicense": "RETLIC003",
                "creditLimit": 40000.00
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(retailerRepository.findAll()).isEmpty();
    }

    /**
     * Test Case: Successfully retrieve retailer by ID
     * 
     * Given: A retailer exists in the database
     * When: GET request is made to /api/v1/retailers/{id}
     * Then: 
     *   - Response status is 200 OK
     *   - Response body contains complete retailer details
     *   - All retailer attributes are present and correct
     */
    @Test
    @DisplayName("GET /api/v1/retailers/{id} - Should successfully retrieve retailer by ID")
    void shouldGetRetailerById() {
        // given - create a retailer
        Retailer retailer = new Retailer();
        retailer.setName("Fashion Boutique");
        retailer.setEmail("fashion@boutique.com");
        retailer.setBusinessLicense("RETLIC004");
        retailer.setCreditLimit(60000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.updateProfile("Fashion Boutique", "+9876543210", "456 Fashion Ave", "Premium fashion retailer");
        Retailer saved = retailerRepository.save(retailer);

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/" + saved.getId(),
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Fashion Boutique");
        assertThat(response.getBody()).contains("fashion@boutique.com");
        assertThat(response.getBody()).contains("+9876543210");
        assertThat(response.getBody()).contains("456 Fashion Ave");
        assertThat(response.getBody()).contains("Premium fashion retailer");
        assertThat(response.getBody()).contains("RETLIC004");
        assertThat(response.getBody()).contains("BRONZE");
        assertThat(response.getBody()).contains("60000");
    }

    /**
     * Test Case: Return 404 when retrieving non-existent retailer
     * 
     * Given: Retailer ID does not exist in database
     * When: GET request is made to /api/v1/retailers/{id}
     * Then: 
     *   - Response status is 404 NOT_FOUND
     *   - Error is handled by ErrorMapper
     */
    @Test
    @DisplayName("GET /api/v1/retailers/{id} - Should return 404 for non-existent retailer")
    void shouldReturn404ForNonExistentRetailer() {
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
     * Test Case: Successfully update retailer profile
     * 
     * Given: A retailer exists and valid update request
     * When: PUT request is made to /api/v1/retailers/{id}
     * Then: 
     *   - Response status is 200 OK
     *   - Retailer profile is updated with new values
     *   - Response contains updated retailer details
     *   - Changes are persisted in database
     */
    @Test
    @DisplayName("PUT /api/v1/retailers/{id} - Should successfully update retailer profile")
    void shouldUpdateRetailerProfile() {
        // given - create a retailer
        Retailer retailer = new Retailer();
        retailer.setName("Old Name Store");
        retailer.setEmail("old@email.com");
        retailer.setBusinessLicense("RETLIC005");
        retailer.setCreditLimit(45000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        Retailer saved = retailerRepository.save(retailer);

        String jsonRequest = """
            {
                "name": "New Name Store",
                "phone": "+1122334455",
                "address": "999 Updated Street, New City",
                "profileDescription": "Updated description for our store"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + saved.getId(),
            HttpMethod.PUT,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("New Name Store");
        assertThat(response.getBody()).contains("+1122334455");
        assertThat(response.getBody()).contains("999 Updated Street");
        assertThat(response.getBody()).contains("Updated description for our store");

        // Verify changes persisted
        Optional<Retailer> updated = retailerRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name Store");
        assertThat(updated.get().getPhone()).isEqualTo("+1122334455");
        assertThat(updated.get().getAddress()).isEqualTo("999 Updated Street, New City");
        assertThat(updated.get().getProfileDescription()).isEqualTo("Updated description for our store");
    }

    /**
     * Test Case: Return 404 when updating non-existent retailer
     * 
     * Given: Retailer ID does not exist in database
     * When: PUT request is made to /api/v1/retailers/{id}
     * Then: 
     *   - Response status is 404 NOT_FOUND
     *   - Error is handled by ErrorMapper
     */
    @Test
    @DisplayName("PUT /api/v1/retailers/{id} - Should return 404 when updating non-existent retailer")
    void shouldReturn404WhenUpdatingNonExistentRetailer() {
        // given - non-existent ID
        Long nonExistentId = 88888L;

        String jsonRequest = """
            {
                "name": "Does Not Matter"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + nonExistentId,
            HttpMethod.PUT,
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Test Case: Successfully add loyalty points
     * 
     * Given: A retailer exists with 0 loyalty points
     * When: POST request is made to /api/v1/retailers/{id}/loyalty-points with ADD operation
     * Then: 
     *   - Response status is 200 OK
     *   - Loyalty points are increased
     *   - Response contains updated retailer with new points
     *   - Changes are persisted in database
     */
    @Test
    @DisplayName("POST /api/v1/retailers/{id}/loyalty-points - Should successfully add loyalty points")
    void shouldAddLoyaltyPoints() {
        // given - create a retailer
        Retailer retailer = new Retailer();
        retailer.setName("Points Test Store");
        retailer.setEmail("points@test.com");
        retailer.setBusinessLicense("RETLIC006");
        retailer.setCreditLimit(35000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        Retailer saved = retailerRepository.save(retailer);

        String jsonRequest = """
            {
                "operationType": "ADD",
                "points": 500
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + saved.getId() + "/loyalty-points",
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"loyaltyPoints\":500");
        assertThat(response.getBody()).contains("BRONZE"); // Still BRONZE (< 1000 points)

        // Verify changes persisted
        Optional<Retailer> updated = retailerRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLoyaltyPoints()).isEqualTo(500);
    }

    /**
     * Test Case: Successfully redeem loyalty points
     * 
     * Given: A retailer exists with sufficient loyalty points
     * When: POST request is made to /api/v1/retailers/{id}/loyalty-points with REDEEM operation
     * Then: 
     *   - Response status is 200 OK
     *   - Loyalty points are decreased
     *   - Response contains updated retailer with reduced points
     *   - Changes are persisted in database
     */
    @Test
    @DisplayName("POST /api/v1/retailers/{id}/loyalty-points - Should successfully redeem loyalty points")
    void shouldRedeemLoyaltyPoints() {
        // given - create a retailer with points
        Retailer retailer = new Retailer();
        retailer.setName("Redeem Test Store");
        retailer.setEmail("redeem@test.com");
        retailer.setBusinessLicense("RETLIC007");
        retailer.setCreditLimit(40000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.addLoyaltyPoints(1000); // Add points first
        Retailer saved = retailerRepository.save(retailer);

        String jsonRequest = """
            {
                "operationType": "REDEEM",
                "points": 300
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + saved.getId() + "/loyalty-points",
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"loyaltyPoints\":700");
        assertThat(response.getBody()).contains("BRONZE"); // Back to BRONZE (< 1000 points)

        // Verify changes persisted
        Optional<Retailer> updated = retailerRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLoyaltyPoints()).isEqualTo(700);
    }

    /**
     * Test Case: Verify loyalty tier upgrade when adding points
     * 
     * Given: A retailer with BRONZE tier (< 1000 points)
     * When: POST request adds enough points to reach SILVER tier (1000-4999)
     * Then: 
     *   - Response status is 200 OK
     *   - Loyalty tier is automatically upgraded to SILVER
     *   - Response contains updated tier
     */
    @Test
    @DisplayName("POST /api/v1/retailers/{id}/loyalty-points - Should upgrade tier when threshold reached")
    void shouldUpgradeLoyaltyTierWhenThresholdReached() {
        // given - create a retailer with some points (BRONZE)
        Retailer retailer = new Retailer();
        retailer.setName("Tier Upgrade Store");
        retailer.setEmail("tier@upgrade.com");
        retailer.setBusinessLicense("RETLIC008");
        retailer.setCreditLimit(55000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.addLoyaltyPoints(800); // 800 points = BRONZE
        Retailer saved = retailerRepository.save(retailer);

        String jsonRequest = """
            {
                "operationType": "ADD",
                "points": 300
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + saved.getId() + "/loyalty-points",
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"loyaltyPoints\":1100");
        assertThat(response.getBody()).contains("SILVER"); // Upgraded to SILVER (>= 1000 points)

        // Verify tier upgrade persisted
        Optional<Retailer> updated = retailerRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLoyaltyPoints()).isEqualTo(1100);
        assertThat(updated.get().getLoyaltyTier()).isEqualTo(RetailerLoyaltyTier.SILVER);
    }

    /**
     * Test Case: Reject loyalty points operation with invalid data
     * 
     * Given: Loyalty points request with invalid points (negative or zero)
     * When: POST request is made to /api/v1/retailers/{id}/loyalty-points
     * Then: 
     *   - Response status is 400 BAD_REQUEST
     *   - Request is rejected by @Positive validation
     *   - No changes are made to retailer
     */
    @Test
    @DisplayName("POST /api/v1/retailers/{id}/loyalty-points - Should reject operation with invalid points")
    void shouldRejectLoyaltyPointsWithInvalidPoints() {
        // given - create a retailer
        Retailer retailer = new Retailer();
        retailer.setName("Invalid Points Store");
        retailer.setEmail("invalid@points.com");
        retailer.setBusinessLicense("RETLIC009");
        retailer.setCreditLimit(30000.00);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(0.0);
        Retailer saved = retailerRepository.save(retailer);

        String jsonRequest = """
            {
                "operationType": "ADD",
                "points": -100
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + saved.getId() + "/loyalty-points",
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify no changes
        Optional<Retailer> unchanged = retailerRepository.findById(saved.getId());
        assertThat(unchanged).isPresent();
        assertThat(unchanged.get().getLoyaltyPoints()).isEqualTo(0);
    }

    /**
     * Test Case: Return 404 when managing points for non-existent retailer
     * 
     * Given: Retailer ID does not exist in database
     * When: POST request is made to /api/v1/retailers/{id}/loyalty-points
     * Then: 
     *   - Response status is 404 NOT_FOUND
     *   - Error is handled by ErrorMapper
     */
    @Test
    @DisplayName("POST /api/v1/retailers/{id}/loyalty-points - Should return 404 for non-existent retailer")
    void shouldReturn404WhenManagingPointsForNonExistentRetailer() {
        // given - non-existent ID
        Long nonExistentId = 77777L;

        String jsonRequest = """
            {
                "operationType": "ADD",
                "points": 100
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/" + nonExistentId + "/loyalty-points",
            request,
            String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
