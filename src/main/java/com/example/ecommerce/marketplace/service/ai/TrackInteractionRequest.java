package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for tracking user interactions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackInteractionRequest {
    
    private Long userId;
    private Long productId;
    private Long variantId;
    private String sku;
    private String action;  // "VIEW", "ADD_TO_CART", "ORDER"
}
