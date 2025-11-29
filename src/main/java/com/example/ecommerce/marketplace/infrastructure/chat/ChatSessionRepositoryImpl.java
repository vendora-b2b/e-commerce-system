package com.example.ecommerce.marketplace.infrastructure.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ChatSessionRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final JpaChatSessionRepository jpaRepository;

    @Override
    public ChatSession save(ChatSession session) {
        ChatSessionEntity entity = ChatSessionEntity.fromDomain(session);
        ChatSessionEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ChatSessionEntity::toDomain);
    }

    @Override
    public Optional<ChatSession> findBySessionToken(String sessionToken) {
        return jpaRepository.findBySessionToken(sessionToken)
            .map(ChatSessionEntity::toDomain);
    }

    @Override
    public List<ChatSession> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(ChatSessionEntity::toDomainWithoutMessages)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findByUserIdAndActiveTrue(Long userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId).stream()
            .map(ChatSessionEntity::toDomainWithoutMessages)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findByUserIdOrderByLastMessageAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByLastMessageAtDesc(userId).stream()
            .map(ChatSessionEntity::toDomainWithoutMessages)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findByUpdatedAtAfter(LocalDateTime since) {
        return jpaRepository.findByUpdatedAtAfter(since).stream()
            .map(ChatSessionEntity::toDomainWithoutMessages)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySessionToken(String sessionToken) {
        return jpaRepository.existsBySessionToken(sessionToken);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public List<ChatSession> findAll() {
        return jpaRepository.findAll().stream()
            .map(ChatSessionEntity::toDomainWithoutMessages)
            .collect(Collectors.toList());
    }
}
