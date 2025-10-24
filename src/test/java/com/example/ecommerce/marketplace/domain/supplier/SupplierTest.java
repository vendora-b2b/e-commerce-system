package com.example.ecommerce.marketplace.domain.supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Supplier domain entity.
 */

class SupplierTest {

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier(
            1L,
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

    // ===== Email Validation Tests =====

    @Test
    @DisplayName("Should validate correct email format")
    void testValidateEmail_ValidEmail() {
        assertTrue(supplier.validateEmail());
    }

    @Test
    @DisplayName("Should reject null email")
    void testValidateEmail_NullEmail() {
        supplier.setEmail(null);
        assertFalse(supplier.validateEmail());
    }

    @Test
    @DisplayName("Should reject empty email")
    void testValidateEmail_EmptyEmail() {
        supplier.setEmail("");
        assertFalse(supplier.validateEmail());
    }

    @Test
    @DisplayName("Should reject email without @")
    void testValidateEmail_InvalidFormat_NoAt() {
        supplier.setEmail("invalidemail.com");
        assertFalse(supplier.validateEmail());
    }

    @Test
    @DisplayName("Should reject email without domain")
    void testValidateEmail_InvalidFormat_NoDomain() {
        supplier.setEmail("invalid@");
        assertFalse(supplier.validateEmail());
    }

    @Test
    @DisplayName("Should validate email with trimmed spaces")
    void testValidateEmail_WithSpaces() {
        supplier.setEmail("  contact@techsupplies.com  ");
        assertTrue(supplier.validateEmail());
    }

    // ===== Business License Validation Tests =====

    @Test
    @DisplayName("Should validate correct business license")
    void testValidateBusinessLicense_ValidLicense() {
        assertTrue(supplier.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject null business license")
    void testValidateBusinessLicense_NullLicense() {
        supplier.setBusinessLicense(null);
        assertFalse(supplier.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject empty business license")
    void testValidateBusinessLicense_EmptyLicense() {
        supplier.setBusinessLicense("");
        assertFalse(supplier.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license shorter than 5 characters")
    void testValidateBusinessLicense_TooShort() {
        supplier.setBusinessLicense("L123");
        assertFalse(supplier.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license with special characters")
    void testValidateBusinessLicense_SpecialCharacters() {
        supplier.setBusinessLicense("LIC@12345");
        assertFalse(supplier.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should validate business license with hyphens")
    void testValidateBusinessLicense_WithHyphens() {
        supplier.setBusinessLicense("LIC-12345");
        assertTrue(supplier.validateBusinessLicense());
    }

    // ===== Rating Update Tests =====

    @Test
    @DisplayName("Should update rating with valid new rating")
    void testUpdateRating_ValidRating() {
        supplier.setRating(4.0);
        supplier.updateRating(5.0);
        assertEquals(4.5, supplier.getRating());
    }

    @Test
    @DisplayName("Should set rating when no existing rating")
    void testUpdateRating_NoExistingRating() {
        supplier.setRating(null);
        supplier.updateRating(4.5);
        assertEquals(4.5, supplier.getRating());
    }

    @Test
    @DisplayName("Should throw exception for null rating")
    void testUpdateRating_NullRating() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplier.updateRating(null);
        });
        assertEquals("New rating cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for rating below minimum")
    void testUpdateRating_BelowMinimum() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplier.updateRating(-1.0);
        });
        assertTrue(exception.getMessage().contains("Rating must be between"));
    }

    @Test
    @DisplayName("Should throw exception for rating above maximum")
    void testUpdateRating_AboveMaximum() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplier.updateRating(6.0);
        });
        assertTrue(exception.getMessage().contains("Rating must be between"));
    }

    @Test
    @DisplayName("Should accept minimum rating")
    void testUpdateRating_MinimumRating() {
        supplier.setRating(null);
        supplier.updateRating(0.0);
        assertEquals(0.0, supplier.getRating());
    }

    @Test
    @DisplayName("Should accept maximum rating")
    void testUpdateRating_MaximumRating() {
        supplier.setRating(null);
        supplier.updateRating(5.0);
        assertEquals(5.0, supplier.getRating());
    }

    @Test
    @DisplayName("Should round rating to 2 decimal places")
    void testUpdateRating_RoundingToTwoDecimals() {
        supplier.setRating(4.333);
        supplier.updateRating(4.666);
        assertEquals(4.5, supplier.getRating());
    }

    // ===== isVerified Tests =====

    @Test
    @DisplayName("Should return true when verified is true")
    void testIsVerified_True() {
        supplier.setVerified(true);
        assertTrue(supplier.isVerified());
    }

    @Test
    @DisplayName("Should return false when verified is false")
    void testIsVerified_False() {
        supplier.setVerified(false);
        assertFalse(supplier.isVerified());
    }

    @Test
    @DisplayName("Should return false when verified is null")
    void testIsVerified_Null() {
        supplier.setVerified(null);
        assertFalse(supplier.isVerified());
    }

    // ===== Update Profile Tests =====

    @Test
    @DisplayName("Should update all profile fields")
    void testUpdateProfile_AllFields() {
        supplier.updateProfile(
            "New Company Name",
            "+9876543210",
            "456 New Address",
            "Updated description"
        );

        assertEquals("New Company Name", supplier.getName());
        assertEquals("+9876543210", supplier.getPhone());
        assertEquals("456 New Address", supplier.getAddress());
        assertEquals("Updated description", supplier.getProfileDescription());
    }

    @Test
    @DisplayName("Should trim whitespace from profile fields")
    void testUpdateProfile_TrimWhitespace() {
        supplier.updateProfile(
            "  Trimmed Name  ",
            "  +1234567890  ",
            "  123 Street  ",
            "  Description  "
        );

        assertEquals("Trimmed Name", supplier.getName());
        assertEquals("+1234567890", supplier.getPhone());
        assertEquals("123 Street", supplier.getAddress());
        assertEquals("Description", supplier.getProfileDescription());
    }

    @Test
    @DisplayName("Should not update fields with null values")
    void testUpdateProfile_NullValues() {
        String originalName = supplier.getName();
        String originalPhone = supplier.getPhone();
        String originalAddress = supplier.getAddress();

        supplier.updateProfile(null, null, null, null);

        assertEquals(originalName, supplier.getName());
        assertEquals(originalPhone, supplier.getPhone());
        assertEquals(originalAddress, supplier.getAddress());
    }

    @Test
    @DisplayName("Should not update fields with empty strings")
    void testUpdateProfile_EmptyStrings() {
        String originalName = supplier.getName();
        String originalPhone = supplier.getPhone();
        String originalAddress = supplier.getAddress();

        supplier.updateProfile("", "", "", "");

        assertEquals(originalName, supplier.getName());
        assertEquals(originalPhone, supplier.getPhone());
        assertEquals(originalAddress, supplier.getAddress());
    }

    @Test
    @DisplayName("Should update only provided fields")
    void testUpdateProfile_PartialUpdate() {
        String originalPhone = supplier.getPhone();
        String originalAddress = supplier.getAddress();

        supplier.updateProfile("Updated Name", null, null, "Updated description");

        assertEquals("Updated Name", supplier.getName());
        assertEquals(originalPhone, supplier.getPhone());
        assertEquals(originalAddress, supplier.getAddress());
        assertEquals("Updated description", supplier.getProfileDescription());
    }

    // ===== Constructor Tests =====

    @Test
    @DisplayName("Should create supplier with all fields")
    void testConstructor_AllFields() {
        Supplier newSupplier = new Supplier(
            2L,
            "New Supplier",
            "new@supplier.com",
            "+1111111111",
            "789 Supplier St",
            "new.jpg",
            "New supplier description",
            "LIC99999",
            3.5,
            false
        );

        assertEquals(2L, newSupplier.getId());
        assertEquals("New Supplier", newSupplier.getName());
        assertEquals("new@supplier.com", newSupplier.getEmail());
        assertEquals("+1111111111", newSupplier.getPhone());
        assertEquals("789 Supplier St", newSupplier.getAddress());
        assertEquals("new.jpg", newSupplier.getProfilePicture());
        assertEquals("New supplier description", newSupplier.getProfileDescription());
        assertEquals("LIC99999", newSupplier.getBusinessLicense());
        assertEquals(3.5, newSupplier.getRating());
        assertFalse(newSupplier.getVerified());
    }

    @Test
    @DisplayName("Should create supplier with default constructor")
    void testConstructor_Default() {
        Supplier newSupplier = new Supplier();
        assertNotNull(newSupplier);
    }
}
