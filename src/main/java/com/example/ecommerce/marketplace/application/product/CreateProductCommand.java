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
    private final Long categoryId;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;
    private final Long supplierId;
    private final List<String> images;
    private final List<PriceTierDto> priceTiers;
    private final List<ProductVariantDto> variants;

    public CreateProductCommand(String sku, String name, String description, Long categoryId,
                                Double basePrice, Integer minimumOrderQuantity, Long supplierId,
                                List<String> images, List<PriceTierDto> priceTiers, List<ProductVariantDto> variants) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.supplierId = supplierId;
        this.images = images;
        this.priceTiers = priceTiers;
        this.variants = variants;
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

    public Long getCategoryId() {
        return categoryId;
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

    public List<PriceTierDto> getPriceTiers() {
        return priceTiers;
    }

    public List<ProductVariantDto> getVariants() {
        return variants;
    }

    /**
     * DTO for price tier information.
     */
    public static class PriceTierDto {
        private final Integer minQuantity;
        private final Integer maxQuantity;
        private final Double pricePerUnit;
        private final Double discountPercent;

        public PriceTierDto(Integer minQuantity, Integer maxQuantity, Double pricePerUnit, Double discountPercent) {
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.pricePerUnit = pricePerUnit;
            this.discountPercent = discountPercent;
        }

        public Integer getMinQuantity() {
            return minQuantity;
        }

        public Integer getMaxQuantity() {
            return maxQuantity;
        }

        public Double getPricePerUnit() {
            return pricePerUnit;
        }

        public Double getDiscountPercent() {
            return discountPercent;
        }
    }

    /**
     * DTO for product variant information.
     */
    public static class ProductVariantDto {
        private final String variantSku;
        private final String color;
        private final String size;
        private final Double priceAdjustment;
        private final List<String> images;

        public ProductVariantDto(String variantSku, String color, String size, Double priceAdjustment, List<String> images) {
            this.variantSku = variantSku;
            this.color = color;
            this.size = size;
            this.priceAdjustment = priceAdjustment;
            this.images = images;
        }

        public String getVariantSku() {
            return variantSku;
        }

        public String getColor() {
            return color;
        }

        public String getSize() {
            return size;
        }

        public Double getPriceAdjustment() {
            return priceAdjustment;
        }

        public List<String> getImages() {
            return images;
        }
    }
}
