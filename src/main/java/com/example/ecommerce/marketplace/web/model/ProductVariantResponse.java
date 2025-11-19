package com.example.ecommerce.marketplace.web.model;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for ProductVariant.
 * Represents a product variant in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String color;
    private String size;
    private Double priceAdjustment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates response from domain model.
     */
    public static ProductVariantResponse fromDomain(ProductVariant variant) {
        return new ProductVariantResponse(
            variant.getId(),
            variant.getProductId(),
            variant.getSku(),
            variant.getColor(),
            variant.getSize(),
            variant.getPriceAdjustment(),
            variant.getCreatedAt(),
            variant.getUpdatedAt()
        );
    }
}
