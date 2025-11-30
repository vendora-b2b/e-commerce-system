package com.example.ecommerce.marketplace.infrastructure.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for ChatMessageEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionId(Long sessionId);

    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<ChatMessageEntity> findBySessionIdAndRole(Long sessionId, ChatRole role);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC LIMIT :limit")
    List<ChatMessageEntity> findRecentBySessionId(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    List<ChatMessageEntity> findBySessionIdAndCreatedAtAfter(Long sessionId, LocalDateTime since);

    long countBySessionId(Long sessionId);

    @Modifying
    @Query("DELETE FROM ChatMessageEntity m WHERE m.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
