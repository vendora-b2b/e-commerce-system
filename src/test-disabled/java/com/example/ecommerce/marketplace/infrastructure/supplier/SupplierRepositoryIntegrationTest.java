package com.example.ecommerce.marketplace.infrastructure.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
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
 * Integration tests for Supplier repository infrastructure layer.
 * Tests JPA entity configuration, database constraints, and entity-domain mapping.
 * Uses MySQL database configured in application-test.properties.
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Supplier Repository Integration Tests")
class SupplierRepositoryIntegrationTest {

    @Autowired
    private SupplierRepository supplierRepository;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier(
            null,
            "Tech Supplies Inc",
            "contact@techsupplies.com",
            "+1234567890",
            "123 Business St, Tech City",
            "profile.jpg",
            "Leading tech supplier",
            "LIC12345",
            4.5,
            true
        );
    }

    // ===== Save and Retrieve Tests =====

    @Test
    @DisplayName("Should save supplier and generate ID")
    void testSave_GeneratesId() {
        // When
        Supplier saved = supplierRepository.save(testSupplier);

        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertTrue(saved.getId() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should save and retrieve supplier by ID")
    void testSaveAndFindById_Success() {
        // Given
        Supplier saved = supplierRepository.save(testSupplier);

        // When
        Optional<Supplier> found = supplierRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent(), "Supplier should be found");
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Tech Supplies Inc", found.get().getName());
        assertEquals("contact@techsupplies.com", found.get().getEmail());
        assertEquals("+1234567890", found.get().getPhone());
        assertEquals("123 Business St, Tech City", found.get().getAddress());
        assertEquals("profile.jpg", found.get().getProfilePicture());
        assertEquals("Leading tech supplier", found.get().getProfileDescription());
        assertEquals("LIC12345", found.get().getBusinessLicense());
        assertEquals(4.5, found.get().getRating());
        assertTrue(found.get().isVerified());
    }

    @Test
    @DisplayName("Should return empty optional when supplier not found")
    void testFindById_NotFound() {
        // When
        Optional<Supplier> found = supplierRepository.findById(999L);

        // Then
        assertFalse(found.isPresent(), "Should return empty optional");
    }

    @Test
    @DisplayName("Should save supplier with null optional fields")
    void testSave_WithNullOptionalFields() {
        // Given
        Supplier supplier = new Supplier(
            null,
            "Minimal Supplier",
            "minimal@test.com",
            null,  // phone can be null
            null,  // address can be null
            null,  // profilePicture can be null
            null,  // profileDescription can be null
            "LIC99999",
            null,  // rating can be null
            false
        );

        // When
        Supplier saved = supplierRepository.save(supplier);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Minimal Supplier", saved.getName());
        assertNull(saved.getPhone());
        assertNull(saved.getAddress());
        assertNull(saved.getRating());
    }

    // ===== Unique Constraint Tests =====

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testSave_DuplicateEmail_ThrowsException() {
        // Given - Save first supplier
        supplierRepository.save(testSupplier);

        // When - Try to save another supplier with same email
        Supplier duplicate = new Supplier(
            null,
            "Different Company",
            "contact@techsupplies.com",  // Same email
            "+9876543210",
            "Different Address",
            null,
            null,
            "LIC99999",  // Different license
            null,
            false
        );

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            supplierRepository.save(duplicate);
            // Force flush to trigger constraint check
        });
    }

    @Test
    @DisplayName("Should enforce unique business license constraint")
    void testSave_DuplicateLicense_ThrowsException() {
        // Given - Save first supplier
        supplierRepository.save(testSupplier);

        // When - Try to save another supplier with same license
        Supplier duplicate = new Supplier(
            null,
            "Different Company",
            "different@email.com",  // Different email
            "+9876543210",
            "Different Address",
            null,
            null,
            "LIC12345",  // Same license
            null,
            false
        );

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            supplierRepository.save(duplicate);
            // Force flush to trigger constraint check
        });
    }

    // ===== Update Tests =====

    @Test
    @DisplayName("Should update existing supplier")
    void testUpdate_Success() {
        // Given - Save initial supplier
        Supplier saved = supplierRepository.save(testSupplier);
        Long supplierId = saved.getId();

        // When - Update the supplier
        saved.updateProfile(
            "Updated Tech Supplies",
            "+9999999999",
            "Updated Address",
            "Updated description"
        );
        saved.updateRating(5.0);

        Supplier updated = supplierRepository.save(saved);

        // Then - Verify updates persisted
        assertEquals(supplierId, updated.getId(), "ID should not change");
        assertEquals("Updated Tech Supplies", updated.getName());
        assertEquals("+9999999999", updated.getPhone());
        assertEquals("Updated Address", updated.getAddress());
        assertEquals("Updated description", updated.getProfileDescription());
        assertEquals(4.75, updated.getRating(), 0.01); // (4.5 + 5.0) / 2
    }

    // ===== Query Method Tests =====

    @Test
    @DisplayName("Should find supplier by email")
    void testFindByEmail_Success() {
        // Given
        supplierRepository.save(testSupplier);

        // When
        Optional<Supplier> found = supplierRepository.findByEmail("contact@techsupplies.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Tech Supplies Inc", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // When
        Optional<Supplier> found = supplierRepository.findByEmail("nonexistent@email.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find supplier by business license")
    void testFindByBusinessLicense_Success() {
        // Given
        supplierRepository.save(testSupplier);

        // When
        Optional<Supplier> found = supplierRepository.findByBusinessLicense("LIC12345");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Tech Supplies Inc", found.get().getName());
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Given
        supplierRepository.save(testSupplier);

        // Then
        assertTrue(supplierRepository.existsByEmail("contact@techsupplies.com"));
        assertFalse(supplierRepository.existsByEmail("nonexistent@email.com"));
    }

    @Test
    @DisplayName("Should check if business license exists")
    void testExistsByBusinessLicense() {
        // Given
        supplierRepository.save(testSupplier);

        // Then
        assertTrue(supplierRepository.existsByBusinessLicense("LIC12345"));
        assertFalse(supplierRepository.existsByBusinessLicense("NONEXISTENT"));
    }

    @Test
    @DisplayName("Should find suppliers by verified status")
    void testFindByVerified() {
        // Given
        Supplier saved = supplierRepository.save(testSupplier); // verified = true

        Supplier unverified = new Supplier(
            null, "Unverified Co", "unverified@test.com", null, null,
            null, null, "LIC99999", null, false
        );
        Supplier savedUnverified = supplierRepository.save(unverified); // verified = false

        // When
        List<Supplier> verifiedSuppliers = supplierRepository.findByVerified(true);
        List<Supplier> unverifiedSuppliers = supplierRepository.findByVerified(false);

        // Then
        assertTrue(verifiedSuppliers.stream().anyMatch(s -> s.getId().equals(saved.getId())));
        assertTrue(verifiedSuppliers.stream().anyMatch(s -> s.getName().equals("Tech Supplies Inc")));

        assertTrue(unverifiedSuppliers.stream().anyMatch(s -> s.getId().equals(savedUnverified.getId())));
        assertTrue(unverifiedSuppliers.stream().anyMatch(s -> s.getName().equals("Unverified Co")));
    }

    @Test
    @DisplayName("Should find suppliers by minimum rating")
    void testFindByRatingGreaterThanEqual() {
        // Given
        supplierRepository.save(testSupplier); // rating = 4.5

        Supplier lowRated = new Supplier(
            null, "Low Rated Co", "lowrated@test.com", null, null,
            null, null, "LIC99999", 2.0, false
        );
        supplierRepository.save(lowRated); // rating = 2.0

        // When
        List<Supplier> highRated = supplierRepository.findByRatingGreaterThanEqual(4.0);

        // Then
        assertEquals(1, highRated.size());
        assertEquals("Tech Supplies Inc", highRated.get(0).getName());
    }

    // ===== Delete Tests =====

    @Test
    @DisplayName("Should delete supplier by ID")
    void testDeleteById() {
        // Given
        Supplier saved = supplierRepository.save(testSupplier);
        Long supplierId = saved.getId();

        // When
        supplierRepository.deleteById(supplierId);

        // Then
        Optional<Supplier> found = supplierRepository.findById(supplierId);
        assertFalse(found.isPresent(), "Supplier should be deleted");
    }

    // ===== Count Tests =====

    @Test
    @DisplayName("Should count all suppliers")
    void testCount() {
        // Given
        long initialCount = supplierRepository.count();

        supplierRepository.save(testSupplier);

        Supplier another = new Supplier(
            null, "Another Co", "another@test.com", null, null,
            null, null, "LIC99999", null, false
        );
        supplierRepository.save(another);

        // Then
        assertEquals(initialCount + 2, supplierRepository.count());
    }

    @Test
    @DisplayName("Should count verified suppliers")
    void testCountByVerified() {
        // Given
        long initialVerifiedCount = supplierRepository.countByVerified(true);
        long initialUnverifiedCount = supplierRepository.countByVerified(false);

        supplierRepository.save(testSupplier); // verified = true

        Supplier unverified = new Supplier(
            null, "Unverified Co", "unverified2@test.com", null, null,
            null, null, "LIC88888", null, false
        );
        supplierRepository.save(unverified); // verified = false

        // Then
        assertEquals(initialVerifiedCount + 1, supplierRepository.countByVerified(true));
        assertEquals(initialUnverifiedCount + 1, supplierRepository.countByVerified(false));
    }

    // ===== Entity-Domain Mapping Tests =====

    @Test
    @DisplayName("Should correctly map all domain fields to entity")
    void testDomainToEntityMapping() {
        // Given - Create domain object with all fields
        Supplier domain = new Supplier(
            null,
            "Mapping Test Co",
            "mapping@test.com",
            "+1111111111",
            "123 Mapping St",
            "map.jpg",
            "Testing entity mapping",
            "LIC-MAP-123",
            3.8,
            true
        );

        // When - Save (triggers domain → entity mapping)
        Supplier saved = supplierRepository.save(domain);

        // Then - Retrieve and verify all fields
        Supplier retrieved = supplierRepository.findById(saved.getId()).get();

        assertNotNull(retrieved.getId());
        assertEquals("Mapping Test Co", retrieved.getName());
        assertEquals("mapping@test.com", retrieved.getEmail());
        assertEquals("+1111111111", retrieved.getPhone());
        assertEquals("123 Mapping St", retrieved.getAddress());
        assertEquals("map.jpg", retrieved.getProfilePicture());
        assertEquals("Testing entity mapping", retrieved.getProfileDescription());
        assertEquals("LIC-MAP-123", retrieved.getBusinessLicense());
        assertEquals(3.8, retrieved.getRating());
        assertTrue(retrieved.isVerified());
    }

    @Test
    @DisplayName("Should preserve domain object state after save")
    void testDomainObjectUnchangedAfterSave() {
        // Given
        String originalName = testSupplier.getName();
        String originalEmail = testSupplier.getEmail();

        // When
        Supplier saved = supplierRepository.save(testSupplier);

        // Then - Original domain object should be unchanged
        assertEquals(originalName, testSupplier.getName());
        assertEquals(originalEmail, testSupplier.getEmail());

        // But returned object should have ID
        assertNotNull(saved.getId());
    }
}
