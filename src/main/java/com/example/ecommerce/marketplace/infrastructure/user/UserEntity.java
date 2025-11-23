package com.example.ecommerce.marketplace.infrastructure.user;

import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for User.
 * This is the persistence model, separate from the domain model.
 * Handles mapping between database tables and domain objects.
 */
@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_entity", columnList = "entity_id, role"),
        @Index(name = "idx_user_enabled", columnList = "enabled"),
        @Index(name = "idx_user_locked", columnList = "account_locked")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_entity_role", columnNames = {"entity_id", "role"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null) {
            enabled = true;
        }
        if (accountLocked == null) {
            accountLocked = false;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Converts JPA entity to domain model.
     */
    public User toDomain() {
        return new User(
            this.id,
            this.username,
            this.passwordHash,
            this.role,
            this.entityId,
            this.enabled,
            this.accountLocked,
            this.failedLoginAttempts,
            this.lastLoginAt,
            this.createdAt,
            this.updatedAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static UserEntity fromDomain(User user) {
        return new UserEntity(
            user.getId(),
            user.getUsername(),
            user.getPasswordHash(),
            user.getRole(),
            user.getEntityId(),
            user.getEnabled() != null ? user.getEnabled() : true,
            user.getAccountLocked() != null ? user.getAccountLocked() : false,
            user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0,
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
