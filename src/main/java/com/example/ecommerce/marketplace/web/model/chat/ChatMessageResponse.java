package com.example.ecommerce.marketplace.web.model.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * HTTP response DTO for chat message information.
 * Represents a chat message entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private Long sessionId;
    private ChatRole role;
    private String content;
    private LocalDateTime createdAt;

    /**
     * Creates a ChatMessageResponse from a domain ChatMessage entity.
     *
     * @param message the domain entity
     * @return the response DTO
     */
    public static ChatMessageResponse fromDomain(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getSessionId(),
            message.getRole(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
}
