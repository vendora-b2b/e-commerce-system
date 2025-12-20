package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Command object for creating a notification.
 */
@Getter
@RequiredArgsConstructor
public class CreateNotificationCommand {
    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long referenceId;
    private final String referenceType;

    /**
     * Creates a command for an order notification.
     */
    public static CreateNotificationCommand forOrder(Long userId, NotificationType type,
                                                     String title, String message, Long orderId) {
        return new CreateNotificationCommand(userId, type, title, message, orderId, "ORDER");
    }

    /**
     * Creates a command for a quotation notification.
     */
    public static CreateNotificationCommand forQuotation(Long userId, NotificationType type,
                                                         String title, String message, Long quotationId) {
        return new CreateNotificationCommand(userId, type, title, message, quotationId, "QUOTATION");
    }

    /**
     * Creates a command for a system notification.
     */
    public static CreateNotificationCommand system(Long userId, String title, String message) {
        return new CreateNotificationCommand(userId, NotificationType.SYSTEM, title, message, null, null);
    }
}

