package com.example.ecommerce.marketplace.application.product;

/**
 * Command object for updating product information.
 */
public class UpdateProductCommand {

    private final Long productId;
    private final String name;
    private final String description;
    private final String category;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;

    public UpdateProductCommand(Long productId, String name, String description, String category,
                                Double basePrice, Integer minimumOrderQuantity) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
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

    public String getCategory() {
        return category;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }
}
