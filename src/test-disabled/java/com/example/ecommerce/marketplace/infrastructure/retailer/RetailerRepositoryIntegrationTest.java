package com.example.ecommerce.marketplace.infrastructure.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Retailer repository infrastructure layer.
 * Tests JPA entity configuration, database constraints, and entity-domain mapping.
 * Uses MySQL database configured in application-test.properties.
 *
 * Tests the complete Retailer infrastructure implementation including:
 * - RetailerEntity JPA entity
 * - JpaRetailerRepository Spring Data repository
 * - RetailerRepositoryImpl adapter
 * - Entity-domain mapping
 * - Database constraints (unique email, unique business license)
 * - Query methods for account status and loyalty tier
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Retailer Repository Integration Tests")
class RetailerRepositoryIntegrationTest {

    @Autowired
    private RetailerRepository retailerRepository;

    private Retailer testRetailer;

    @BeforeEach
    void setUp() {
        testRetailer = new Retailer();
        testRetailer.setId(null);
        testRetailer.setName("Tech Retail Store");
        testRetailer.setEmail("contact@techretail.com");
        testRetailer.setPhone("+1234567890");
        testRetailer.setAddress("456 Retail Blvd, Commerce City");
        testRetailer.setProfilePicture("retailer-profile.jpg");
        testRetailer.setProfileDescription("Leading tech retail business");
        testRetailer.setBusinessLicense("RTL12345678");
        testRetailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        testRetailer.setCreditLimit(10000.0);
        testRetailer.setTotalPurchaseAmount(0.0);
        testRetailer.setLoyaltyPoints(0);
    }

    // ===== Save and Retrieve Tests =====

    @Test
    @DisplayName("Should save retailer and generate ID")
    void testSave_GeneratesId() {
        // When
        Retailer saved = retailerRepository.save(testRetailer);

        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertTrue(saved.getId() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should save and retrieve retailer by ID")
    void testSaveAndFindById_Success() {
        // Given
        Retailer saved = retailerRepository.save(testRetailer);

        // When
        Optional<Retailer> found = retailerRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent(), "Retailer should be found");
        Retailer retailer = found.get();
        assertEquals(saved.getId(), retailer.getId());
        assertEquals("Tech Retail Store", retailer.getName());
        assertEquals("contact@techretail.com", retailer.getEmail());
        assertEquals("+1234567890", retailer.getPhone());
        assertEquals("456 Retail Blvd, Commerce City", retailer.getAddress());
        assertEquals("retailer-profile.jpg", retailer.getProfilePicture());
        assertEquals("Leading tech retail business", retailer.getProfileDescription());
        assertEquals("RTL12345678", retailer.getBusinessLicense());
        assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier());
        assertEquals(10000.0, retailer.getCreditLimit());
        assertEquals(0.0, retailer.getTotalPurchaseAmount());
        assertEquals(0, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should return empty optional when retailer not found")
    void testFindById_NotFound() {
        // When
        Optional<Retailer> found = retailerRepository.findById(999L);

        // Then
        assertFalse(found.isPresent(), "Should return empty optional");
    }

    @Test
    @DisplayName("Should save retailer with null optional fields")
    void testSave_WithNullOptionalFields() {
        // Given
        Retailer retailer = new Retailer();
        retailer.setName("Minimal Retailer");
        retailer.setEmail("minimal@retailer.com");
        retailer.setPhone(null);  // phone can be null
        retailer.setAddress(null);  // address can be null
        retailer.setProfilePicture(null);  // profilePicture can be null
        retailer.setProfileDescription(null);  // profileDescription can be null
        retailer.setBusinessLicense("MIN12345678");
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setCreditLimit(null);  // creditLimit can be null
        retailer.setTotalPurchaseAmount(null);  // totalPurchaseAmount can be null
        retailer.setLoyaltyPoints(null);  // loyaltyPoints can be null

        // When
        Retailer saved = retailerRepository.save(retailer);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Minimal Retailer", saved.getName());
        assertNull(saved.getPhone());
        assertNull(saved.getAddress());
        assertNull(saved.getProfilePicture());
        assertNull(saved.getProfileDescription());
        assertNull(saved.getCreditLimit());
        assertNull(saved.getTotalPurchaseAmount());
        assertNull(saved.getLoyaltyPoints());
    }

    // ===== Unique Constraint Tests =====

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testSave_DuplicateEmail_ThrowsException() {
        // Given - Save first retailer
        retailerRepository.save(testRetailer);

        // When - Try to save another retailer with same email
        Retailer duplicate = new Retailer();
        duplicate.setName("Different Retailer");
        duplicate.setEmail("contact@techretail.com");  // Same email
        duplicate.setPhone("+9876543210");
        duplicate.setAddress("Different Address");
        duplicate.setBusinessLicense("DIFF12345678");  // Different license
        duplicate.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            retailerRepository.save(duplicate);
            // Force flush to trigger constraint check
        });
    }

