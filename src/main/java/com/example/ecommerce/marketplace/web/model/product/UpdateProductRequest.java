package com.example.ecommerce.marketplace.web.model.product;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for updating an existing product.
 * Contains validation constraints at the API boundary.
 * All fields are optional - only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;

    private String description;

    private Long categoryId;

    @Positive(message = "Base price must be positive")
    private Double basePrice;

    @Positive(message = "Minimum order quantity must be positive")
    private Integer minimumOrderQuantity;
}
