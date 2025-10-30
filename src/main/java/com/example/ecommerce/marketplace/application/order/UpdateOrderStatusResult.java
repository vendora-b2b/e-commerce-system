package com.example.ecommerce.marketplace.application.order;

/**
 * Result object returned after order status update attempt.
 */
public class UpdateOrderStatusResult {

    private final boolean success;
    private final Long orderId;
    private final String message;
    private final String errorCode;

    private UpdateOrderStatusResult(boolean success, Long orderId, String message, String errorCode) {
        this.success = success;
        this.orderId = orderId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateOrderStatusResult success(Long orderId) {
        return new UpdateOrderStatusResult(true, orderId, "Order status updated successfully", null);
    }

    public static UpdateOrderStatusResult failure(String message, String errorCode) {
        return new UpdateOrderStatusResult(false, null, message, errorCode);
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
