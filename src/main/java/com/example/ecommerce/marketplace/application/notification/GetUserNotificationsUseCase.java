package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for retrieving user notifications.
 */
@Service
@RequiredArgsConstructor
public class GetUserNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public GetUserNotificationsResult execute(GetUserNotificationsCommand command) {
        // Validate command
        if (command.getUserId() == null) {
            return GetUserNotificationsResult.failure("User ID is required", "INVALID_USER_ID");
        }

        List<Notification> notifications;

        // Get notifications based on filters
        if (Boolean.TRUE.equals(command.getUnreadOnly())) {
            notifications = notificationRepository.findByUserIdAndReadFalse(command.getUserId());
        } else if (command.getPage() != null && command.getSize() != null) {
            notifications = notificationRepository.findByUserIdPaginated(
                command.getUserId(), command.getPage(), command.getSize());
        } else {
            notifications = notificationRepository.findByUserId(command.getUserId());
        }

        // Get counts
        long totalCount = notificationRepository.countByUserId(command.getUserId());
        long unreadCount = notificationRepository.countUnreadByUserId(command.getUserId());

        return GetUserNotificationsResult.success(notifications, totalCount, unreadCount);
    }
}

