package com.example.ecommerce.marketplace.application.product;

import java.util.List;

/**
 * Command object for creating a new product.
 * Contains all necessary data for product creation.
 */
public class CreateProductCommand {

    private final String sku;
    private final String name;
    private final String description;
    private final String category;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;
    private final Long supplierId;
    private final List<String> images;

    public CreateProductCommand(String sku, String name, String description, String category,
                                Double basePrice, Integer minimumOrderQuantity, Long supplierId,
                                List<String> images) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.supplierId = supplierId;
        this.images = images;
    }

    public String getSku() {
        return sku;
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

    public Long getSupplierId() {
        return supplierId;
    }

    public List<String> getImages() {
        return images;
    }
}
