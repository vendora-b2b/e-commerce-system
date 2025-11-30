package com.example.ecommerce.marketplace.domain.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a chat session between a user and the AI assistant.
 * Sessions group related messages and track conversation context.
 * This is an aggregate root that manages chat messages.
 */
@Getter
@Setter
public class ChatSession {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_MESSAGES_PER_SESSION = 100;

    private Long id;
    private String sessionToken;
    private Long userId;
    private String title;
    private List<ChatMessage> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    private Boolean active;

    public ChatSession() {
        this.messages = new ArrayList<>();
        this.sessionToken = generateSessionToken();
        this.active = true;
    }

    public ChatSession(Long id, String sessionToken, Long userId, String title,
                       List<ChatMessage> messages, LocalDateTime createdAt,
                       LocalDateTime updatedAt, LocalDateTime lastMessageAt, Boolean active) {
        this.id = id;
        this.sessionToken = sessionToken != null ? sessionToken : generateSessionToken();
        this.userId = userId;
        this.title = title;
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastMessageAt = lastMessageAt;
        this.active = active != null ? active : true;
    }

    /**
     * Creates a new chat session for a user.
     *
     * @param userId the user ID
     * @param title  optional title for the session
     * @return a new ChatSession
     */
    public static ChatSession createNew(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "New Chat");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
    }

    /**
     * Generates a unique session token.
     *
     * @return a UUID-based session token
     */
    private static String generateSessionToken() {
        return "chat_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Adds a user message to the session.
     *
     * @param content the message content
     * @return the created ChatMessage
     */
    public ChatMessage addUserMessage(String content) {
        ChatMessage message = ChatMessage.userMessage(this.id, content);
        addMessage(message);
        return message;
    }

    /**
     * Adds an assistant message to the session.
     *
     * @param content the message content
     * @return the created ChatMessage
     */
    public ChatMessage addAssistantMessage(String content) {
        ChatMessage message = ChatMessage.assistantMessage(this.id, content);
        addMessage(message);
        return message;
    }

    /**
     * Adds a message to the session.
     *
     * @param message the message to add
     */
    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        message.setSessionId(this.id);
        this.messages.add(message);
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Auto-generates a title from the first user message if title is default.
     */
    public void autoGenerateTitle() {
        if (!"New Chat".equals(this.title) || this.messages == null || this.messages.isEmpty()) {
            return;
        }

        // Find first user message
        for (ChatMessage message : this.messages) {
            if (message.isUserMessage() && message.getContent() != null) {
                String content = message.getContent().trim();
                // Take first sentence or first 50 chars
                int endIndex = Math.min(content.length(), 50);
                int sentenceEnd = content.indexOf('.');
                if (sentenceEnd > 0 && sentenceEnd < endIndex) {
                    endIndex = sentenceEnd;
                }
                this.title = content.substring(0, endIndex);
                if (content.length() > endIndex) {
                    this.title += "...";
                }
                break;
            }
        }
    }

    /**
     * Gets the message count in this session.
     *
     * @return number of messages
     */
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    /**
     * Checks if the session can accept more messages.
     *
     * @return true if under the message limit
     */
    public boolean canAddMoreMessages() {
        return getMessageCount() < MAX_MESSAGES_PER_SESSION;
    }

    /**
     * Gets the most recent messages for context.
     *
     * @param limit maximum number of messages to return
     * @return list of recent messages
     */
    public List<ChatMessage> getRecentMessages(int limit) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        int startIndex = Math.max(0, messages.size() - limit);
        return new ArrayList<>(messages.subList(startIndex, messages.size()));
    }

    /**
     * Deactivates the session (soft close).
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates the session title.
     *
     * @return true if title is valid
     */
    public boolean validateTitle() {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        return title.length() <= MAX_TITLE_LENGTH;
    }

    /**
     * Checks if the session belongs to a specific user.
     *
     * @param userId the user ID to check
     * @return true if this session belongs to the user
     */
    public boolean belongsToUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}
