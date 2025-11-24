package com.example.ecommerce.marketplace.domain.order;

/**
 * Enumeration of possible order statuses.
 * Represents the lifecycle of an order from creation to completion.
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet confirmed by supplier.
     */
    PENDING,

    /**
     * Order has been confirmed and is being prepared by the supplier.
     */
    CONFIRMED,

    /**
     * Order has been shipped to the retailer.
     */
    SHIPPED,

    /**
     * Order has been delivered to the retailer.
     */
    DELIVERED,

    /**
     * Order has been cancelled by retailer or supplier.
     */
    CANCELLED
}
