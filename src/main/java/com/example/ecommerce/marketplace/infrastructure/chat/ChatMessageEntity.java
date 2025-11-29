package com.example.ecommerce.marketplace.infrastructure.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for ChatMessage.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_message_session", columnList = "session_id"),
        @Index(name = "idx_chat_message_role", columnList = "role"),
        @Index(name = "idx_chat_message_created", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSessionEntity session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Converts JPA entity to domain model.
     */
    public ChatMessage toDomain() {
        return new ChatMessage(
            this.id,
            this.session != null ? this.session.getId() : null,
            this.role,
            this.content,
            this.createdAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     * Note: The session must be set separately after creation.
     */
    public static ChatMessageEntity fromDomain(ChatMessage message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(message.getId());
        entity.setRole(message.getRole());
        entity.setContent(message.getContent());
        entity.setCreatedAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());
        return entity;
    }
}
