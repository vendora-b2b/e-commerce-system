package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;

/**
 * Result object returned after order placement attempt.
 * Returns the full Order object on success for immediate use.
 */
public class PlaceOrderResult {

    private final boolean success;
    private final Order order;
    private final String message;
    private final String errorCode;


    private PlaceOrderResult(boolean success, Order order, String message, String errorCode) {
        this.success = success;
        this.order = order;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static PlaceOrderResult success(Order order) {
        return new PlaceOrderResult(true, order, "Order placed successfully", null);
    }

    public static PlaceOrderResult failure(String message, String errorCode) {
        return new PlaceOrderResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Order getOrder() {
        return order;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
