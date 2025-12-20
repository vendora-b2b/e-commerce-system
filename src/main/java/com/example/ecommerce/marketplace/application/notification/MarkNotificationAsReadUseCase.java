package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for marking notifications as read.
 */
@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional
    public MarkNotificationAsReadResult execute(MarkNotificationAsReadCommand command) {
        // Validate command
        if (command.getUserId() == null) {
            return MarkNotificationAsReadResult.failure("User ID is required", "INVALID_USER_ID");
        }

        // Mark all notifications as read
        if (Boolean.TRUE.equals(command.getMarkAll())) {
            notificationRepository.markAllAsReadByUserId(command.getUserId());
            return MarkNotificationAsReadResult.success("All notifications marked as read");
        }

        // Mark single notification as read
        if (command.getNotificationId() == null) {
            return MarkNotificationAsReadResult.failure("Notification ID is required", "INVALID_NOTIFICATION_ID");
        }

        Optional<Notification> notificationOpt = notificationRepository.findById(command.getNotificationId());
        if (notificationOpt.isEmpty()) {
            return MarkNotificationAsReadResult.failure("Notification not found", "NOTIFICATION_NOT_FOUND");
        }

        Notification notification = notificationOpt.get();

        // Verify ownership
        if (!notification.getUserId().equals(command.getUserId())) {
            return MarkNotificationAsReadResult.failure("Notification does not belong to user", "UNAUTHORIZED");
        }

        // Mark as read
        notification.markAsRead();
        notificationRepository.save(notification);

        return MarkNotificationAsReadResult.success("Notification marked as read");
    }
}

