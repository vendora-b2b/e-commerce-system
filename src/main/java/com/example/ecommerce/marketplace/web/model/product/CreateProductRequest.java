package com.example.ecommerce.marketplace.web.model.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP request DTO for creating a new product.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    private Long categoryId;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;

    @NotNull(message = "Minimum order quantity is required")
    @Positive(message = "Minimum order quantity must be positive")
    private Integer minimumOrderQuantity;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private List<String> images;

    @Valid
    private List<PriceTierRequest> priceTiers;

    @Valid
    private List<ProductVariantRequest> variants;

    /**
     * Inner class representing a price tier in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceTierRequest {

        @NotNull(message = "Minimum quantity is required")
        @Positive(message = "Minimum quantity must be positive")
        private Integer minQuantity;

        private Integer maxQuantity;

        @NotNull(message = "Price per unit is required")
        @Positive(message = "Price per unit must be positive")
        private Double pricePerUnit;

        private Double discountPercent;
    }

    /**
     * Inner class representing a product variant in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantRequest {

        @NotBlank(message = "Variant SKU is required")
        private String variantSku;

        private String color;

        private String size;

        private Double priceAdjustment;

        private List<String> images;
    }
}
