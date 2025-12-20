package com.example.ecommerce.marketplace.infrastructure.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for NotificationEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * Find all notifications for a user, ordered by creation date descending.
     */
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all unread notifications for a user.
     */
    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Find notifications for a user with pagination.
     */
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Count all notifications for a user.
     */
    long countByUserId(Long userId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.read = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * Delete all notifications for a user.
     */
    void deleteByUserId(Long userId);

    /**
     * Find notifications by reference.
     */
    List<NotificationEntity> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}

