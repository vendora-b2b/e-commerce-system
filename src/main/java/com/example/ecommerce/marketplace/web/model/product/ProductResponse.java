package com.example.ecommerce.marketplace.web.model.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP response DTO for product information.
 * Represents a product entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private List<CategoryResponse> categories;
    private Long supplierId;
    private Double basePrice;
    private Integer minimumOrderQuantity;
    private String unit;
    private List<String> images;
    private List<ProductVariantResponse> variants;
    private List<PriceTierResponse> priceTiers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a ProductResponse from a domain Product entity.
     */
    public static ProductResponse fromDomain(Product product) {
        List<ProductVariantResponse> variantResponses = null;
        if (product.getVariants() != null) {
            variantResponses = product.getVariants().stream()
                .map(ProductVariantResponse::fromDomain)
                .collect(Collectors.toList());
        }

        List<PriceTierResponse> priceTierResponses = null;
        if (product.getPriceTiers() != null) {
            priceTierResponses = product.getPriceTiers().stream()
                .map(PriceTierResponse::fromDomain)
                .collect(Collectors.toList());
        }

        List<CategoryResponse> categoryResponses = null;
        if (product.getCategories() != null) {
            categoryResponses = product.getCategories().stream()
                .map(CategoryResponse::fromDomain)
                .collect(Collectors.toList());
        }

        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            categoryResponses,
            product.getSupplierId(),
            product.getBasePrice(),
            product.getMinimumOrderQuantity(),
            product.getUnit(),
            product.getImages(),
            variantResponses,
            priceTierResponses,
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    /**
     * Inner class representing a product variant in the response.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantResponse {

        private Long id;
        private Long productId;
        private String sku;
        private String color;
        private String size;
        private Double priceAdjustment;
        private List<String> images;

        /**
         * Creates a ProductVariantResponse from a domain ProductVariant entity.
         */
        public static ProductVariantResponse fromDomain(ProductVariant variant) {
            return new ProductVariantResponse(
                variant.getId(),
                variant.getProductId(),
                variant.getSku(),
                variant.getColor(),
                variant.getSize(),
                variant.getPriceAdjustment(),
                variant.getImages()
            );
        }
    }

    /**
     * Inner class representing a price tier in the response.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceTierResponse {

        private Long id;
        private Integer minQuantity;
        private Integer maxQuantity;
        private Double discountPercent;

        /**
         * Creates a PriceTierResponse from a domain PriceTier entity.
         */
        public static PriceTierResponse fromDomain(PriceTier priceTier) {
            return new PriceTierResponse(
                priceTier.getId(),
                priceTier.getMinQuantity(),
                priceTier.getMaxQuantity(),
                priceTier.getDiscountPercent()
            );
        }
    }

    /**
     * Inner class representing a category in the response.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {

        private Long id;
        private String name;
        private String slug;

        /**
         * Creates a CategoryResponse from a domain Category entity.
         */
        public static CategoryResponse fromDomain(Category category) {
            return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
            );
        }
    }
}
