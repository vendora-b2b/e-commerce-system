package com.example.ecommerce.marketplace.infrastructure.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ChatSessionEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

    Optional<ChatSessionEntity> findBySessionToken(String sessionToken);

    List<ChatSessionEntity> findByUserId(Long userId);

    List<ChatSessionEntity> findByUserIdAndActiveTrue(Long userId);

    List<ChatSessionEntity> findByUserIdOrderByLastMessageAtDesc(Long userId);

    List<ChatSessionEntity> findByUpdatedAtAfter(LocalDateTime since);

    boolean existsBySessionToken(String sessionToken);

    long countByUserId(Long userId);

    long countByUserIdAndActiveTrue(Long userId);
}
