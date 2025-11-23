package com.example.ecommerce.marketplace.application.order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command object for placing a new order.
 * Contains all necessary data for order creation.
 */
public class PlaceOrderCommand {

    private final String orderNumber;
    private final Long retailerId;
    private final Long supplierId;
    private final List<OrderItemCommand> orderItems;
    private final String shippingAddress;
    private final LocalDateTime orderDate;

    public PlaceOrderCommand(String orderNumber, Long retailerId, Long supplierId,
                            List<OrderItemCommand> orderItems, String shippingAddress,
                            LocalDateTime orderDate) {
        this.orderNumber = orderNumber;
        this.retailerId = retailerId;
        this.supplierId = supplierId;
        this.orderItems = orderItems;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public List<OrderItemCommand> getOrderItems() {
        return orderItems;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /**
     * Inner class representing an order item in the command.
     */
    public static class OrderItemCommand {
        private final Long productId;
        private final Long variantId;
        private final Integer quantity;
        private final Double price;
        private final String productName;

        public OrderItemCommand(Long productId, Long variantId, Integer quantity, Double price, String productName) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.price = price;
            this.productName = productName;
        }

        public Long getProductId() {
            return productId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Double getPrice() {
            return price;
        }

        public String getProductName() {
            return productName;
        }
    }
}
