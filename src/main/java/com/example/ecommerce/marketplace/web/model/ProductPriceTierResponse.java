package com.example.ecommerce.marketplace.web.model;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for PriceTier.
 * Represents a price tier in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceTierResponse {
    private Long id;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Double discountPercent;
    private java.time.LocalDateTime createdAt;

    /**
     * Creates response from domain model.
     */
    public static ProductPriceTierResponse fromDomain(PriceTier tier) {
        return new ProductPriceTierResponse(
            tier.getId(),
            tier.getMinQuantity(),
            tier.getMaxQuantity(),
            tier.getDiscountPercent(),
            tier.getCreatedAt()
        );
    }
}
