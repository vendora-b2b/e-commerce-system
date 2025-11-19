package com.example.ecommerce.marketplace.application.product;

import java.util.List;

/**
 * Command object for updating product information.
 */
public class UpdateProductCommand {

    private final Long productId;
    private final String name;
    private final String description;
    private final List<Long> categoryIds;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;
    private final List<String> colors;
    private final List<String> sizes;

    public UpdateProductCommand(Long productId, String name, String description, List<Long> categoryIds,
                                Double basePrice, Integer minimumOrderQuantity,
                                List<String> colors, List<String> sizes) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.categoryIds = categoryIds;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.colors = colors;
        this.sizes = sizes;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public List<String> getColors() {
        return colors;
    }

    public List<String> getSizes() {
        return sizes;
    }
}
