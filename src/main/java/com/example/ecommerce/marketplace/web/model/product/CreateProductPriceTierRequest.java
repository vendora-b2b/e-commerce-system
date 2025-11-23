package com.example.ecommerce.marketplace.web.model.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a product price tier.
 * Contains tier information with validation constraints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductPriceTierRequest {
    
    @NotNull(message = "Minimum quantity is required")
    @Positive(message = "Minimum quantity must be positive")
    private Integer minQuantity;
    
    private Integer maxQuantity;
    
    @Min(value = 0, message = "Discount percent must be at least 0")
    @Max(value = 100, message = "Discount percent must not exceed 100")
    private Double discountPercent;
}
