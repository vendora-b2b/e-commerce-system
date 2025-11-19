package com.example.ecommerce.marketplace.domain.product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> images;
    private LocalDateTime createdAt;

    public ProductVariant() {
        this.images = new ArrayList<>();
    }

    public ProductVariant(Long id, Long productId, String sku, String color, String size,
                        Double priceAdjustment, List<String> images, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.priceAdjustment = priceAdjustment;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.createdAt = createdAt;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
