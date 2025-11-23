package com.example.ecommerce.marketplace.web.model.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating a product variant (partial update).
 * All fields are optional - only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVariantRequest {
    private String sku;
    private String color;
    private String size;
    private Double priceAdjustment;
}
