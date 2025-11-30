package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after retrieving chat messages.
 */
public class GetChatMessagesResult {

    private final boolean success;
    private final List<ChatMessage> messages;
    private final Long sessionId;
    private final String message;
    private final String errorCode;

    private GetChatMessagesResult(boolean success, List<ChatMessage> messages, Long sessionId, 
                                   String message, String errorCode) {
        this.success = success;
        this.messages = messages;
        this.sessionId = sessionId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetChatMessagesResult success(List<ChatMessage> messages, Long sessionId) {
        return new GetChatMessagesResult(true, messages, sessionId, "Messages retrieved successfully", null);
    }

    public static GetChatMessagesResult failure(String message, String errorCode) {
        return new GetChatMessagesResult(false, Collections.emptyList(), null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getCount() {
        return messages != null ? messages.size() : 0;
    }
}
