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
    private final List<CategoryDto> categories;
    private final Double basePrice;
    private final Integer minimumOrderQuantity;
    private final Long supplierId;
    private final String unit;
    private final List<String> images;
    private final List<String> colors;
    private final List<String> sizes;
    private final List<PriceTierDto> priceTiers;
    private final List<ProductVariantDto> variants;

    public CreateProductCommand(String sku, String name, String description, List<CategoryDto> categories,
                                Double basePrice, Integer minimumOrderQuantity, Long supplierId,
                                String unit, List<String> images, List<String> colors, List<String> sizes,
                                List<PriceTierDto> priceTiers, List<ProductVariantDto> variants) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.supplierId = supplierId;
        this.unit = unit;
        this.images = images;
        this.colors = colors;
        this.sizes = sizes;
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

    public List<CategoryDto> getCategories() {
        return categories;
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

    public String getUnit() {
        return unit;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getColors() {
        return colors;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public List<PriceTierDto> getPriceTiers() {
        return priceTiers;
    }

    public List<ProductVariantDto> getVariants() {
        return variants;
    }

    /**
     * DTO for category information.
     */
    public static class CategoryDto {
        private final String name;
        private final String slug;

        public CategoryDto(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
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
