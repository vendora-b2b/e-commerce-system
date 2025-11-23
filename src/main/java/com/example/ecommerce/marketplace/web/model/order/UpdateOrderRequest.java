package com.example.ecommerce.marketplace.web.model.order;

import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP request DTO for updating an order via PATCH endpoint.
 * Supports updating status, deliveryDate, and item prices.
 * All fields are optional - only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

    private OrderStatus status;
    
    private LocalDateTime deliveryDate;
    
    @Valid
    private List<OrderItemUpdate> items;

    /**
     * Represents an update to an order item's final price.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemUpdate {
        
        @Positive(message = "Item ID must be positive")
        private Long id;
        
        @Positive(message = "Final total price must be positive")
        private Double finalTotalPrice;
    }
}
