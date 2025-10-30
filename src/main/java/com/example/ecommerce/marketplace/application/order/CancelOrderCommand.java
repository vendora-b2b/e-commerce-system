package com.example.ecommerce.marketplace.application.order;

/**
 * Command object for canceling an order.
 */
public class CancelOrderCommand {

    private final Long orderId;

    public CancelOrderCommand(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
