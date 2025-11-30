package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after retrieving chat sessions.
 */
public class GetChatSessionsResult {

    private final boolean success;
    private final List<ChatSession> sessions;
    private final String message;
    private final String errorCode;

    private GetChatSessionsResult(boolean success, List<ChatSession> sessions, String message, String errorCode) {
        this.success = success;
        this.sessions = sessions;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetChatSessionsResult success(List<ChatSession> sessions) {
        return new GetChatSessionsResult(true, sessions, "Sessions retrieved successfully", null);
    }

    public static GetChatSessionsResult failure(String message, String errorCode) {
        return new GetChatSessionsResult(false, Collections.emptyList(), message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ChatSession> getSessions() {
        return sessions;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getCount() {
        return sessions != null ? sessions.size() : 0;
    }
}
