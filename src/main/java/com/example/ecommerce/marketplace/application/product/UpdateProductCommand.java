package com.example.ecommerce.marketplace.application.product;

import java.util.List;

/**
 * Command object for updating product information.
 */
public class UpdateProductCommand {

    private final Long productId;
    private final String sku;
    private final String name;
    private final String description;
    private final List<Long> categoryIds;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;
    private final String unit;
    private final List<String> images;
    private final List<PriceTierDto> priceTiers;

    public UpdateProductCommand(Long productId, String sku, String name, String description, List<Long> categoryIds,
                                Double basePrice, Integer minimumOrderQuantity, String unit,
                                List<String> images, List<PriceTierDto> priceTiers) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryIds = categoryIds;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.unit = unit;
        this.images = images;
        this.priceTiers = priceTiers;
    }

    public Long getProductId() {
        return productId;
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

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public List<String> getImages() {
        return images;
    }

    public List<PriceTierDto> getPriceTiers() {
        return priceTiers;
    }

    /**
     * DTO for price tier information.
     */
    public static class PriceTierDto {
        private final Integer minQuantity;
        private final Integer maxQuantity;
        private final Double discountPercent;

        public PriceTierDto(Integer minQuantity, Integer maxQuantity, Double discountPercent) {
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.discountPercent = discountPercent;
        }

        public Integer getMinQuantity() {
            return minQuantity;
        }

        public Integer getMaxQuantity() {
            return maxQuantity;
        }

        public Double getDiscountPercent() {
            return discountPercent;
        }
    }
}
