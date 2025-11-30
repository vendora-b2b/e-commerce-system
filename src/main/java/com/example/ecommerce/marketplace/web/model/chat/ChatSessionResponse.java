package com.example.ecommerce.marketplace.web.model.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * HTTP response DTO for chat session information.
 * Represents a chat session entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {

    private Long id;
    private String sessionToken;
    private Long userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    private Boolean active;
    private Integer messageCount;

    /**
     * Creates a ChatSessionResponse from a domain ChatSession entity.
     *
     * @param session the domain entity
     * @return the response DTO
     */
    public static ChatSessionResponse fromDomain(ChatSession session) {
        return new ChatSessionResponse(
            session.getId(),
            session.getSessionToken(),
            session.getUserId(),
            session.getTitle(),
            session.getCreatedAt(),
            session.getUpdatedAt(),
            session.getLastMessageAt(),
            session.getActive(),
            session.getMessages() != null ? session.getMessages().size() : 0
        );
    }
}
