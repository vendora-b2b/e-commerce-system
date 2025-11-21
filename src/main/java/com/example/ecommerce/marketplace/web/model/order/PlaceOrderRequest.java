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

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Retailer ID is required")
    private Long retailerId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    /**
     * Inner class representing an order item in the request.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Price is required")
        private Double price;

        @NotBlank(message = "Product name is required")
        private String productName;
    }
}
