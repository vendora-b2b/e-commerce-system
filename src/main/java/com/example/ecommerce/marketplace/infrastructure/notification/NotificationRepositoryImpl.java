package com.example.ecommerce.marketplace.infrastructure.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationEntity.fromDomain(notification);
        NotificationEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id)
            .map(NotificationEntity::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(NotificationEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByUserIdAndReadFalse(Long userId) {
        return jpaRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
            .map(NotificationEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByUserIdPaginated(Long userId, int page, int size) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
            .getContent().stream()
            .map(NotificationEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public void markAllAsReadByUserId(Long userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public List<Notification> findByReference(Long referenceId, String referenceType) {
        return jpaRepository.findByReferenceIdAndReferenceType(referenceId, referenceType).stream()
            .map(NotificationEntity::toDomain)
            .collect(Collectors.toList());
    }
}

