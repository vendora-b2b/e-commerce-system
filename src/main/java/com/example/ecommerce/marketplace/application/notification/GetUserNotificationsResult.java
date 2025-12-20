package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import lombok.Getter;

import java.util.List;

/**
 * Result object for getting user notifications.
 */
@Getter
public class GetUserNotificationsResult {
    private final boolean success;
    private final List<Notification> notifications;
    private final long totalCount;
    private final long unreadCount;
    private final String message;
    private final String errorCode;

    private GetUserNotificationsResult(boolean success, List<Notification> notifications,
                                       long totalCount, long unreadCount, String message, String errorCode) {
        this.success = success;
        this.notifications = notifications;
        this.totalCount = totalCount;
        this.unreadCount = unreadCount;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetUserNotificationsResult success(List<Notification> notifications,
                                                     long totalCount, long unreadCount) {
        return new GetUserNotificationsResult(true, notifications, totalCount, unreadCount, null, null);
    }

    public static GetUserNotificationsResult failure(String message, String errorCode) {
        return new GetUserNotificationsResult(false, null, 0, 0, message, errorCode);
    }
}

