package com.example.ecommerce.marketplace.web.model.order;

import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for updating order status.
 * All fields are required for status updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
}
