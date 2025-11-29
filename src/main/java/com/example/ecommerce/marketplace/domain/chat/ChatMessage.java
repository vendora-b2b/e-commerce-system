package com.example.ecommerce.marketplace.domain.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a single message in a chat session.
 * Messages can be from the user, AI assistant, or system.
 */
@Getter
@Setter
public class ChatMessage {

    private static final int MAX_CONTENT_LENGTH = 10000;

    private Long id;
    private Long sessionId;
    private ChatRole role;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long id, Long sessionId, ChatRole role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new user message.
     *
     * @param sessionId the chat session ID
     * @param content   the message content
     * @return a new ChatMessage with USER role
     */
    public static ChatMessage userMessage(Long sessionId, String content) {
        return new ChatMessage(null, sessionId, ChatRole.USER, content, LocalDateTime.now());
    }

    /**
     * Creates a new assistant message.
     *
     * @param sessionId the chat session ID
     * @param content   the message content
     * @return a new ChatMessage with ASSISTANT role
     */
    public static ChatMessage assistantMessage(Long sessionId, String content) {
        return new ChatMessage(null, sessionId, ChatRole.ASSISTANT, content, LocalDateTime.now());
    }

    /**
     * Creates a new system message.
     *
     * @param sessionId the chat session ID
     * @param content   the message content
     * @return a new ChatMessage with SYSTEM role
     */
    public static ChatMessage systemMessage(Long sessionId, String content) {
        return new ChatMessage(null, sessionId, ChatRole.SYSTEM, content, LocalDateTime.now());
    }

    /**
     * Validates the message content.
     *
     * @return true if content is valid, false otherwise
     */
    public boolean validateContent() {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        return content.length() <= MAX_CONTENT_LENGTH;
    }

    /**
     * Checks if this message is from the user.
     *
     * @return true if role is USER
     */
    public boolean isUserMessage() {
        return ChatRole.USER.equals(role);
    }

    /**
     * Checks if this message is from the assistant.
     *
     * @return true if role is ASSISTANT
     */
    public boolean isAssistantMessage() {
        return ChatRole.ASSISTANT.equals(role);
    }

    /**
     * Returns a truncated preview of the content.
     *
     * @param maxLength maximum length of preview
     * @return truncated content with "..." if needed
     */
    public String getContentPreview(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength - 3) + "...";
    }
}
