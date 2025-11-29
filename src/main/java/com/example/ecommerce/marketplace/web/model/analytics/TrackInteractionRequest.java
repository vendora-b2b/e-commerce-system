package com.example.ecommerce.marketplace.web.model.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * HTTP request DTO for tracking user interactions.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackInteractionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    /**
     * Optional variant ID for variant-specific interactions.
     */
    private Long variantId;

    @NotNull(message = "Interaction type is required")
    private InteractionType interactionType;

    /**
     * Optional session ID for tracking user sessions.
     */
    private String sessionId;

    /**
     * Optional metadata for additional interaction context.
     * Can include things like search query, referrer, etc.
     */
    private Map<String, String> metadata;
}
