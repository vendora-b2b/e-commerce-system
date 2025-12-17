package com.example.ecommerce.marketplace.domain.product;

/**
 * Represents a product variant.
 * A variant is a distinct version of a product that shares the same base model but differs in one or more attributes
 * (e.g., color, size, style, or price).
 */
public class ProductVariant {
    private Long id;
    private Long productId;
    private String sku;          // Unique SKU for this variant
    private String color;        // e.g., "Red", "Blue" (nullable)
    private String size;         // e.g., "Small", "Large" (nullable)
    private Double priceAdjustment; // Additional cost or discount

    public ProductVariant() {
    }

    public ProductVariant(Long id, Long productId, String sku, String color, String size,
                        Double priceAdjustment) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.priceAdjustment = priceAdjustment;
    }

    /**
     * Calculates the final price with variant adjustment.
     * @param basePrice the base product price
     * @return the adjusted price
     */
    public Double calculateAdjustedPrice(Double basePrice) {
        if (basePrice == null) {
            return null;
        }
        if (priceAdjustment == null) {
            return basePrice;
        }
        return basePrice + priceAdjustment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Double getPriceAdjustment() {
        return priceAdjustment;
    }

    public void setPriceAdjustment(Double priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }
}
