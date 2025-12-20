package com.example.ecommerce.marketplace.domain.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a notification entity in the e-commerce marketplace.
 * Notifications are sent to users (retailers or suppliers) when specific events occur.
 */
@Getter
@Setter
public class Notification {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId; // ID of the related entity (order, quotation, etc.)
    private String referenceType; // Type of related entity ("ORDER", "QUOTATION", etc.)
    private Boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Long id, Long userId, NotificationType type, String title,
                        String message, Long referenceId, String referenceType,
                        Boolean read, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.read = read != null ? read : false;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.readAt = readAt;
    }

    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the notification as unread.
     */
    public void markAsUnread() {
        this.read = false;
        this.readAt = null;
    }

    /**
     * Checks if the notification has been read.
     * @return true if read, false otherwise
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(this.read);
    }

    /**
     * Validates the notification has required fields.
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return userId != null && type != null && title != null && !title.trim().isEmpty();
    }

    /**
     * Creates a notification for an order event.
     */
    public static Notification forOrder(Long userId, NotificationType type, String title,
                                        String message, Long orderId) {
        return new Notification(null, userId, type, title, message, orderId, "ORDER",
                                false, LocalDateTime.now(), null);
    }

    /**
     * Creates a notification for a quotation event.
     */
    public static Notification forQuotation(Long userId, NotificationType type, String title,
                                            String message, Long quotationId) {
        return new Notification(null, userId, type, title, message, quotationId, "QUOTATION",
                                false, LocalDateTime.now(), null);
    }

    /**
     * Creates a system notification.
     */
    public static Notification system(Long userId, String title, String message) {
        return new Notification(null, userId, NotificationType.SYSTEM, title, message,
                                null, null, false, LocalDateTime.now(), null);
    }
}

