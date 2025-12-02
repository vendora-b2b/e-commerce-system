package com.example.ecommerce.marketplace.web.model.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP request DTO for placing a new order.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotBlank(message = "Please provide an order number")
    private String orderNumber;

    @NotNull(message = "You must be logged in as a retailer to place an order")
    private Long retailerId;

    @NotNull(message = "Please select a supplier for this order")
    private Long supplierId;

    @NotEmpty(message = "Please add at least one item to your order")
    @Valid
    private List<OrderItemRequest> orderItems;

    @NotBlank(message = "Please enter a shipping address")
    private String shippingAddress;

    @NotNull(message = "Please specify the order date")
    private LocalDateTime orderDate;

    /**
     * Inner class representing an order item in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Please select a product variant for this order item")
        private Long variantId;

        @NotNull(message = "Please specify the quantity for this item")
        private Integer quantity;

        @NotNull(message = "Please specify the price for this item")
        private Double price;

        @NotBlank(message = "Product name is missing for this order item")
        private String productName;
    }
}
