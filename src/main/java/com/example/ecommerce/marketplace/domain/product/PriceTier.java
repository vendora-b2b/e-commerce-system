package com.example.ecommerce.marketplace.domain.product;

/**
 * Represents a price tier for bulk pricing.
 * Defines discounted pricing based on quantity ranges.
 */
public class PriceTier {
    private Long id;
    private Integer minQuantity;
    private Integer maxQuantity; // null for unlimited
    private Double discountPercent;
    private java.time.LocalDateTime createdAt;

    public PriceTier() {
    }

    public PriceTier(Long id, Integer minQuantity, Integer maxQuantity, 
                    Double discountPercent) {
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.discountPercent = discountPercent;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public PriceTier(Long id, Integer minQuantity, Integer maxQuantity, 
                    Double discountPercent, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.discountPercent = discountPercent;
        this.createdAt = createdAt;
    }

    /**
     * Checks if a quantity falls within this tier's range.
     * @param quantity the quantity to check
     * @return true if quantity is in range, false otherwise
     */
    public boolean isApplicableForQuantity(Integer quantity) {
        if (quantity == null || minQuantity == null) {
            return false;
        }
        boolean meetsMin = quantity >= minQuantity;
        boolean meetsMax = maxQuantity == null || quantity <= maxQuantity;
        return meetsMin && meetsMax;
    }

    /**
     * Calculates total price for a quantity at this tier using base price and discount.
     * @param quantity the quantity
     * @param basePrice the base price per unit
     * @return total price
     */
    public Double calculateTotalPrice(Integer quantity, Double basePrice) {
        if (quantity == null || basePrice == null) {
            return null;
        }
        Double pricePerUnit = calculatePricePerUnit(basePrice);
        return quantity * pricePerUnit;
    }

    /**
     * Calculates the effective price per unit based on discount percent.
     * @param basePrice the base price per unit
     * @return the discounted price per unit
     */
    public Double calculatePricePerUnit(Double basePrice) {
        if (basePrice == null) {
            return null;
        }
        if (discountPercent == null || discountPercent == 0) {
            return basePrice;
        }
        return basePrice * (1 - discountPercent / 100.0);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
