package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of updating a chat session title.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateChatSessionTitleResult {
    private final boolean success;
    private final ChatSession session;
    private final String errorCode;
    private final String message;

    /**
     * Creates a success result.
     */
    public static UpdateChatSessionTitleResult success(ChatSession session) {
        return new UpdateChatSessionTitleResult(true, session, null, "Session title updated successfully");
    }

    /**
     * Creates a failure result.
     */
    public static UpdateChatSessionTitleResult failure(String errorCode, String message) {
        return new UpdateChatSessionTitleResult(false, null, errorCode, message);
    }
}
