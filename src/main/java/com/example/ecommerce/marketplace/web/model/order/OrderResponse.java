package com.example.ecommerce.marketplace.web.model.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP response DTO for order information.
 * Represents an order entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long retailerId;
    private Long supplierId;
    private List<OrderItemResponse> orderItems;
    private Double totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    /**
     * Creates an OrderResponse from a domain Order entity.
     */
    public static OrderResponse fromDomain(Order order) {
        List<OrderItemResponse> itemResponses = null;
        if (order.getOrderItems() != null) {
            itemResponses = order.getOrderItems().stream()
                .map(OrderItemResponse::fromDomain)
                .collect(Collectors.toList());
        }

        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getRetailerId(),
            order.getSupplierId(),
            itemResponses,
            order.getTotalAmount(),
            order.getStatus(),
            order.getShippingAddress(),
            order.getOrderDate(),
            order.getDeliveryDate()
        );
    }

    /**
     * Inner class representing an order item in the response.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {

        private Long id;
        private Long productId;
        private Integer quantity;
        private Double price;
        private String productName;

        /**
         * Creates an OrderItemResponse from a domain OrderItem entity.
         */
        public static OrderItemResponse fromDomain(OrderItem item) {
            return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice(),
                item.getProductName()
            );
        }
    }
}
