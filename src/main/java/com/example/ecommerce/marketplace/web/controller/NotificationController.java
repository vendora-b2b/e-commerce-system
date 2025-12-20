package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.notification.*;
import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;
import com.example.ecommerce.marketplace.web.model.notification.MarkAsReadRequest;
import com.example.ecommerce.marketplace.web.model.notification.NotificationListResponse;
import com.example.ecommerce.marketplace.web.model.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Notification operations.
 * Handles HTTP requests for user notifications.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification API")
public class NotificationController {

    private final GetUserNotificationsUseCase getUserNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final NotificationRepository notificationRepository;

    /**
     * Get all notifications for a user.
     * GET /api/v1/notifications?userId={userId}
     */
    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves all notifications for a user")
    public ResponseEntity<?> getNotifications(
        @RequestParam Long userId,
        @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        GetUserNotificationsCommand command = new GetUserNotificationsCommand(
            userId, unreadOnly, page, size
        );

        GetUserNotificationsResult result = getUserNotificationsUseCase.execute(command);

        if (!result.isSuccess()) {
            HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
            ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }

        List<NotificationResponse> notificationResponses = result.getNotifications().stream()
            .map(NotificationResponse::fromDomain)
            .collect(Collectors.toList());

        NotificationListResponse response = NotificationListResponse.of(
            notificationResponses, result.getTotalCount(), result.getUnreadCount()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single notification by ID.
     * GET /api/v1/notifications/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves a single notification")
    public ResponseEntity<?> getNotification(@PathVariable Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);

        if (notification.isPresent()) {
            NotificationResponse response = NotificationResponse.fromDomain(notification.get());
            return ResponseEntity.ok(response);
        }

        ErrorResponse errorResponse = ErrorResponse.of("NOTIFICATION_NOT_FOUND", 
            "Notification not found with ID: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Get unread notification count for a user.
     * GET /api/v1/notifications/unread-count?userId={userId}
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Gets the count of unread notifications for a user")
    public ResponseEntity<?> getUnreadCount(@RequestParam Long userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
    }

    /**
     * Mark a notification as read.
     * PATCH /api/v1/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read")
    public ResponseEntity<?> markAsRead(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        MarkNotificationAsReadCommand command = MarkNotificationAsReadCommand.single(id, userId);
        MarkNotificationAsReadResult result = markNotificationAsReadUseCase.execute(command);

        if (!result.isSuccess()) {
            HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
            ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }

        return ResponseEntity.ok(java.util.Map.of("message", result.getMessage()));
    }

    /**
     * Mark all notifications as read for a user.
     * PATCH /api/v1/notifications/read-all?userId={userId}
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Marks all notifications as read for a user")
    public ResponseEntity<?> markAllAsRead(@RequestParam Long userId) {
        MarkNotificationAsReadCommand command = MarkNotificationAsReadCommand.all(userId);
        MarkNotificationAsReadResult result = markNotificationAsReadUseCase.execute(command);

        if (!result.isSuccess()) {
            HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
            ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }

        return ResponseEntity.ok(java.util.Map.of("message", result.getMessage()));
    }

    /**
     * Delete a notification.
     * DELETE /api/v1/notifications/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Deletes a notification by ID")
    public ResponseEntity<?> deleteNotification(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (notificationOpt.isEmpty()) {
            ErrorResponse errorResponse = ErrorResponse.of("NOTIFICATION_NOT_FOUND",
                "Notification not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Notification notification = notificationOpt.get();

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            ErrorResponse errorResponse = ErrorResponse.of("UNAUTHORIZED",
                "Notification does not belong to user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

