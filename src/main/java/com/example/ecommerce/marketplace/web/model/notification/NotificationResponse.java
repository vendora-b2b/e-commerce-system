package com.example.ecommerce.marketplace.web.model.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a single notification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private Boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /**
     * Creates a response from a domain notification.
     */
    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.getRead(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}

