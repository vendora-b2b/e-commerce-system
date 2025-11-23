package com.example.ecommerce.marketplace.web.model.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for updating inventory.
 * Used in PATCH /api/v1/products/{productId}/variants/{variantId}/inventory
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryRequest {

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity must be non-negative")
    private Integer availableQuantity;

    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    @Min(value = 0, message = "Reorder quantity must be non-negative")
    private Integer reorderQuantity;

    private String warehouseLocation;
}
