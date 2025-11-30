package com.example.ecommerce.marketplace.application.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;

import java.util.Map;

/**
 * Command object for tracking a user interaction.
 */
public class TrackUserInteractionCommand {

    private final Long userId;
    private final Long productId;
    private final Long variantId;
    private final InteractionType interactionType;
    private final String sessionId;
    private final Map<String, String> metadata;

    private TrackUserInteractionCommand(Builder builder) {
        this.userId = builder.userId;
        this.productId = builder.productId;
        this.variantId = builder.variantId;
        this.interactionType = builder.interactionType;
        this.sessionId = builder.sessionId;
        this.metadata = builder.metadata;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Factory method for tracking a product view.
     */
    public static TrackUserInteractionCommand view(Long userId, Long productId) {
        return builder()
            .userId(userId)
            .productId(productId)
            .interactionType(InteractionType.VIEW)
            .build();
    }

    /**
     * Factory method for tracking a product click.
     */
    public static TrackUserInteractionCommand click(Long userId, Long productId) {
        return builder()
            .userId(userId)
            .productId(productId)
            .interactionType(InteractionType.CLICK)
            .build();
    }

    /**
     * Factory method for tracking an add-to-cart action.
     */
    public static TrackUserInteractionCommand addToCart(Long userId, Long productId, Long variantId) {
        return builder()
            .userId(userId)
            .productId(productId)
            .variantId(variantId)
            .interactionType(InteractionType.ADD_TO_CART)
            .build();
    }

    /**
     * Factory method for tracking a purchase.
     */
    public static TrackUserInteractionCommand purchase(Long userId, Long productId, Long variantId) {
        return builder()
            .userId(userId)
            .productId(productId)
            .variantId(variantId)
            .interactionType(InteractionType.PURCHASE)
            .build();
    }

    public static class Builder {
        private Long userId;
        private Long productId;
        private Long variantId;
        private InteractionType interactionType;
        private String sessionId;
        private Map<String, String> metadata;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder variantId(Long variantId) {
            this.variantId = variantId;
            return this;
        }

        public Builder interactionType(InteractionType interactionType) {
            this.interactionType = interactionType;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public TrackUserInteractionCommand build() {
            return new TrackUserInteractionCommand(this);
        }
    }
}
