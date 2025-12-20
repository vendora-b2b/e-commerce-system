package com.example.ecommerce.marketplace.web.model.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for a list of notifications with metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private long totalCount;
    private long unreadCount;

    public static NotificationListResponse of(List<NotificationResponse> notifications,
                                              long totalCount, long unreadCount) {
        return new NotificationListResponse(notifications, totalCount, unreadCount);
    }
}

