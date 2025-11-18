package com.example.ecommerce.marketplace.domain.product;

/**
 * Represents a price tier for bulk pricing.
 * Defines discounted pricing based on quantity ranges.
 */
public class PriceTier {
    private Long id;
    private Integer minQuantity;
    private Integer maxQuantity; // null for unlimited
    private Double pricePerUnit;
    private Double discountPercent;

    public PriceTier() {
    }

    public PriceTier(Long id, Integer minQuantity, Integer maxQuantity, 
                    Double pricePerUnit, Double discountPercent) {
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.pricePerUnit = pricePerUnit;
        this.discountPercent = discountPercent;
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
     * Calculates total price for a quantity at this tier.
     * @param quantity the quantity
     * @return total price
     */
    public Double calculateTotalPrice(Integer quantity) {
        if (quantity == null || pricePerUnit == null) {
            return null;
        }
        return quantity * pricePerUnit;
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

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }
}
