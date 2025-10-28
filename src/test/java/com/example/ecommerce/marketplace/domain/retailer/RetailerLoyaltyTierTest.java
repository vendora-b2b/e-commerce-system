package com.example.ecommerce.marketplace.domain.retailer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RetailerLoyaltyTier enum.
 */
class RetailerLoyaltyTierTest {

    // ===== Enum Constants Tests =====

    @Test
    @DisplayName("Should have three tier levels")
    void testTierLevels() {
        assertEquals(3, RetailerLoyaltyTier.values().length);
    }

    @Test
    @DisplayName("Should have BRONZE tier")
    void testBronzeTierExists() {
        assertNotNull(RetailerLoyaltyTier.BRONZE);
    }

    @Test
    @DisplayName("Should have SILVER tier")
    void testSilverTierExists() {
        assertNotNull(RetailerLoyaltyTier.SILVER);
    }

    @Test
    @DisplayName("Should have GOLD tier")
    void testGoldTierExists() {
        assertNotNull(RetailerLoyaltyTier.GOLD);
    }

    // ===== Display Name Tests =====

    @Test
    @DisplayName("BRONZE tier should have correct display name")
    void testBronzeDisplayName() {
        assertEquals("Bronze", RetailerLoyaltyTier.BRONZE.getDisplayName());
    }

    @Test
    @DisplayName("SILVER tier should have correct display name")
    void testSilverDisplayName() {
        assertEquals("Silver", RetailerLoyaltyTier.SILVER.getDisplayName());
    }

    @Test
    @DisplayName("GOLD tier should have correct display name")
    void testGoldDisplayName() {
        assertEquals("Gold", RetailerLoyaltyTier.GOLD.getDisplayName());
    }

    // ===== Minimum Points Tests =====

    @Test
    @DisplayName("BRONZE tier should have 0 minimum points")
    void testBronzeMinimumPoints() {
        assertEquals(0, RetailerLoyaltyTier.BRONZE.getMinimumPoints());
    }

    @Test
    @DisplayName("SILVER tier should have 1000 minimum points")
    void testSilverMinimumPoints() {
        assertEquals(1000, RetailerLoyaltyTier.SILVER.getMinimumPoints());
    }

    @Test
    @DisplayName("GOLD tier should have 5000 minimum points")
    void testGoldMinimumPoints() {
        assertEquals(5000, RetailerLoyaltyTier.GOLD.getMinimumPoints());
    }

    // ===== Minimum Purchase Amount Tests =====

    @Test
    @DisplayName("BRONZE tier should have 0.0 minimum purchase amount")
    void testBronzeMinimumPurchaseAmount() {
        assertEquals(0.0, RetailerLoyaltyTier.BRONZE.getMinimumPurchaseAmount());
    }

    @Test
    @DisplayName("SILVER tier should have 10000.0 minimum purchase amount")
    void testSilverMinimumPurchaseAmount() {
        assertEquals(10000.0, RetailerLoyaltyTier.SILVER.getMinimumPurchaseAmount());
    }

    @Test
    @DisplayName("GOLD tier should have 50000.0 minimum purchase amount")
    void testGoldMinimumPurchaseAmount() {
        assertEquals(50000.0, RetailerLoyaltyTier.GOLD.getMinimumPurchaseAmount());
    }

    // ===== Discount Percentage Tests =====

    @Test
    @DisplayName("BRONZE tier should have 0% discount")
    void testBronzeDiscountPercentage() {
        assertEquals(0.0, RetailerLoyaltyTier.BRONZE.getDiscountPercentage());
    }

    @Test
    @DisplayName("SILVER tier should have 5% discount")
    void testSilverDiscountPercentage() {
        assertEquals(0.05, RetailerLoyaltyTier.SILVER.getDiscountPercentage());
    }

    @Test
    @DisplayName("GOLD tier should have 10% discount")
    void testGoldDiscountPercentage() {
        assertEquals(0.10, RetailerLoyaltyTier.GOLD.getDiscountPercentage());
    }

    // ===== Calculate Tier Tests - Based on Points =====

    @Test
    @DisplayName("Should calculate BRONZE tier for 0 points and 0 purchases")
    void testCalculateTier_Bronze_Zero() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 0.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should calculate BRONZE tier for 999 points")
    void testCalculateTier_Bronze_MaxPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(999, 0.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for exactly 1000 points")
    void testCalculateTier_Silver_MinPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(1000, 0.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for 4999 points")
    void testCalculateTier_Silver_MaxPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(4999, 0.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier for exactly 5000 points")
    void testCalculateTier_Gold_MinPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(5000, 0.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier for 10000 points")
    void testCalculateTier_Gold_HighPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(10000, 0.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    // ===== Calculate Tier Tests - Based on Purchase Amount =====

    @Test
    @DisplayName("Should calculate BRONZE tier for 9999.99 purchase amount")
    void testCalculateTier_Bronze_MaxPurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 9999.99);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for exactly 10000.0 purchase amount")
    void testCalculateTier_Silver_MinPurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 10000.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for 49999.99 purchase amount")
    void testCalculateTier_Silver_MaxPurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 49999.99);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier for exactly 50000.0 purchase amount")
    void testCalculateTier_Gold_MinPurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 50000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier for 100000.0 purchase amount")
    void testCalculateTier_Gold_HighPurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, 100000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    // ===== Calculate Tier Tests - Mixed Criteria =====

    @Test
    @DisplayName("Should calculate SILVER tier when points qualify but purchase doesn't")
    void testCalculateTier_Silver_PointsQualify() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(1000, 5000.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier when purchase qualifies but points don't")
    void testCalculateTier_Silver_PurchaseQualifies() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(500, 10000.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier when points qualify but purchase doesn't")
    void testCalculateTier_Gold_PointsQualify() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(5000, 20000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier when purchase qualifies but points don't")
    void testCalculateTier_Gold_PurchaseQualifies() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(2000, 50000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier when both criteria exceed GOLD threshold")
    void testCalculateTier_Gold_BothQualify() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(10000, 100000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    @Test
    @DisplayName("Should calculate BRONZE tier when neither criteria qualify for higher tier")
    void testCalculateTier_Bronze_NeitherQualifies() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(500, 5000.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    // ===== Boundary Tests =====

    @Test
    @DisplayName("Should calculate BRONZE tier for just below SILVER points threshold")
    void testCalculateTier_BoundaryBelowSilverPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(999, 9999.99);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for exactly at SILVER threshold")
    void testCalculateTier_BoundaryAtSilverPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(1000, 10000.0);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate SILVER tier for just below GOLD points threshold")
    void testCalculateTier_BoundaryBelowGoldPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(4999, 49999.99);
        assertEquals(RetailerLoyaltyTier.SILVER, tier);
    }

    @Test
    @DisplayName("Should calculate GOLD tier for exactly at GOLD threshold")
    void testCalculateTier_BoundaryAtGoldPoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(5000, 50000.0);
        assertEquals(RetailerLoyaltyTier.GOLD, tier);
    }

    // ===== Negative Values Tests =====

    @Test
    @DisplayName("Should handle negative points as BRONZE tier")
    void testCalculateTier_NegativePoints() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(-100, 0.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should handle negative purchase amount as BRONZE tier")
    void testCalculateTier_NegativePurchase() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(0, -1000.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }

    @Test
    @DisplayName("Should handle both negative values as BRONZE tier")
    void testCalculateTier_BothNegative() {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.calculateTier(-100, -1000.0);
        assertEquals(RetailerLoyaltyTier.BRONZE, tier);
    }
}
