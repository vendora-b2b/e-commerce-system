package com.example.ecommerce.marketplace.infrastructure.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA entity for ChatSession.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "chat_sessions",
    indexes = {
        @Index(name = "idx_chat_session_user", columnList = "user_id"),
        @Index(name = "idx_chat_session_token", columnList = "session_token"),
        @Index(name = "idx_chat_session_active", columnList = "active"),
        @Index(name = "idx_chat_session_last_message", columnList = "last_message_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_session_token", columnNames = "session_token")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200)
    private String title;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ChatMessageEntity> messages = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Converts JPA entity to domain model.
     */
    public ChatSession toDomain() {
        List<ChatMessage> domainMessages = null;
        if (this.messages != null) {
            domainMessages = this.messages.stream()
                .map(ChatMessageEntity::toDomain)
                .collect(Collectors.toList());
        }

        return new ChatSession(
            this.id,
            this.sessionToken,
            this.userId,
            this.title,
            domainMessages,
            this.createdAt,
            this.updatedAt,
            this.lastMessageAt,
            this.active
        );
    }

    /**
     * Converts JPA entity to domain model without loading messages (for list queries).
     */
    public ChatSession toDomainWithoutMessages() {
        return new ChatSession(
            this.id,
            this.sessionToken,
            this.userId,
            this.title,
            new ArrayList<>(),
            this.createdAt,
            this.updatedAt,
            this.lastMessageAt,
            this.active
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static ChatSessionEntity fromDomain(ChatSession session) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(session.getId());
        entity.setSessionToken(session.getSessionToken());
        entity.setUserId(session.getUserId());
        entity.setTitle(session.getTitle());
        entity.setCreatedAt(session.getCreatedAt() != null ? session.getCreatedAt() : LocalDateTime.now());
        entity.setUpdatedAt(session.getUpdatedAt() != null ? session.getUpdatedAt() : LocalDateTime.now());
        entity.setLastMessageAt(session.getLastMessageAt());
        entity.setActive(session.getActive() != null ? session.getActive() : true);

        // Convert messages
        if (session.getMessages() != null) {
            for (ChatMessage domainMessage : session.getMessages()) {
                ChatMessageEntity messageEntity = ChatMessageEntity.fromDomain(domainMessage);
                messageEntity.setSession(entity);
                entity.getMessages().add(messageEntity);
            }
        }

        return entity;
    }

    /**
     * Adds a message to this session.
     */
    public void addMessage(ChatMessageEntity message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        message.setSession(this);
        this.messages.add(message);
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
