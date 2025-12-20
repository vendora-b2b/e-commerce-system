package com.example.ecommerce.marketplace.domain.notification;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification aggregate.
 * Defines persistence operations for notifications following the repository pattern.
 */
public interface NotificationRepository {

    /**
     * Saves a new notification or updates an existing one.
     * @param notification the notification to save
     * @return the saved notification with generated ID if new
     */
    Notification save(Notification notification);

    /**
     * Finds a notification by its unique identifier.
     * @param id the notification ID
     * @return an Optional containing the notification if found, empty otherwise
     */
    Optional<Notification> findById(Long id);

    /**
     * Finds all notifications for a specific user.
     * @param userId the user ID
     * @return list of notifications for the user, ordered by createdAt descending
     */
    List<Notification> findByUserId(Long userId);

    /**
     * Finds all unread notifications for a specific user.
     * @param userId the user ID
     * @return list of unread notifications for the user
     */
    List<Notification> findByUserIdAndReadFalse(Long userId);

    /**
     * Finds notifications by user ID with pagination.
     * @param userId the user ID
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return list of notifications for the page
     */
    List<Notification> findByUserIdPaginated(Long userId, int page, int size);

    /**
     * Counts unread notifications for a user.
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countUnreadByUserId(Long userId);

    /**
     * Counts all notifications for a user.
     * @param userId the user ID
     * @return total count of notifications
     */
    long countByUserId(Long userId);

    /**
     * Marks all notifications as read for a user.
     * @param userId the user ID
     */
    void markAllAsReadByUserId(Long userId);

    /**
     * Deletes a notification by its ID.
     * @param id the notification ID
     */
    void deleteById(Long id);

    /**
     * Deletes all notifications for a user.
     * @param userId the user ID
     */
    void deleteAllByUserId(Long userId);

    /**
     * Finds notifications by reference.
     * @param referenceId the reference entity ID
     * @param referenceType the reference entity type
     * @return list of matching notifications
     */
    List<Notification> findByReference(Long referenceId, String referenceType);
}

