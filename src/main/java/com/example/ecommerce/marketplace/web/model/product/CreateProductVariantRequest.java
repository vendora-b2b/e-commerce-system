package com.example.ecommerce.marketplace.web.model.product;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a product variant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductVariantRequest {

    @NotBlank(message = "Please enter a SKU for this variant")
    private String sku;

    @NotBlank(message = "Please select or enter a color for this variant")
    private String color;

    @NotBlank(message = "Please select or enter a size for this variant")
    private String size;

    private Double priceAdjustment = 0.0;
}
