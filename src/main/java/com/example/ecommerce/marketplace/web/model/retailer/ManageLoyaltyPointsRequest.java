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

    @NotNull(message = "Please enter the number of points")
    @Positive(message = "Points must be greater than zero")
    private Integer points;

    @NotNull(message = "Please select an operation type (ADD or REDEEM)")
    private OperationType operationType;

    /**
     * Enum for loyalty points operation types.
     */
    public enum OperationType {
        ADD,
        REDEEM
    }
}
