package com.example.ecommerce.marketplace.web.model.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP request DTO for partially updating a product.
 * All fields are optional - only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;

    private String description;

    private List<Long> categoryIds;

    @Positive(message = "Base price must be greater than zero")
    private Double basePrice;

    @Positive(message = "Minimum order quantity must be greater than zero")
    private Integer minimumOrderQuantity;

    private String unit;

    private List<String> images;

    @Valid
    private List<PriceTierRequest> priceTiers;

    /**
     * Inner class representing a price tier in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceTierRequest {

        @Positive(message = "Minimum quantity must be greater than zero")
        private Integer minQuantity;

        private Integer maxQuantity;

        private Double discountPercent;
    }
}
