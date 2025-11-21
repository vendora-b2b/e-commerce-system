package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;

/**
 * Result object returned after order update attempt.
 */
public class UpdateOrderStatusResult {

    private final boolean success;
    private final Order order;
    private final String message;
    private final String errorCode;

    private UpdateOrderStatusResult(boolean success, Order order, String message, String errorCode) {
        this.success = success;
        this.order = order;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateOrderStatusResult success(Order order) {
        return new UpdateOrderStatusResult(true, order, "Order updated successfully", null);
    }

    public static UpdateOrderStatusResult failure(String message, String errorCode) {
        return new UpdateOrderStatusResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Order getOrder() {
        return order;
    }

    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
