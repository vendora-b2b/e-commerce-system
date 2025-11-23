package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command object for updating an order.
 * Supports updating status, deliveryDate, and item prices.
 */
@Getter
public class UpdateOrderCommand {

    private final Long orderId;
    private final OrderStatus newStatus;
    private final LocalDateTime deliveryDate;
    private final List<OrderItemPriceUpdate> itemPriceUpdates;

    public UpdateOrderCommand(Long orderId, OrderStatus newStatus, LocalDateTime deliveryDate, 
                            List<OrderItemPriceUpdate> itemPriceUpdates) {
        this.orderId = orderId;
        this.newStatus = newStatus;
        this.deliveryDate = deliveryDate;
        this.itemPriceUpdates = itemPriceUpdates;
    }

    /**
     * Represents a price update for an order item.
     */
    @Getter
    public static class OrderItemPriceUpdate {
        private final Long orderItemId;
        private final Double finalTotalPrice;

        public OrderItemPriceUpdate(Long orderItemId, Double finalTotalPrice) {
            this.orderItemId = orderItemId;
            this.finalTotalPrice = finalTotalPrice;
        }
    }
}
