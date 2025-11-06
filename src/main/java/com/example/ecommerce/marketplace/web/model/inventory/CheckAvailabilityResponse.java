package com.example.ecommerce.marketplace.web.model.inventory;

import com.example.ecommerce.marketplace.application.inventory.CheckInventoryAvailabilityResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for inventory availability check.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailabilityResponse {

    private Boolean available;
    private Boolean sufficientStock;
    private Integer availableQuantity;
    private String status;
    private String message;

    /**
     * Creates a CheckAvailabilityResponse from a result.
     */
    public static CheckAvailabilityResponse fromResult(CheckInventoryAvailabilityResult result) {
        return new CheckAvailabilityResponse(
            result.isAvailable(),
            result.isSufficientStock(),
            result.getAvailableQuantity(),
            result.getStatus(),
            result.getMessage()
        );
    }
}
