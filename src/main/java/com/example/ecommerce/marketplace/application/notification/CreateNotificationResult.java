package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import lombok.Getter;

/**
 * Result object for notification creation.
 */
@Getter
public class CreateNotificationResult {
    private final boolean success;
    private final Notification notification;
    private final String message;
    private final String errorCode;

    private CreateNotificationResult(boolean success, Notification notification, String message, String errorCode) {
        this.success = success;
        this.notification = notification;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CreateNotificationResult success(Notification notification) {
        return new CreateNotificationResult(true, notification, "Notification created successfully", null);
    }

    public static CreateNotificationResult failure(String message, String errorCode) {
        return new CreateNotificationResult(false, null, message, errorCode);
    }
}

