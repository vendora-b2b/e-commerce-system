package com.example.ecommerce.marketplace.application.notification;

import lombok.Getter;

/**
 * Result object for marking notifications as read.
 */
@Getter
public class MarkNotificationAsReadResult {
    private final boolean success;
    private final String message;
    private final String errorCode;

    private MarkNotificationAsReadResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static MarkNotificationAsReadResult success(String message) {
        return new MarkNotificationAsReadResult(true, message, null);
    }

    public static MarkNotificationAsReadResult failure(String message, String errorCode) {
        return new MarkNotificationAsReadResult(false, message, errorCode);
    }
}

