package com.example.ecommerce.marketplace.domain.analytics;

/**
 * Enumeration of user interaction types.
 * Represents different ways users interact with products for analytics and recommendations.
 */
public enum InteractionType {
    /**
     * User viewed a product detail page.
     */
    VIEW(1.0),

    /**
     * User clicked on a product in search results or listings.
     */
    CLICK(1.5),

    /**
     * User added a product to their cart.
     */
    ADD_TO_CART(3.0),

    /**
     * User removed a product from their cart.
     */
    REMOVE_FROM_CART(-1.0),

    /**
     * User added a product to their wishlist/favorites.
     */
    WISHLIST(2.5),

    /**
     * User purchased a product.
     */
    PURCHASE(5.0),

    /**
     * User searched for products (query-level interaction).
     */
    SEARCH(0.5),

    /**
     * User rated or reviewed a product.
     */
    REVIEW(4.0);

    private final double weight;

    InteractionType(double weight) {
        this.weight = weight;
    }

    /**
     * Gets the weight of this interaction type for recommendation scoring.
     * Higher weights indicate stronger user interest.
     *
     * @return the interaction weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Checks if this is a positive interaction (indicates interest).
     *
     * @return true if the interaction is positive
     */
    public boolean isPositive() {
        return weight > 0;
    }

    /**
     * Checks if this is a high-value interaction (strong purchase intent).
     *
     * @return true if the interaction indicates high purchase intent
     */
    public boolean isHighValue() {
        return weight >= 3.0;
    }
}
