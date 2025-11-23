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

    @Valid
    private List<CategoryRequest> categories;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;

    @NotNull(message = "Minimum order quantity is required")
    @Positive(message = "Minimum order quantity must be positive")
    private Integer minimumOrderQuantity;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String unit;

    private List<String> images;

    private List<String> colors;

    private List<String> sizes;

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

        @NotNull(message = "Minimum quantity is required")
        @Positive(message = "Minimum quantity must be positive")
        private Integer minQuantity;

        private Integer maxQuantity;

        private Double discountPercent;
    }

    /**
     * Inner class representing a category in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRequest {

        @NotBlank(message = "Category name is required")
        private String name;

        @NotBlank(message = "Category slug is required")
        private String slug;
    }
}
