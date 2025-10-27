package com.example.ecommerce.marketplace.application.order;

/**
 * Result object returned after order cancellation attempt.
 */
public class CancelOrderResult {

    private final boolean success;
    private final Long orderId;
    private final String message;
    private final String errorCode;

    private CancelOrderResult(boolean success, Long orderId, String message, String errorCode) {
        this.success = success;
        this.orderId = orderId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CancelOrderResult success(Long orderId) {
        return new CancelOrderResult(true, orderId, "Order cancelled successfully", null);
    }

    public static CancelOrderResult failure(String message, String errorCode) {
        return new CancelOrderResult(false, null, message, errorCode);
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
