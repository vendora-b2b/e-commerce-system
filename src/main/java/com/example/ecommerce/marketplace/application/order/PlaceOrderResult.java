package com.example.ecommerce.marketplace.application.order;

/**
 * Result object returned after order placement attempt.
 */
public class PlaceOrderResult {

    private final boolean success;
    private final Long orderId;
    private final String message;
    private final String errorCode;

    private PlaceOrderResult(boolean success, Long orderId, String message, String errorCode) {
        this.success = success;
        this.orderId = orderId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static PlaceOrderResult success(Long orderId) {
        return new PlaceOrderResult(true, orderId, "Order placed successfully", null);
    }

    public static PlaceOrderResult failure(String message, String errorCode) {
        return new PlaceOrderResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
