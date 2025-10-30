package com.example.ecommerce.marketplace.application.retailer;

import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;

/**
 * Result object returned after loyalty points management attempt.
 */
public class ManageLoyaltyPointsResult {

    private final boolean success;
    private final Long retailerId;
    private final Integer newPointsBalance;
    private final RetailerLoyaltyTier newTier;
    private final String message;
    private final String errorCode;

    private ManageLoyaltyPointsResult(boolean success, Long retailerId, Integer newPointsBalance,
                                     RetailerLoyaltyTier newTier, String message, String errorCode) {
        this.success = success;
        this.retailerId = retailerId;
        this.newPointsBalance = newPointsBalance;
        this.newTier = newTier;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ManageLoyaltyPointsResult success(Long retailerId, Integer newPointsBalance, RetailerLoyaltyTier newTier, String message) {
        return new ManageLoyaltyPointsResult(true, retailerId, newPointsBalance, newTier, message, null);
    }

    public static ManageLoyaltyPointsResult failure(String message, String errorCode) {
        return new ManageLoyaltyPointsResult(false, null, null, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public Integer getNewPointsBalance() {
        return newPointsBalance;
    }

    public RetailerLoyaltyTier getNewTier() {
        return newTier;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
