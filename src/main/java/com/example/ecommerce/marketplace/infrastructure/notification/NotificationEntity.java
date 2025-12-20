package com.example.ecommerce.marketplace.infrastructure.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for Notification.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notification_created", columnList = "created_at"),
        @Index(name = "idx_notification_reference", columnList = "reference_id, reference_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String message;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "is_read", nullable = false)
    private Boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
    }

    /**
     * Converts JPA entity to domain model.
     */
    public Notification toDomain() {
        return new Notification(
            this.id,
            this.userId,
            this.type,
            this.title,
            this.message,
            this.referenceId,
            this.referenceType,
            this.read,
            this.createdAt,
            this.readAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static NotificationEntity fromDomain(Notification notification) {
        return new NotificationEntity(
            notification.getId(),
            notification.getUserId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.getRead() != null ? notification.getRead() : false,
            notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now(),
            notification.getReadAt()
        );
    }
}

