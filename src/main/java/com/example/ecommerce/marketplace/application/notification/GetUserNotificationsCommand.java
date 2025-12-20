package com.example.ecommerce.marketplace.application.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Command object for getting user notifications.
 */
@Getter
@RequiredArgsConstructor
public class GetUserNotificationsCommand {
    private final Long userId;
    private final Boolean unreadOnly;
    private final Integer page;
    private final Integer size;

    public GetUserNotificationsCommand(Long userId) {
        this(userId, false, 0, 20);
    }

    public GetUserNotificationsCommand(Long userId, Boolean unreadOnly) {
        this(userId, unreadOnly, 0, 20);
    }
}

