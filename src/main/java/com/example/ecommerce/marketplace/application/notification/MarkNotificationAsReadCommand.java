package com.example.ecommerce.marketplace.application.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Command object for marking notifications as read.
 */
@Getter
@RequiredArgsConstructor
public class MarkNotificationAsReadCommand {
    private final Long notificationId;
    private final Long userId;
    private final Boolean markAll;

    /**
     * Command to mark a single notification as read.
     */
    public static MarkNotificationAsReadCommand single(Long notificationId, Long userId) {
        return new MarkNotificationAsReadCommand(notificationId, userId, false);
    }

    /**
     * Command to mark all notifications as read for a user.
     */
    public static MarkNotificationAsReadCommand all(Long userId) {
        return new MarkNotificationAsReadCommand(null, userId, true);
    }
}

