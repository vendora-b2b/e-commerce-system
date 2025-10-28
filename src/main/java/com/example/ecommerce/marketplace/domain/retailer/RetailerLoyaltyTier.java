package com.example.ecommerce.marketplace.domain.retailer;

/**
 * Represents loyalty tier levels for retailers.
 * Tiers are determined by purchase history and loyalty points.
 */
public enum RetailerLoyaltyTier {
    /**
     * Bronze tier - Entry level (0-999 points or 0-9,999 purchase amount)
     */
    BRONZE("Bronze", 0, 0.0),

    /**
     * Silver tier - Mid level (1,000-4,999 points or 10,000-49,999 purchase amount)
     */
    SILVER("Silver", 1000, 10000.0),

    /**
     * Gold tier - Premium level (5,000+ points or 50,000+ purchase amount)
     */
    GOLD("Gold", 5000, 50000.0);

    private final String displayName;
    private final int minimumPoints;
    private final double minimumPurchaseAmount;

    RetailerLoyaltyTier(String displayName, int minimumPoints, double minimumPurchaseAmount) {
        this.displayName = displayName;
        this.minimumPoints = minimumPoints;
        this.minimumPurchaseAmount = minimumPurchaseAmount;
    }

    /**
     * Gets the human-readable display name for this tier.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the minimum loyalty points required for this tier.
     * @return the minimum points threshold
     */
    public int getMinimumPoints() {
        return minimumPoints;
    }

    /**
     * Gets the minimum purchase amount required for this tier.
     * @return the minimum purchase amount threshold
     */
    public double getMinimumPurchaseAmount() {
        return minimumPurchaseAmount;
    }

    /**
     * Calculates the appropriate loyalty tier based on points and purchase amount.
     * Uses the higher tier if either threshold is met.
     *
     * @param loyaltyPoints the retailer's loyalty points
     * @param purchaseAmount the retailer's total purchase amount
     * @return the appropriate loyalty tier
     */
    public static RetailerLoyaltyTier calculateTier(int loyaltyPoints, double purchaseAmount) {
        if (loyaltyPoints >= GOLD.minimumPoints || purchaseAmount >= GOLD.minimumPurchaseAmount) {
            return GOLD;
        } else if (loyaltyPoints >= SILVER.minimumPoints || purchaseAmount >= SILVER.minimumPurchaseAmount) {
            return SILVER;
        } else {
            return BRONZE;
        }
    }

    /**
     * Gets the discount percentage associated with this tier.
     * @return discount percentage (0.0 to 1.0)
     */
    public double getDiscountPercentage() {
        switch (this) {
            case BRONZE:
                return 0.0;
            case SILVER:
                return 0.05; // 5% discount
            case GOLD:
                return 0.10; // 10% discount
            default:
                return 0.0;
        }
    }
}