    @Test
    @DisplayName("Should enforce unique business license constraint")
    void testSave_DuplicateLicense_ThrowsException() {
        // Given - Save first retailer
        retailerRepository.save(testRetailer);

        // When - Try to save another retailer with same license
        Retailer duplicate = new Retailer();
        duplicate.setName("Different Retailer");
        duplicate.setEmail("different@retailer.com");  // Different email
        duplicate.setPhone("+9876543210");
        duplicate.setAddress("Different Address");
        duplicate.setBusinessLicense("RTL12345678");  // Same license
        duplicate.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            retailerRepository.save(duplicate);
            // Force flush to trigger constraint check
        });
    }

    // ===== Update Tests =====

    @Test
    @DisplayName("Should update existing retailer")
    void testUpdate_Success() {
        // Given - Save initial retailer
        Retailer saved = retailerRepository.save(testRetailer);
        Long retailerId = saved.getId();

        // When - Update the retailer
        saved.updateProfile(
            "Updated Tech Retail",
            "+9999999999",
            "Updated Address 123",
            "Updated business description"
        );
        saved.setCreditLimit(20000.0);

        Retailer updated = retailerRepository.save(saved);

        // Then - Verify updates persisted
        assertEquals(retailerId, updated.getId(), "ID should not change");
        assertEquals("Updated Tech Retail", updated.getName());
        assertEquals("+9999999999", updated.getPhone());
        assertEquals("Updated Address 123", updated.getAddress());
        assertEquals("Updated business description", updated.getProfileDescription());
        assertEquals(20000.0, updated.getCreditLimit());
    }

    @Test
    @DisplayName("Should update loyalty tier and points")
    void testUpdate_LoyaltyTierAndPoints() {
        // Given - Save initial retailer with bronze tier
        Retailer saved = retailerRepository.save(testRetailer);
        assertEquals(RetailerLoyaltyTier.BRONZE, saved.getLoyaltyTier());
        assertEquals(0, saved.getLoyaltyPoints());

        // When - Add loyalty points and update tier
        saved.addLoyaltyPoints(1500);  // This should update tier to SILVER
        Retailer updated = retailerRepository.save(saved);

        // Then - Verify tier was updated
        assertEquals(1500, updated.getLoyaltyPoints());
        assertEquals(RetailerLoyaltyTier.SILVER, updated.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should record purchases and update total purchase amount")
    void testUpdate_RecordPurchase() {
        // Given - Save initial retailer
        Retailer saved = retailerRepository.save(testRetailer);
        assertEquals(0.0, saved.getTotalPurchaseAmount());

        // When - Record purchases
        saved.recordPurchase(5000.0);
        saved.recordPurchase(3000.0);
        Retailer updated = retailerRepository.save(saved);

        // Then - Verify total purchase amount updated
        assertEquals(8000.0, updated.getTotalPurchaseAmount());
    }

    // ===== Query Method Tests =====

    @Test
    @DisplayName("Should find retailer by email")
    void testFindByEmail_Success() {
        // Given
        retailerRepository.save(testRetailer);

        // When
        Optional<Retailer> found = retailerRepository.findByEmail("contact@techretail.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Tech Retail Store", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // When
        Optional<Retailer> found = retailerRepository.findByEmail("nonexistent@email.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find retailer by business license")
    void testFindByBusinessLicense_Success() {
        // Given
        retailerRepository.save(testRetailer);

        // When
        Optional<Retailer> found = retailerRepository.findByBusinessLicense("RTL12345678");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Tech Retail Store", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty when business license not found")
    void testFindByBusinessLicense_NotFound() {
        // When
        Optional<Retailer> found = retailerRepository.findByBusinessLicense("NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Given
        retailerRepository.save(testRetailer);

        // Then
        assertTrue(retailerRepository.existsByEmail("contact@techretail.com"));
        assertFalse(retailerRepository.existsByEmail("nonexistent@email.com"));
    }

    @Test
    @DisplayName("Should check if business license exists")
    void testExistsByBusinessLicense() {
        // Given
        retailerRepository.save(testRetailer);

        // Then
        assertTrue(retailerRepository.existsByBusinessLicense("RTL12345678"));
        assertFalse(retailerRepository.existsByBusinessLicense("NONEXISTENT"));
    }

    @Test
    @DisplayName("Should find retailers by account status")
    void testFindByAccountStatus() {
        // Given - Create retailers with different account statuses
        retailerRepository.save(testRetailer);  // Default status should be ACTIVE

        Retailer suspended = new Retailer();
        suspended.setName("Suspended Retailer");
        suspended.setEmail("suspended@retailer.com");
        suspended.setBusinessLicense("SUS12345678");
        suspended.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailerRepository.save(suspended);

        Retailer inactive = new Retailer();
        inactive.setName("Inactive Retailer");
        inactive.setEmail("inactive@retailer.com");
        inactive.setBusinessLicense("INA12345678");
        inactive.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailerRepository.save(inactive);

        // When
        List<Retailer> activeRetailers = retailerRepository.findByAccountStatus("ACTIVE");

        // Then
        assertEquals(3, activeRetailers.size());  // All should default to ACTIVE
        assertTrue(activeRetailers.stream()
            .anyMatch(r -> r.getName().equals("Tech Retail Store")));
    }

    @Test
    @DisplayName("Should find retailers by loyalty tier")
    void testFindByLoyaltyTier() {
        // Given
        retailerRepository.save(testRetailer);  // BRONZE tier

        Retailer silverRetailer = new Retailer();
        silverRetailer.setName("Silver Retailer");
        silverRetailer.setEmail("silver@retailer.com");
        silverRetailer.setBusinessLicense("SIL12345678");
        silverRetailer.setLoyaltyTier(RetailerLoyaltyTier.SILVER);
        silverRetailer.setLoyaltyPoints(1500);
        retailerRepository.save(silverRetailer);

        Retailer goldRetailer = new Retailer();
        goldRetailer.setName("Gold Retailer");
        goldRetailer.setEmail("gold@retailer.com");
        goldRetailer.setBusinessLicense("GLD12345678");
        goldRetailer.setLoyaltyTier(RetailerLoyaltyTier.GOLD);
        goldRetailer.setLoyaltyPoints(6000);
        retailerRepository.save(goldRetailer);

        // When
        List<Retailer> bronzeRetailers = retailerRepository.findByLoyaltyTier("BRONZE");
        List<Retailer> silverRetailers = retailerRepository.findByLoyaltyTier("SILVER");
        List<Retailer> goldRetailers = retailerRepository.findByLoyaltyTier("GOLD");

        // Then
        assertEquals(1, bronzeRetailers.size());
        assertEquals("Tech Retail Store", bronzeRetailers.get(0).getName());

        assertEquals(1, silverRetailers.size());
        assertEquals("Silver Retailer", silverRetailers.get(0).getName());

        assertEquals(1, goldRetailers.size());
        assertEquals("Gold Retailer", goldRetailers.get(0).getName());
    }

    @Test
    @DisplayName("Should find all retailers")
    void testFindAll() {
        // Given
        retailerRepository.save(testRetailer);

        Retailer another = new Retailer();
        another.setName("Another Retailer");
        another.setEmail("another@retailer.com");
        another.setBusinessLicense("ANO12345678");
        another.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailerRepository.save(another);

        // When
        List<Retailer> allRetailers = retailerRepository.findAll();

        // Then
        assertEquals(2, allRetailers.size());
        assertTrue(allRetailers.stream()
            .anyMatch(r -> r.getName().equals("Tech Retail Store")));
        assertTrue(allRetailers.stream()
            .anyMatch(r -> r.getName().equals("Another Retailer")));
    }

    // ===== Delete Tests =====

    @Test
    @DisplayName("Should delete retailer by ID")
    void testDeleteById() {
        // Given
        Retailer saved = retailerRepository.save(testRetailer);
        Long retailerId = saved.getId();

        // When
        retailerRepository.deleteById(retailerId);

        // Then
        Optional<Retailer> found = retailerRepository.findById(retailerId);
        assertFalse(found.isPresent(), "Retailer should be deleted");
    }

    // ===== Count Tests =====

    @Test
    @DisplayName("Should count all retailers")
    void testCount() {
        // Given
        assertEquals(0, retailerRepository.count());

        retailerRepository.save(testRetailer);

        Retailer another = new Retailer();
        another.setName("Another Retailer");
        another.setEmail("another@retailer.com");
        another.setBusinessLicense("ANO12345678");
        another.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailerRepository.save(another);

        // Then
        assertEquals(2, retailerRepository.count());
    }

    @Test
    @DisplayName("Should count retailers by account status")
    void testCountByAccountStatus() {
        // Given
        retailerRepository.save(testRetailer);  // ACTIVE

        Retailer another = new Retailer();
        another.setName("Another Active");
        another.setEmail("active2@retailer.com");
        another.setBusinessLicense("ACT12345678");
        another.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailerRepository.save(another);  // ACTIVE

        // When
        long activeCount = retailerRepository.countByAccountStatus("ACTIVE");

        // Then
        assertEquals(2, activeCount);
    }

    // ===== Entity-Domain Mapping Tests =====

    @Test
    @DisplayName("Should correctly map all domain fields to entity")
    void testDomainToEntityMapping() {
        // Given - Create domain object with all fields
        Retailer domain = new Retailer();
        domain.setName("Mapping Test Retailer");
        domain.setEmail("mapping@test.com");
        domain.setPhone("+1111111111");
        domain.setAddress("123 Mapping St");
        domain.setProfilePicture("map.jpg");
        domain.setProfileDescription("Testing entity mapping");
        domain.setBusinessLicense("MAP12345678");
        domain.setLoyaltyTier(RetailerLoyaltyTier.GOLD);
        domain.setCreditLimit(50000.0);
        domain.setTotalPurchaseAmount(100000.0);
        domain.setLoyaltyPoints(8000);

        // When - Save (triggers domain → entity mapping)
        Retailer saved = retailerRepository.save(domain);

        // Then - Retrieve and verify all fields
        Retailer retrieved = retailerRepository.findById(saved.getId()).get();

        assertNotNull(retrieved.getId());
        assertEquals("Mapping Test Retailer", retrieved.getName());
        assertEquals("mapping@test.com", retrieved.getEmail());
        assertEquals("+1111111111", retrieved.getPhone());
        assertEquals("123 Mapping St", retrieved.getAddress());
        assertEquals("map.jpg", retrieved.getProfilePicture());
        assertEquals("Testing entity mapping", retrieved.getProfileDescription());
        assertEquals("MAP12345678", retrieved.getBusinessLicense());
        assertEquals(RetailerLoyaltyTier.GOLD, retrieved.getLoyaltyTier());
        assertEquals(50000.0, retrieved.getCreditLimit());
        assertEquals(100000.0, retrieved.getTotalPurchaseAmount());
        assertEquals(8000, retrieved.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should preserve domain object state after save")
    void testDomainObjectUnchangedAfterSave() {
        // Given
        String originalName = testRetailer.getName();
        String originalEmail = testRetailer.getEmail();
        Double originalCreditLimit = testRetailer.getCreditLimit();

        // When
        Retailer saved = retailerRepository.save(testRetailer);

        // Then - Original domain object should be unchanged
        assertEquals(originalName, testRetailer.getName());
        assertEquals(originalEmail, testRetailer.getEmail());
        assertEquals(originalCreditLimit, testRetailer.getCreditLimit());

        // But returned object should have ID
        assertNotNull(saved.getId());
    }

    // ===== Business Logic Integration Tests =====

    @Test
    @DisplayName("Should persist loyalty tier changes when points are added")
    void testPersistLoyaltyTierChanges() {
        // Given - Save retailer with bronze tier
        Retailer saved = retailerRepository.save(testRetailer);
        assertEquals(RetailerLoyaltyTier.BRONZE, saved.getLoyaltyTier());

        // When - Add enough points to reach GOLD tier
        saved.addLoyaltyPoints(5000);
        Retailer updated = retailerRepository.save(saved);

        // Then - Verify tier persisted correctly
        Retailer retrieved = retailerRepository.findById(updated.getId()).get();
        assertEquals(RetailerLoyaltyTier.GOLD, retrieved.getLoyaltyTier());
        assertEquals(5000, retrieved.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should persist changes after redeeming loyalty points")
    void testPersistAfterRedeemingPoints() {
        // Given - Save retailer with points
        testRetailer.setLoyaltyPoints(1000);
        Retailer saved = retailerRepository.save(testRetailer);

        // When - Redeem some points
        saved.redeemLoyaltyPoints(500);
        Retailer updated = retailerRepository.save(saved);

        // Then - Verify points were deducted
        Retailer retrieved = retailerRepository.findById(updated.getId()).get();
        assertEquals(500, retrieved.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should persist purchase history correctly")
    void testPersistPurchaseHistory() {
        // Given - Save retailer
        Retailer saved = retailerRepository.save(testRetailer);
        assertEquals(0.0, saved.getTotalPurchaseAmount());

        // When - Record multiple purchases
        saved.recordPurchase(10000.0);
        saved.recordPurchase(20000.0);
        saved.recordPurchase(25000.0);
        Retailer updated = retailerRepository.save(saved);

        // Then - Verify total purchase amount and tier upgrade
        Retailer retrieved = retailerRepository.findById(updated.getId()).get();
        assertEquals(55000.0, retrieved.getTotalPurchaseAmount());
        assertEquals(RetailerLoyaltyTier.GOLD, retrieved.getLoyaltyTier());  // >= 50000
    }

    @Test
    @DisplayName("Should validate email format before persisting")
    void testEmailValidation() {
        // Given
        Retailer saved = retailerRepository.save(testRetailer);

        // Then
        assertTrue(saved.validateEmail(), "Valid email should pass validation");
        assertEquals("contact@techretail.com", saved.getEmail());
    }

    @Test
    @DisplayName("Should validate business license format before persisting")
    void testBusinessLicenseValidation() {
        // Given
        Retailer saved = retailerRepository.save(testRetailer);

        // Then
        assertTrue(saved.validateBusinessLicense(), "Valid license should pass validation");
        assertEquals("RTL12345678", saved.getBusinessLicense());
    }

    @Test
    @DisplayName("Should handle credit limit checks correctly")
    void testCreditLimitPersistence() {
        // Given
        testRetailer.setCreditLimit(5000.0);
        Retailer saved = retailerRepository.save(testRetailer);

        // When - Verify credit availability
        assertTrue(saved.hasAvailableCredit(4000.0));
        assertFalse(saved.hasAvailableCredit(6000.0));

        // Then - Verify persisted correctly
        Retailer retrieved = retailerRepository.findById(saved.getId()).get();
        assertEquals(5000.0, retrieved.getCreditLimit());
        assertTrue(retrieved.hasAvailableCredit(4000.0));
    }
}
