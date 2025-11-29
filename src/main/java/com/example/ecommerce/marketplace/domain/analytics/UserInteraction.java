package com.example.ecommerce.marketplace.domain.analytics;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a user interaction event for analytics and recommendation learning.
 * Tracks how users interact with products to build personalized recommendations.
 */
@Getter
@Setter
public class UserInteraction {

    private Long id;
    private Long userId;
    private Long productId;
    private Long variantId;  // Optional - for variant-specific tracking
    private InteractionType interactionType;
    private String sessionId;  // Browser/app session for grouping interactions
    private Map<String, String> metadata;  // Additional context (search query, referrer, etc.)
    private LocalDateTime createdAt;

    public UserInteraction() {
    }

    public UserInteraction(Long id, Long userId, Long productId, Long variantId,
                           InteractionType interactionType, String sessionId,
                           Map<String, String> metadata, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.variantId = variantId;
        this.interactionType = interactionType;
        this.sessionId = sessionId;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new view interaction.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @return a new UserInteraction with VIEW type
     */
    public static UserInteraction view(Long userId, Long productId) {
        return create(userId, productId, null, InteractionType.VIEW, null, null);
    }

    /**
     * Creates a new click interaction.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @return a new UserInteraction with CLICK type
     */
    public static UserInteraction click(Long userId, Long productId) {
        return create(userId, productId, null, InteractionType.CLICK, null, null);
    }

    /**
     * Creates a new add-to-cart interaction.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @param variantId the variant ID (optional)
     * @return a new UserInteraction with ADD_TO_CART type
     */
    public static UserInteraction addToCart(Long userId, Long productId, Long variantId) {
        return create(userId, productId, variantId, InteractionType.ADD_TO_CART, null, null);
    }

    /**
     * Creates a new purchase interaction.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @param variantId the variant ID (optional)
     * @return a new UserInteraction with PURCHASE type
     */
    public static UserInteraction purchase(Long userId, Long productId, Long variantId) {
        return create(userId, productId, variantId, InteractionType.PURCHASE, null, null);
    }

    /**
     * Creates a new wishlist interaction.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @return a new UserInteraction with WISHLIST type
     */
    public static UserInteraction wishlist(Long userId, Long productId) {
        return create(userId, productId, null, InteractionType.WISHLIST, null, null);
    }

    /**
     * Creates a new user interaction.
     *
     * @param userId          the user ID
     * @param productId       the product ID
     * @param variantId       the variant ID (optional)
     * @param interactionType the type of interaction
     * @param sessionId       the session ID (optional)
     * @param metadata        additional metadata (optional)
     * @return a new UserInteraction
     */
    public static UserInteraction create(Long userId, Long productId, Long variantId,
                                         InteractionType interactionType, String sessionId,
                                         Map<String, String> metadata) {
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setProductId(productId);
        interaction.setVariantId(variantId);
        interaction.setInteractionType(interactionType);
        interaction.setSessionId(sessionId);
        interaction.setMetadata(metadata);
        interaction.setCreatedAt(LocalDateTime.now());
        return interaction;
    }

    /**
     * Gets the weight of this interaction for recommendation scoring.
     *
     * @return the interaction weight
     */
    public double getWeight() {
        return interactionType != null ? interactionType.getWeight() : 0.0;
    }

    /**
     * Checks if this is a high-value interaction (strong purchase intent).
     *
     * @return true if the interaction indicates high purchase intent
     */
    public boolean isHighValue() {
        return interactionType != null && interactionType.isHighValue();
    }

    /**
     * Validates that required fields are present.
     *
     * @return true if the interaction is valid
     */
    public boolean isValid() {
        return userId != null && productId != null && interactionType != null;
    }

    /**
     * Checks if this interaction is recent (within the last N hours).
     *
     * @param hours the number of hours
     * @return true if created within the specified hours
     */
    public boolean isRecent(int hours) {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusHours(hours));
    }

    /**
     * Gets a metadata value by key.
     *
     * @param key the metadata key
     * @return the value, or null if not present
     */
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}
