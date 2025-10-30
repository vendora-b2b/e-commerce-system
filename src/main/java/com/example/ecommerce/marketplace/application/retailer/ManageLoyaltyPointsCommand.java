package com.example.ecommerce.marketplace.application.retailer;

/**
 * Command object for managing retailer loyalty points.
 * Supports both adding and redeeming points.
 */
public class ManageLoyaltyPointsCommand {

    public enum OperationType {
        ADD,
        REDEEM
    }

    private final Long retailerId;
    private final Integer points;
    private final OperationType operationType;

    public ManageLoyaltyPointsCommand(Long retailerId, Integer points, OperationType operationType) {
        this.retailerId = retailerId;
        this.points = points;
        this.operationType = operationType;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public Integer getPoints() {
        return points;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
