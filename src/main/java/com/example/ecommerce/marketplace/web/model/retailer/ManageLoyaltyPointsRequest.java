package com.example.ecommerce.marketplace.web.model.retailer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for managing retailer loyalty points.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManageLoyaltyPointsRequest {

    @NotNull(message = "Points amount is required")
    @Positive(message = "Points must be positive")
    private Integer points;

    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    /**
     * Enum for loyalty points operation types.
     */
    public enum OperationType {
        ADD,
        REDEEM
    }
}
