package com.example.ecommerce.marketplace.infrastructure.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatMessageRepository;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ChatMessageRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final JpaChatMessageRepository jpaRepository;
    private final JpaChatSessionRepository jpaSessionRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageEntity entity = ChatMessageEntity.fromDomain(message);
        
        // Set the session relationship
        if (message.getSessionId() != null) {
            jpaSessionRepository.findById(message.getSessionId())
                .ifPresent(entity::setSession);
        }
        
        ChatMessageEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<ChatMessage> saveAll(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatMessageEntity> entities = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageEntity entity = ChatMessageEntity.fromDomain(message);
            if (message.getSessionId() != null) {
                jpaSessionRepository.findById(message.getSessionId())
                    .ifPresent(entity::setSession);
            }
            entities.add(entity);
        }

        return jpaRepository.saveAll(entities).stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ChatMessageEntity::toDomain);
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId).stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId) {
        return jpaRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findBySessionIdAndRole(Long sessionId, ChatRole role) {
        return jpaRepository.findBySessionIdAndRole(sessionId, role).stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findRecentBySessionId(Long sessionId, int limit) {
        List<ChatMessageEntity> recentMessages = jpaRepository.findRecentBySessionId(sessionId, limit);
        // Reverse to get chronological order (oldest first)
        List<ChatMessage> result = recentMessages.stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<ChatMessage> findBySessionIdAndCreatedAtAfter(Long sessionId, LocalDateTime since) {
        return jpaRepository.findBySessionIdAndCreatedAtAfter(sessionId, since).stream()
            .map(ChatMessageEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countBySessionId(Long sessionId) {
        return jpaRepository.countBySessionId(sessionId);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
