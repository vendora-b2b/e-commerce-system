package com.example.ecommerce.marketplace.infrastructure.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import com.example.ecommerce.marketplace.domain.analytics.UserInteraction;
import com.example.ecommerce.marketplace.domain.analytics.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserInteractionRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class UserInteractionRepositoryImpl implements UserInteractionRepository {

    private final JpaUserInteractionRepository jpaRepository;

    @Override
    public UserInteraction save(UserInteraction interaction) {
        UserInteractionEntity entity = UserInteractionEntity.fromDomain(interaction);
        UserInteractionEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<UserInteraction> saveAll(List<UserInteraction> interactions) {
        List<UserInteractionEntity> entities = interactions.stream()
            .map(UserInteractionEntity::fromDomain)
            .collect(Collectors.toList());
        return jpaRepository.saveAll(entities).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<UserInteraction> findById(Long id) {
        return jpaRepository.findById(id)
            .map(UserInteractionEntity::toDomain);
    }

    @Override
    public List<UserInteraction> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findByUserIdAndProductId(Long userId, Long productId) {
        return jpaRepository.findByUserIdAndProductId(userId, productId).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findByUserIdAndInteractionType(Long userId, InteractionType interactionType) {
        return jpaRepository.findByUserIdAndInteractionType(userId, interactionType).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since) {
        return jpaRepository.findByUserIdAndCreatedAtAfter(userId, since).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findRecentByUserId(Long userId, int limit) {
        return jpaRepository.findRecentByUserId(userId, limit).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserInteraction> findByUserIdAndInteractionTypeIn(Long userId, List<InteractionType> types) {
        return jpaRepository.findByUserIdAndInteractionTypeIn(userId, types).stream()
            .map(UserInteractionEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public long countByProductId(Long productId) {
        return jpaRepository.countByProductId(productId);
    }

    @Override
    public long countByUserIdAndInteractionType(Long userId, InteractionType interactionType) {
        return jpaRepository.countByUserIdAndInteractionType(userId, interactionType);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return jpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean existsByUserIdAndProductIdAndInteractionType(Long userId, Long productId, InteractionType interactionType) {
        return jpaRepository.existsByUserIdAndProductIdAndInteractionType(userId, productId, interactionType);
    }

    @Override
    public long deleteByCreatedAtBefore(LocalDateTime before) {
        return jpaRepository.deleteByCreatedAtBefore(before);
    }

    @Override
    public List<Long> findDistinctProductIdsByUserId(Long userId) {
        return jpaRepository.findDistinctProductIdsByUserId(userId);
    }

    @Override
    public List<Long> findDistinctUserIdsByProductId(Long productId) {
        return jpaRepository.findDistinctUserIdsByProductId(productId);
    }
}
