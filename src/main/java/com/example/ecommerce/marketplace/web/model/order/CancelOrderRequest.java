package com.example.ecommerce.marketplace.web.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for order cancellation.
 * Currently minimal, but can be extended with cancellation reason, notes, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    // Optional fields for future extensibility
    private String cancellationReason;
    private String notes;
}
