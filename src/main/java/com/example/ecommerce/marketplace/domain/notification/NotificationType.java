package com.example.ecommerce.marketplace.domain.notification;

/**
 * Enumeration of notification types.
 * Represents different events that can trigger notifications between retailers and suppliers.
 */
public enum NotificationType {
    /**
     * Notification when a new order is placed by a retailer.
     */
    ORDER_PLACED,

    /**
     * Notification when an order is confirmed by the supplier.
     */
    ORDER_CONFIRMED,

    /**
     * Notification when an order has been shipped.
     */
    ORDER_SHIPPED,

    /**
     * Notification when an order has been delivered.
     */
    ORDER_DELIVERED,

    /**
     * Notification when an order is cancelled.
     */
    ORDER_CANCELLED,

    /**
     * Notification when a quotation request is created.
     */
    QUOTATION_REQUESTED,

    /**
     * Notification when a supplier responds to a quotation.
     */
    QUOTATION_RESPONDED,

    /**
     * Notification when a quotation is accepted by the retailer.
     */
    QUOTATION_ACCEPTED,

    /**
     * Notification when a quotation is rejected.
     */
    QUOTATION_REJECTED,

    /**
     * Notification when a quotation is finalized.
     */
    QUOTATION_FINALIZED,

    /**
     * General system notification.
     */
    SYSTEM
}
