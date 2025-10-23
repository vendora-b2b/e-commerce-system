package com.example.ecommerce.marketplace.domain.product;

import java.util.List;

/**
 * Represents a product entity in the e-commerce marketplace.
 * Products are offered by suppliers and can be purchased by retailers.
 */
public class Product {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private String category;
    private Long supplierId;
    private Double basePrice;
    private Integer minimumOrderQuantity;
    private String unit;
    private List<String> images;
    private List<ProductVariant> variants;
    private List<PriceTier> priceTiers;
    private String status; // ACTIVE, INACTIVE, DISCONTINUED

    /**
     * Checks if the order quantity meets the minimum requirement.
     * @param quantity the quantity to validate
     * @return true if meets minimum, false otherwise
     */
    public boolean meetsMinimumOrderQuantity(Integer quantity) {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Calculates the price for a given quantity considering price tiers.
     * Falls back to base price if no tiers are defined.
     * @param quantity the quantity to calculate price for
     * @return calculated total price
     */
    public Double calculatePriceForQuantity(Integer quantity) {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Gets the applicable price tier for a given quantity.
     * @param quantity the quantity to check
     * @return the applicable price tier, or null if no tiers apply
     */
    public PriceTier getPriceTierForQuantity(Integer quantity) {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Checks if the product has price tiers for bulk pricing.
     * @return true if price tiers exist, false otherwise
     */
    public boolean hasPriceTiers() {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Checks if the product has variants.
     * @return true if product has variants, false otherwise
     */
    public boolean hasVariants() {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Checks if the product is active.
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Activates the product (sets status to ACTIVE).
     */
    public void activate() {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Deactivates the product (sets status to INACTIVE).
     */
    public void deactivate() {
        // TODO: Not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Inner class representing a product variant.
     * A variant is a distinct version of a product that shares the same base model but differs in one or more attributes
     * (e.g., color, size, style, or price).
     */
    public static class ProductVariant {
        private Long id;
        private String variantName;
        private String variantValue;
        private Double priceAdjustment;
        private List<String> images;
    }

    /**
     * Inner class representing a price tier for bulk pricing.
     * Defines discounted pricing based on quantity ranges.
     */
    public static class PriceTier {
        private Integer minQuantity;
        private Integer maxQuantity; // null for unlimited
        private Double pricePerUnit;
        private Double discountPercent;
    }
}
