package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating notifications.
 */
@Service
@RequiredArgsConstructor
public class CreateNotificationUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional
    public CreateNotificationResult execute(CreateNotificationCommand command) {
        // Validate command
        if (command.getUserId() == null) {
            return CreateNotificationResult.failure("User ID is required", "INVALID_USER_ID");
        }
        if (command.getType() == null) {
            return CreateNotificationResult.failure("Notification type is required", "INVALID_TYPE");
        }
        if (command.getTitle() == null || command.getTitle().trim().isEmpty()) {
            return CreateNotificationResult.failure("Title is required", "INVALID_TITLE");
        }

        // Create notification
        Notification notification = new Notification(
            null,
            command.getUserId(),
            command.getType(),
            command.getTitle(),
            command.getMessage(),
            command.getReferenceId(),
            command.getReferenceType(),
            false,
            null,
            null
        );

        // Save notification
        Notification savedNotification = notificationRepository.save(notification);

        return CreateNotificationResult.success(savedNotification);
    }
}

