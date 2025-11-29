package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result object returned after asking a question.
 * Contains the AI-generated response and source references.
 */
public class AskQuestionResult {

    private final boolean success;
    private final ChatMessage userMessage;
    private final ChatMessage assistantMessage;
    private final List<Map<String, Object>> sources;
    private final String queryType;
    private final String message;
    private final String errorCode;

    private AskQuestionResult(boolean success, ChatMessage userMessage, ChatMessage assistantMessage,
                              List<Map<String, Object>> sources, String queryType, 
                              String message, String errorCode) {
        this.success = success;
        this.userMessage = userMessage;
        this.assistantMessage = assistantMessage;
        this.sources = sources;
        this.queryType = queryType;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static AskQuestionResult success(ChatMessage userMessage, ChatMessage assistantMessage,
                                            List<Map<String, Object>> sources, String queryType) {
        return new AskQuestionResult(true, userMessage, assistantMessage, sources, queryType,
                "Response generated successfully", null);
    }

    public static AskQuestionResult failure(String message, String errorCode) {
        return new AskQuestionResult(false, null, null, Collections.emptyList(), null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public ChatMessage getUserMessage() {
        return userMessage;
    }

    public ChatMessage getAssistantMessage() {
        return assistantMessage;
    }

    public String getResponse() {
        return assistantMessage != null ? assistantMessage.getContent() : null;
    }

    public List<Map<String, Object>> getSources() {
        return sources;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
