package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;

/**
 * Result object returned after chat session creation attempt.
 * Returns the full ChatSession object on success for immediate use.
 */
public class CreateChatSessionResult {

    private final boolean success;
    private final ChatSession session;
    private final String message;
    private final String errorCode;

    private CreateChatSessionResult(boolean success, ChatSession session, String message, String errorCode) {
        this.success = success;
        this.session = session;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CreateChatSessionResult success(ChatSession session) {
        return new CreateChatSessionResult(true, session, "Chat session created successfully", null);
    }

    public static CreateChatSessionResult failure(String message, String errorCode) {
        return new CreateChatSessionResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public ChatSession getSession() {
        return session;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
