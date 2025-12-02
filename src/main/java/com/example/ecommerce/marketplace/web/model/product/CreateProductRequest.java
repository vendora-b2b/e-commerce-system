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

    @NotBlank(message = "Please enter a SKU (Stock Keeping Unit) for this product")
    private String sku;

    @NotBlank(message = "Please enter a product name")
    private String name;

    private String description;

    @Valid
    private List<CategoryRequest> categories;

    @NotNull(message = "Please enter a base price for this product")
    @Positive(message = "Base price must be greater than zero")
    private Double basePrice;

    @NotNull(message = "Please enter a minimum order quantity")
    @Positive(message = "Minimum order quantity must be greater than zero")
    private Integer minimumOrderQuantity;

    @NotNull(message = "You must be logged in as a supplier to create a product")
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

        @NotNull(message = "Please enter a minimum quantity for this price tier")
        @Positive(message = "Minimum quantity must be greater than zero")
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

        @NotBlank(message = "Please enter a category name")
        private String name;

        @NotBlank(message = "Please enter a category slug (URL-friendly identifier)")
        private String slug;
    }
}
