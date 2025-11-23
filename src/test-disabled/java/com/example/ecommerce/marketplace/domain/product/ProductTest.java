package com.example.ecommerce.marketplace.domain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Product domain entity.
 * Tests all domain behaviors, validations, and business rules.
 */
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("PROD-12345");
        product.setName("Test Product");
        product.setDescription("Sample product description");
        product.setCategoryId(1L);
        product.setSupplierId(10L);
        product.setBasePrice(100.0);
        product.setMinimumOrderQuantity(5);
        product.setUnit("pcs");
        product.setStatus("ACTIVE");
    }

    // ===== SKU Validation Tests =====

    @Test
    @DisplayName("Should validate correct SKU format")
    void testValidateSku_ValidSku() {
        assertTrue(product.validateSku());
    }

    @Test
    @DisplayName("Should reject null SKU")
    void testValidateSku_NullSku() {
        product.setSku(null);
        assertFalse(product.validateSku());
    }

    @Test
    @DisplayName("Should reject empty SKU")
    void testValidateSku_EmptySku() {
        product.setSku("");
        assertFalse(product.validateSku());
    }

    @Test
    @DisplayName("Should reject SKU shorter than 5 characters")
    void testValidateSku_TooShort() {
        product.setSku("AB12");
        assertFalse(product.validateSku());
    }

    @Test
    @DisplayName("Should reject SKU with lowercase characters")
    void testValidateSku_Lowercase() {
        product.setSku("prod-12345");
        assertFalse(product.validateSku());
    }

    @Test
    @DisplayName("Should reject SKU with special characters")
    void testValidateSku_SpecialCharacters() {
        product.setSku("PROD@12345");
        assertFalse(product.validateSku());
    }

    @Test
    @DisplayName("Should validate SKU with hyphens")
    void testValidateSku_WithHyphens() {
        product.setSku("PROD-12-345");
        assertTrue(product.validateSku());
    }

    @Test
    @DisplayName("Should reject SKU longer than 50 characters")
    void testValidateSku_TooLong() {
        product.setSku("PROD-1234567890123456789012345678901234567890123456");
        assertFalse(product.validateSku());
    }

    // ===== Name Validation Tests =====

    @Test
    @DisplayName("Should validate correct name")
    void testValidateName_ValidName() {
        assertTrue(product.validateName());
    }

    @Test
    @DisplayName("Should reject null name")
    void testValidateName_NullName() {
        product.setName(null);
        assertFalse(product.validateName());
    }

    @Test
    @DisplayName("Should reject empty name")
    void testValidateName_EmptyName() {
        product.setName("");
        assertFalse(product.validateName());
    }

    @Test
    @DisplayName("Should reject name shorter than 3 characters")
    void testValidateName_TooShort() {
        product.setName("AB");
        assertFalse(product.validateName());
    }

    @Test
    @DisplayName("Should reject name longer than 200 characters")
    void testValidateName_TooLong() {
        product.setName("A".repeat(201));
        assertFalse(product.validateName());
    }

    @Test
    @DisplayName("Should validate name with trimmed spaces")
    void testValidateName_WithSpaces() {
        product.setName("  Valid Product Name  ");
        assertTrue(product.validateName());
    }

    // ===== Price Validation Tests =====

    @Test
    @DisplayName("Should validate correct price")
    void testValidatePrice_ValidPrice() {
        assertTrue(product.validatePrice());
    }

    @Test
    @DisplayName("Should reject null price")
    void testValidatePrice_NullPrice() {
        product.setBasePrice(null);
        assertFalse(product.validatePrice());
    }

    @Test
    @DisplayName("Should reject zero price")
    void testValidatePrice_ZeroPrice() {
        product.setBasePrice(0.0);
        assertFalse(product.validatePrice());
    }

    @Test
    @DisplayName("Should reject negative price")
    void testValidatePrice_NegativePrice() {
        product.setBasePrice(-10.0);
        assertFalse(product.validatePrice());
    }

    @Test
    @DisplayName("Should accept minimum price")
    void testValidatePrice_MinimumPrice() {
        product.setBasePrice(0.01);
        assertTrue(product.validatePrice());
    }

    // ===== Minimum Order Quantity Validation Tests =====

    @Test
    @DisplayName("Should validate correct MOQ")
    void testValidateMinimumOrderQuantity_ValidMoq() {
        assertTrue(product.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject null MOQ")
    void testValidateMinimumOrderQuantity_NullMoq() {
        product.setMinimumOrderQuantity(null);
        assertFalse(product.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject zero MOQ")
    void testValidateMinimumOrderQuantity_ZeroMoq() {
        product.setMinimumOrderQuantity(0);
        assertFalse(product.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject negative MOQ")
    void testValidateMinimumOrderQuantity_NegativeMoq() {
        product.setMinimumOrderQuantity(-5);
        assertFalse(product.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should accept minimum MOQ value")
    void testValidateMinimumOrderQuantity_MinimumMoq() {
        product.setMinimumOrderQuantity(1);
        assertTrue(product.validateMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should reject MOQ above maximum")
    void testValidateMinimumOrderQuantity_AboveMaximum() {
        product.setMinimumOrderQuantity(1000001);
        assertFalse(product.validateMinimumOrderQuantity());
    }

    // ===== Supplier ID Validation Tests =====

    @Test
    @DisplayName("Should validate correct supplier ID")
    void testValidateSupplierId_ValidId() {
        assertTrue(product.validateSupplierId());
    }

    @Test
    @DisplayName("Should reject null supplier ID")
    void testValidateSupplierId_NullId() {
        product.setSupplierId(null);
        assertFalse(product.validateSupplierId());
    }

    @Test
    @DisplayName("Should reject zero supplier ID")
    void testValidateSupplierId_ZeroId() {
        product.setSupplierId(0L);
        assertFalse(product.validateSupplierId());
    }

    @Test
    @DisplayName("Should reject negative supplier ID")
    void testValidateSupplierId_NegativeId() {
        product.setSupplierId(-1L);
        assertFalse(product.validateSupplierId());
    }

    // ===== Minimum Order Quantity Check Tests =====

    @Test
    @DisplayName("Should meet minimum order quantity")
    void testMeetsMinimumOrderQuantity_Success() {
        assertTrue(product.meetsMinimumOrderQuantity(5));
        assertTrue(product.meetsMinimumOrderQuantity(10));
    }

    @Test
    @DisplayName("Should not meet minimum order quantity")
    void testMeetsMinimumOrderQuantity_Failure() {
        assertFalse(product.meetsMinimumOrderQuantity(4));
        assertFalse(product.meetsMinimumOrderQuantity(1));
    }

    @Test
    @DisplayName("Should handle null quantity check")
    void testMeetsMinimumOrderQuantity_NullQuantity() {
        assertFalse(product.meetsMinimumOrderQuantity(null));
    }

    @Test
    @DisplayName("Should handle null MOQ")
    void testMeetsMinimumOrderQuantity_NullMoq() {
        product.setMinimumOrderQuantity(null);
        assertFalse(product.meetsMinimumOrderQuantity(10));
    }

    // ===== Price Calculation Tests =====

    @Test
    @DisplayName("Should calculate price without tiers")
    void testCalculatePriceForQuantity_NoTiers() {
        Double total = product.calculatePriceForQuantity(10);
        assertEquals(1000.0, total);
    }

    @Test
    @DisplayName("Should calculate price with tiers")
    void testCalculatePriceForQuantity_WithTiers() {
        PriceTier tier1 = new PriceTier(null, 10, 49, 90.0, 10.0);
        PriceTier tier2 = new PriceTier(null, 50, null, 80.0, 20.0);
        product.addPriceTier(tier1);
        product.addPriceTier(tier2);

        // Quantity 10 should use tier1 (90.0 per unit)
        assertEquals(900.0, product.calculatePriceForQuantity(10));

        // Quantity 50 should use tier2 (80.0 per unit)
        assertEquals(4000.0, product.calculatePriceForQuantity(50));
    }

    @Test
    @DisplayName("Should throw exception for null quantity")
    void testCalculatePriceForQuantity_NullQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.calculatePriceForQuantity(null);
        });
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void testCalculatePriceForQuantity_ZeroQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.calculatePriceForQuantity(0);
        });
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for quantity below MOQ")
    void testCalculatePriceForQuantity_BelowMoq() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.calculatePriceForQuantity(3);
        });
        assertTrue(exception.getMessage().contains("does not meet minimum order quantity"));
    }

    // ===== Price Tier Tests =====

    @Test
    @DisplayName("Should report no price tiers")
    void testHasPriceTiers_False() {
        assertFalse(product.hasPriceTiers());
    }

    @Test
    @DisplayName("Should report has price tiers")
    void testHasPriceTiers_True() {
        product.addPriceTier(new PriceTier(null, 10, 49, 90.0, 10.0));
        assertTrue(product.hasPriceTiers());
    }

    @Test
    @DisplayName("Should get correct price tier for quantity")
    void testGetPriceTierForQuantity() {
        PriceTier tier1 = new PriceTier(null, 10, 49, 90.0, 10.0);
        PriceTier tier2 = new PriceTier(null, 50, 99, 80.0, 20.0);
        product.addPriceTier(tier1);
        product.addPriceTier(tier2);

        assertEquals(tier1, product.getPriceTierForQuantity(25));
        assertEquals(tier2, product.getPriceTierForQuantity(75));
        assertNull(product.getPriceTierForQuantity(5));
    }

    @Test
    @DisplayName("Should add price tier")
    void testAddPriceTier() {
        PriceTier tier = new PriceTier(null, 10, 49, 90.0, 10.0);
        product.addPriceTier(tier);
        assertTrue(product.hasPriceTiers());
    }

    @Test
    @DisplayName("Should throw exception when adding null price tier")
    void testAddPriceTier_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.addPriceTier(null);
        });
    }

    @Test
    @DisplayName("Should remove price tier")
    void testRemovePriceTier() {
        PriceTier tier = new PriceTier(null, 10, 49, 90.0, 10.0);
        product.addPriceTier(tier);
        assertTrue(product.hasPriceTiers());
        product.removePriceTier(tier);
        assertFalse(product.hasPriceTiers());
    }

    @Test
    @DisplayName("Should clear all price tiers")
    void testClearPriceTiers() {
        product.addPriceTier(new PriceTier(null, 10, 49, 90.0, 10.0));
        product.addPriceTier(new PriceTier(null, 50, 99, 80.0, 20.0));
        assertTrue(product.hasPriceTiers());
        product.clearPriceTiers();
        assertFalse(product.hasPriceTiers());
    }

    // ===== Variant Tests =====

    @Test
    @DisplayName("Should report no variants")
    void testHasVariants_False() {
        assertFalse(product.hasVariants());
    }

    @Test
    @DisplayName("Should report has variants")
    void testHasVariants_True() {
        product.addVariant(new ProductVariant(1L, 1L, "VAR-001", "Red", null, 0.0, null));
        assertTrue(product.hasVariants());
    }

    @Test
    @DisplayName("Should add variant")
    void testAddVariant() {
        ProductVariant variant = new ProductVariant(1L, 1L, "VAR-002", null, "Large", 5.0, null);
        product.addVariant(variant);
        assertTrue(product.hasVariants());
    }

    @Test
    @DisplayName("Should throw exception when adding null variant")
    void testAddVariant_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.addVariant(null);
        });
    }

    @Test
    @DisplayName("Should remove variant")
    void testRemoveVariant() {
        ProductVariant variant = new ProductVariant(1L, 1L, "VAR-003", "Blue", null, 0.0, null);
        product.addVariant(variant);
        assertTrue(product.hasVariants());
        product.removeVariant(variant);
        assertFalse(product.hasVariants());
    }

    // ===== Status Tests =====

    @Test
    @DisplayName("Should be active")
    void testIsActive_True() {
        assertTrue(product.isActive());
    }

    @Test
    @DisplayName("Should not be active")
    void testIsActive_False() {
        product.setStatus("INACTIVE");
        assertFalse(product.isActive());
    }

    @Test
    @DisplayName("Should activate product")
    void testActivate() {
        product.setStatus("INACTIVE");
        product.activate();
        assertTrue(product.isActive());
    }

    @Test
    @DisplayName("Should deactivate product")
    void testDeactivate() {
        product.deactivate();
        assertFalse(product.isActive());
        assertEquals("INACTIVE", product.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when activating discontinued product")
    void testActivate_Discontinued() {
        product.setStatus("DISCONTINUED");
        assertThrows(IllegalStateException.class, () -> {
            product.activate();
        });
    }

    @Test
    @DisplayName("Should throw exception when deactivating discontinued product")
    void testDeactivate_Discontinued() {
        product.setStatus("DISCONTINUED");
        assertThrows(IllegalStateException.class, () -> {
            product.deactivate();
        });
    }

    @Test
    @DisplayName("Should discontinue product")
    void testDiscontinue() {
        product.discontinue();
        assertTrue(product.isDiscontinued());
        assertEquals("DISCONTINUED", product.getStatus());
    }

    @Test
    @DisplayName("Should check if discontinued")
    void testIsDiscontinued() {
        assertFalse(product.isDiscontinued());
        product.setStatus("DISCONTINUED");
        assertTrue(product.isDiscontinued());
    }

    // ===== Update Product Info Tests =====

    @Test
    @DisplayName("Should update all product info fields")
    void testUpdateProductInfo_AllFields() {
        product.updateProductInfo("New Name", "New Description", 2L, "kg");
        assertEquals("New Name", product.getName());
        assertEquals("New Description", product.getDescription());
        assertEquals(2L, product.getCategoryId());
        assertEquals("kg", product.getUnit());
    }

    @Test
    @DisplayName("Should trim whitespace from updated fields")
    void testUpdateProductInfo_TrimWhitespace() {
        product.updateProductInfo("  New Name  ", "  New Desc  ", 3L, "  box  ");
        assertEquals("New Name", product.getName());
        assertEquals("New Desc", product.getDescription());
        assertEquals(3L, product.getCategoryId());
        assertEquals("box", product.getUnit());
    }

    @Test
    @DisplayName("Should not update fields with null values")
    void testUpdateProductInfo_NullValues() {
        String originalName = product.getName();
        Long originalCategoryId = product.getCategoryId();
        product.updateProductInfo(null, "New Desc", null, null);
        assertEquals(originalName, product.getName());
        assertEquals(originalCategoryId, product.getCategoryId());
        assertEquals("New Desc", product.getDescription());
    }

    @Test
    @DisplayName("Should not update fields with empty strings")
    void testUpdateProductInfo_EmptyStrings() {
        String originalName = product.getName();
        product.updateProductInfo("", "New Desc", null, "");
        assertEquals(originalName, product.getName());
        assertEquals("New Desc", product.getDescription());
    }

    // ===== Update Base Price Tests =====

    @Test
    @DisplayName("Should update base price")
    void testUpdateBasePrice_Success() {
        product.updateBasePrice(150.0);
        assertEquals(150.0, product.getBasePrice());
    }

    @Test
    @DisplayName("Should throw exception for null price")
    void testUpdateBasePrice_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.updateBasePrice(null);
        });
    }

    @Test
    @DisplayName("Should throw exception for price below minimum")
    void testUpdateBasePrice_BelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.updateBasePrice(0.0);
        });
    }

    // ===== Update MOQ Tests =====

    @Test
    @DisplayName("Should update MOQ")
    void testUpdateMinimumOrderQuantity_Success() {
        product.updateMinimumOrderQuantity(10);
        assertEquals(10, product.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should throw exception for null MOQ")
    void testUpdateMinimumOrderQuantity_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.updateMinimumOrderQuantity(null);
        });
    }

    @Test
    @DisplayName("Should throw exception for MOQ below minimum")
    void testUpdateMinimumOrderQuantity_BelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.updateMinimumOrderQuantity(0);
        });
    }

    @Test
    @DisplayName("Should throw exception for MOQ above maximum")
    void testUpdateMinimumOrderQuantity_AboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> {
            product.updateMinimumOrderQuantity(1000001);
        });
    }

    // ===== Image Management Tests =====

    @Test
    @DisplayName("Should add image")
    void testAddImage() {
        product.addImage("new-image.jpg");
        assertTrue(product.getImages().contains("new-image.jpg"));
    }

    @Test
    @DisplayName("Should not add duplicate image")
    void testAddImage_Duplicate() {
        product.addImage("img.jpg");
        product.addImage("img.jpg");
        assertEquals(1, product.getImages().stream().filter(img -> img.equals("img.jpg")).count());
    }

    @Test
    @DisplayName("Should trim image URL")
    void testAddImage_Trim() {
        product.addImage("  image.jpg  ");
        assertTrue(product.getImages().contains("image.jpg"));
    }

    @Test
    @DisplayName("Should not add null or empty image")
    void testAddImage_NullOrEmpty() {
        int originalSize = product.getImages().size();
        product.addImage(null);
        product.addImage("");
        product.addImage("   ");
        assertEquals(originalSize, product.getImages().size());
    }

    @Test
    @DisplayName("Should remove image")
    void testRemoveImage() {
        product.addImage("to-remove.jpg");
        assertTrue(product.getImages().contains("to-remove.jpg"));
        product.removeImage("to-remove.jpg");
        assertFalse(product.getImages().contains("to-remove.jpg"));
    }

    // ===== Constructor Tests =====

    @Test
    @DisplayName("Should create product with default constructor")
    void testConstructor_Default() {
        Product p = new Product();
        assertNotNull(p);
        assertNotNull(p.getImages());
        assertNotNull(p.getVariants());
        assertNotNull(p.getPriceTiers());
    }

    @Test
    @DisplayName("Should create product with full constructor")
    void testConstructor_Full() {
        LocalDateTime now = LocalDateTime.now();
        Product p = new Product(
            1L, "SKU-123", "Product", "Description", 1L,
            10L, 50.0, 5, "pcs",
            Arrays.asList("img1.jpg", "img2.jpg"),
            null, null,
            "ACTIVE", now, now
        );
        assertEquals(1L, p.getId());
        assertEquals("SKU-123", p.getSku());
        assertEquals("Product", p.getName());
        assertEquals(2, p.getImages().size());
    }

}
