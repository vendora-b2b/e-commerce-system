package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.OrderStatus;

/**
 * Command object for updating order status.
 */
public class UpdateOrderStatusCommand {

    private final Long orderId;
    private final OrderStatus newStatus;

    public UpdateOrderStatusCommand(Long orderId, OrderStatus newStatus) {
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }
}
