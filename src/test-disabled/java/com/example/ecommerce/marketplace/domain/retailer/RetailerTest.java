package com.example.ecommerce.marketplace.domain.retailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Retailer domain entity.
 */
class RetailerTest {

    private Retailer retailer;

    @BeforeEach
    void setUp() {
        retailer = new Retailer();
        retailer.setId(1L);
        retailer.setName("Premium Retail Store");
        retailer.setEmail("contact@premiumretail.com");
        retailer.setPhone("+1234567890");
        retailer.setAddress("123 Retail Avenue, Commerce City");
        retailer.setProfilePicture("profile.jpg");
        retailer.setProfileDescription("Leading retail business");
        retailer.setBusinessLicense("BL12345678");
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setCreditLimit(10000.0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.setLoyaltyPoints(0);
    }

    // ===== Email Validation Tests =====

    @Test
    @DisplayName("Should validate correct email format")
    void testValidateEmail_ValidEmail() {
        assertTrue(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should reject null email")
    void testValidateEmail_NullEmail() {
        retailer.setEmail(null);
        assertFalse(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should reject empty email")
    void testValidateEmail_EmptyEmail() {
        retailer.setEmail("");
        assertFalse(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should reject email without @")
    void testValidateEmail_InvalidFormat_NoAt() {
        retailer.setEmail("invalidemail.com");
        assertFalse(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should reject email without domain")
    void testValidateEmail_InvalidFormat_NoDomain() {
        retailer.setEmail("invalid@");
        assertFalse(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should validate email with trimmed spaces")
    void testValidateEmail_WithSpaces() {
        retailer.setEmail("  contact@premiumretail.com  ");
        assertTrue(retailer.validateEmail());
    }

    @Test
    @DisplayName("Should validate email with plus sign")
    void testValidateEmail_WithPlusSign() {
        retailer.setEmail("contact+test@premiumretail.com");
        assertTrue(retailer.validateEmail());
    }

    // ===== Business License Validation Tests =====

    @Test
    @DisplayName("Should validate correct business license")
    void testValidateBusinessLicense_ValidLicense() {
        assertTrue(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject null business license")
    void testValidateBusinessLicense_NullLicense() {
        retailer.setBusinessLicense(null);
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject empty business license")
    void testValidateBusinessLicense_EmptyLicense() {
        retailer.setBusinessLicense("");
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license shorter than 8 characters")
    void testValidateBusinessLicense_TooShort() {
        retailer.setBusinessLicense("BL12345");
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license longer than 20 characters")
    void testValidateBusinessLicense_TooLong() {
        retailer.setBusinessLicense("BL123456789012345678901");
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license with special characters")
    void testValidateBusinessLicense_SpecialCharacters() {
        retailer.setBusinessLicense("BL@12345678");
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should reject business license with lowercase letters")
    void testValidateBusinessLicense_LowercaseLetters() {
        retailer.setBusinessLicense("bl12345678");
        assertFalse(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should validate business license at minimum length")
    void testValidateBusinessLicense_MinimumLength() {
        retailer.setBusinessLicense("BL123456");
        assertTrue(retailer.validateBusinessLicense());
    }

    @Test
    @DisplayName("Should validate business license at maximum length")
    void testValidateBusinessLicense_MaximumLength() {
        retailer.setBusinessLicense("BL123456789012345678");
        assertTrue(retailer.validateBusinessLicense());
    }

    // ===== Loyalty Tier Update Tests =====

    @Test
    @DisplayName("Should update tier to BRONZE with low points and purchases")
    void testUpdateLoyaltyTier_Bronze() {
        retailer.setLoyaltyPoints(500);
        retailer.setTotalPurchaseAmount(5000.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should update tier to SILVER with 1000 points")
    void testUpdateLoyaltyTier_Silver_ByPoints() {
        retailer.setLoyaltyPoints(1000);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.SILVER, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should update tier to SILVER with 10000 purchase amount")
    void testUpdateLoyaltyTier_Silver_ByPurchaseAmount() {
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(10000.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.SILVER, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should update tier to GOLD with 5000 points")
    void testUpdateLoyaltyTier_Gold_ByPoints() {
        retailer.setLoyaltyPoints(5000);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.GOLD, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should update tier to GOLD with 50000 purchase amount")
    void testUpdateLoyaltyTier_Gold_ByPurchaseAmount() {
        retailer.setLoyaltyPoints(0);
        retailer.setTotalPurchaseAmount(50000.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.GOLD, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should handle null loyalty points in tier calculation")
    void testUpdateLoyaltyTier_NullPoints() {
        retailer.setLoyaltyPoints(null);
        retailer.setTotalPurchaseAmount(5000.0);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should handle null purchase amount in tier calculation")
    void testUpdateLoyaltyTier_NullPurchaseAmount() {
        retailer.setLoyaltyPoints(500);
        retailer.setTotalPurchaseAmount(null);
        retailer.updateLoyaltyTier();
        assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier());
    }

    // ===== Update Profile Tests =====

    @Test
    @DisplayName("Should update all profile fields")
    void testUpdateProfile_AllFields() {
        retailer.updateProfile(
            "New Retail Name",
            "+9876543210",
            "456 New Address",
            "Updated business description"
        );

        assertEquals("New Retail Name", retailer.getName());
        assertEquals("+9876543210", retailer.getPhone());
        assertEquals("456 New Address", retailer.getAddress());
        assertEquals("Updated business description", retailer.getProfileDescription());
    }

    @Test
    @DisplayName("Should trim whitespace from profile fields")
    void testUpdateProfile_TrimWhitespace() {
        retailer.updateProfile(
            "  Trimmed Name  ",
            "  +1234567890  ",
            "  123 Street  ",
            "  Description  "
        );

        assertEquals("Trimmed Name", retailer.getName());
        assertEquals("+1234567890", retailer.getPhone());
        assertEquals("123 Street", retailer.getAddress());
        assertEquals("Description", retailer.getProfileDescription());
    }

    @Test
    @DisplayName("Should not update fields with null values")
    void testUpdateProfile_NullValues() {
        String originalName = retailer.getName();
        String originalPhone = retailer.getPhone();
        String originalAddress = retailer.getAddress();

        retailer.updateProfile(null, null, null, null);

        assertEquals(originalName, retailer.getName());
        assertEquals(originalPhone, retailer.getPhone());
        assertEquals(originalAddress, retailer.getAddress());
    }

    @Test
    @DisplayName("Should not update fields with empty strings")
    void testUpdateProfile_EmptyStrings() {
        String originalName = retailer.getName();
        String originalPhone = retailer.getPhone();
        String originalAddress = retailer.getAddress();

        retailer.updateProfile("", "", "", "");

        assertEquals(originalName, retailer.getName());
        assertEquals(originalPhone, retailer.getPhone());
        assertEquals(originalAddress, retailer.getAddress());
    }

    @Test
    @DisplayName("Should update only provided fields")
    void testUpdateProfile_PartialUpdate() {
        String originalPhone = retailer.getPhone();
        String originalAddress = retailer.getAddress();

        retailer.updateProfile("Updated Name", null, null, "Updated description");

        assertEquals("Updated Name", retailer.getName());
        assertEquals(originalPhone, retailer.getPhone());
        assertEquals(originalAddress, retailer.getAddress());
        assertEquals("Updated description", retailer.getProfileDescription());
    }

    // ===== Can Place Orders Tests =====

    @Test
    @DisplayName("Should allow orders with valid email and business license")
    void testCanPlaceOrders_Valid() {
        assertTrue(retailer.canPlaceOrders());
    }

    @Test
    @DisplayName("Should not allow orders with invalid email")
    void testCanPlaceOrders_InvalidEmail() {
        retailer.setEmail("invalid-email");
        assertFalse(retailer.canPlaceOrders());
    }

    @Test
    @DisplayName("Should not allow orders with invalid business license")
    void testCanPlaceOrders_InvalidBusinessLicense() {
        retailer.setBusinessLicense("invalid");
        assertFalse(retailer.canPlaceOrders());
    }

    @Test
    @DisplayName("Should not allow orders with both invalid email and license")
    void testCanPlaceOrders_BothInvalid() {
        retailer.setEmail("invalid");
        retailer.setBusinessLicense("invalid");
        assertFalse(retailer.canPlaceOrders());
    }

    // ===== Available Credit Tests =====

    @Test
    @DisplayName("Should have available credit for amount within limit")
    void testHasAvailableCredit_WithinLimit() {
        retailer.setCreditLimit(10000.0);
        assertTrue(retailer.hasAvailableCredit(5000.0));
    }

    @Test
    @DisplayName("Should have available credit for exact credit limit")
    void testHasAvailableCredit_ExactLimit() {
        retailer.setCreditLimit(10000.0);
        assertTrue(retailer.hasAvailableCredit(10000.0));
    }

    @Test
    @DisplayName("Should not have available credit for amount exceeding limit")
    void testHasAvailableCredit_ExceedsLimit() {
        retailer.setCreditLimit(10000.0);
        assertFalse(retailer.hasAvailableCredit(15000.0));
    }

    @Test
    @DisplayName("Should not have available credit with null credit limit")
    void testHasAvailableCredit_NullCreditLimit() {
        retailer.setCreditLimit(null);
        assertFalse(retailer.hasAvailableCredit(5000.0));
    }

    @Test
    @DisplayName("Should not have available credit for null amount")
    void testHasAvailableCredit_NullAmount() {
        retailer.setCreditLimit(10000.0);
        assertFalse(retailer.hasAvailableCredit(null));
    }

    @Test
    @DisplayName("Should not have available credit for zero amount")
    void testHasAvailableCredit_ZeroAmount() {
        retailer.setCreditLimit(10000.0);
        assertFalse(retailer.hasAvailableCredit(0.0));
    }

    @Test
    @DisplayName("Should not have available credit for negative amount")
    void testHasAvailableCredit_NegativeAmount() {
        retailer.setCreditLimit(10000.0);
        assertFalse(retailer.hasAvailableCredit(-5000.0));
    }

    // ===== Add Loyalty Points Tests =====

    @Test
    @DisplayName("Should add loyalty points successfully")
    void testAddLoyaltyPoints_Success() {
        retailer.setLoyaltyPoints(100);
        retailer.addLoyaltyPoints(50);
        assertEquals(150, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should add loyalty points when starting from null")
    void testAddLoyaltyPoints_FromNull() {
        retailer.setLoyaltyPoints(null);
        retailer.addLoyaltyPoints(100);
        assertEquals(100, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should update tier after adding points")
    void testAddLoyaltyPoints_UpdatesTier() {
        retailer.setLoyaltyPoints(900);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.addLoyaltyPoints(100);
        assertEquals(RetailerLoyaltyTier.SILVER, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should throw exception for null points")
    void testAddLoyaltyPoints_NullPoints() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.addLoyaltyPoints(null);
        });
        assertEquals("Points must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero points")
    void testAddLoyaltyPoints_ZeroPoints() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.addLoyaltyPoints(0);
        });
        assertEquals("Points must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative points")
    void testAddLoyaltyPoints_NegativePoints() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.addLoyaltyPoints(-50);
        });
        assertEquals("Points must be positive", exception.getMessage());
    }

    // ===== Redeem Loyalty Points Tests =====

    @Test
    @DisplayName("Should redeem points successfully")
    void testRedeemLoyaltyPoints_Success() {
        retailer.setLoyaltyPoints(100);
        boolean result = retailer.redeemLoyaltyPoints(50);
        assertTrue(result);
        assertEquals(50, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should redeem all available points")
    void testRedeemLoyaltyPoints_AllPoints() {
        retailer.setLoyaltyPoints(100);
        boolean result = retailer.redeemLoyaltyPoints(100);
        assertTrue(result);
        assertEquals(0, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should fail to redeem more points than available")
    void testRedeemLoyaltyPoints_InsufficientPoints() {
        retailer.setLoyaltyPoints(50);
        boolean result = retailer.redeemLoyaltyPoints(100);
        assertFalse(result);
        assertEquals(50, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should fail to redeem from null points")
    void testRedeemLoyaltyPoints_NullPoints() {
        retailer.setLoyaltyPoints(null);
        boolean result = retailer.redeemLoyaltyPoints(50);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to redeem null amount")
    void testRedeemLoyaltyPoints_NullAmount() {
        retailer.setLoyaltyPoints(100);
        boolean result = retailer.redeemLoyaltyPoints(null);
        assertFalse(result);
        assertEquals(100, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should fail to redeem zero points")
    void testRedeemLoyaltyPoints_ZeroAmount() {
        retailer.setLoyaltyPoints(100);
        boolean result = retailer.redeemLoyaltyPoints(0);
        assertFalse(result);
        assertEquals(100, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should fail to redeem negative points")
    void testRedeemLoyaltyPoints_NegativeAmount() {
        retailer.setLoyaltyPoints(100);
        boolean result = retailer.redeemLoyaltyPoints(-50);
        assertFalse(result);
        assertEquals(100, retailer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Should update tier after redeeming points")
    void testRedeemLoyaltyPoints_UpdatesTier() {
        retailer.setLoyaltyPoints(1000);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.SILVER);
        retailer.redeemLoyaltyPoints(100);
        // Should still be SILVER as 900 >= 1000 threshold
        assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier());
    }

    // ===== Record Purchase Tests =====

    @Test
    @DisplayName("Should record purchase successfully")
    void testRecordPurchase_Success() {
        retailer.setTotalPurchaseAmount(1000.0);
        retailer.recordPurchase(500.0);
        assertEquals(1500.0, retailer.getTotalPurchaseAmount());
    }

    @Test
    @DisplayName("Should record purchase when starting from null")
    void testRecordPurchase_FromNull() {
        retailer.setTotalPurchaseAmount(null);
        retailer.recordPurchase(1000.0);
        assertEquals(1000.0, retailer.getTotalPurchaseAmount());
    }

    @Test
    @DisplayName("Should update tier after recording purchase")
    void testRecordPurchase_UpdatesTier() {
        retailer.setTotalPurchaseAmount(9000.0);
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.recordPurchase(1000.0);
        assertEquals(RetailerLoyaltyTier.SILVER, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should throw exception for null purchase amount")
    void testRecordPurchase_NullAmount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.recordPurchase(null);
        });
        assertEquals("Purchase amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero purchase amount")
    void testRecordPurchase_ZeroAmount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.recordPurchase(0.0);
        });
        assertEquals("Purchase amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative purchase amount")
    void testRecordPurchase_NegativeAmount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            retailer.recordPurchase(-1000.0);
        });
        assertEquals("Purchase amount must be positive", exception.getMessage());
    }

    // ===== Get Discount Percentage Tests =====

    @Test
    @DisplayName("Should return 0% discount for BRONZE tier")
    void testGetDiscountPercentage_Bronze() {
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        assertEquals(0.0, retailer.getDiscountPercentage());
    }

    @Test
    @DisplayName("Should return 5% discount for SILVER tier")
    void testGetDiscountPercentage_Silver() {
        retailer.setLoyaltyTier(RetailerLoyaltyTier.SILVER);
        assertEquals(0.05, retailer.getDiscountPercentage());
    }

    @Test
    @DisplayName("Should return 10% discount for GOLD tier")
    void testGetDiscountPercentage_Gold() {
        retailer.setLoyaltyTier(RetailerLoyaltyTier.GOLD);
        assertEquals(0.10, retailer.getDiscountPercentage());
    }

    @Test
    @DisplayName("Should return 0% discount for null tier")
    void testGetDiscountPercentage_NullTier() {
        retailer.setLoyaltyTier(null);
        assertEquals(0.0, retailer.getDiscountPercentage());
    }

    // ===== Getters and Setters Tests =====

    @Test
    @DisplayName("Should get and set id correctly")
    void testGetSetId() {
        retailer.setId(999L);
        assertEquals(999L, retailer.getId());
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void testGetSetName() {
        retailer.setName("Test Retailer");
        assertEquals("Test Retailer", retailer.getName());
    }

    @Test
    @DisplayName("Should get and set email correctly")
    void testGetSetEmail() {
        retailer.setEmail("test@example.com");
        assertEquals("test@example.com", retailer.getEmail());
    }

    @Test
    @DisplayName("Should get and set phone correctly")
    void testGetSetPhone() {
        retailer.setPhone("+9999999999");
        assertEquals("+9999999999", retailer.getPhone());
    }

    @Test
    @DisplayName("Should get and set address correctly")
    void testGetSetAddress() {
        retailer.setAddress("Test Address");
        assertEquals("Test Address", retailer.getAddress());
    }

    @Test
    @DisplayName("Should get and set profile picture correctly")
    void testGetSetProfilePicture() {
        retailer.setProfilePicture("test.jpg");
        assertEquals("test.jpg", retailer.getProfilePicture());
    }

    @Test
    @DisplayName("Should get and set profile description correctly")
    void testGetSetProfileDescription() {
        retailer.setProfileDescription("Test Description");
        assertEquals("Test Description", retailer.getProfileDescription());
    }

    @Test
    @DisplayName("Should get and set business license correctly")
    void testGetSetBusinessLicense() {
        retailer.setBusinessLicense("TEST12345678");
        assertEquals("TEST12345678", retailer.getBusinessLicense());
    }

    @Test
    @DisplayName("Should get and set loyalty tier correctly")
    void testGetSetLoyaltyTier() {
        retailer.setLoyaltyTier(RetailerLoyaltyTier.GOLD);
        assertEquals(RetailerLoyaltyTier.GOLD, retailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should get and set credit limit correctly")
    void testGetSetCreditLimit() {
        retailer.setCreditLimit(50000.0);
        assertEquals(50000.0, retailer.getCreditLimit());
    }

    @Test
    @DisplayName("Should get and set total purchase amount correctly")
    void testGetSetTotalPurchaseAmount() {
        retailer.setTotalPurchaseAmount(25000.0);
        assertEquals(25000.0, retailer.getTotalPurchaseAmount());
    }

    @Test
    @DisplayName("Should get and set loyalty points correctly")
    void testGetSetLoyaltyPoints() {
        retailer.setLoyaltyPoints(5000);
        assertEquals(5000, retailer.getLoyaltyPoints());
    }
}
