package com.example.ecommerce.marketplace.domain.chat;

/**
 * Enumeration of possible chat message roles.
 * Represents who sent the message in a chat conversation.
 */
public enum ChatRole {
    /**
     * Message sent by the user (retailer or supplier).
     */
    USER,

    /**
     * Message sent by the AI assistant.
     */
    ASSISTANT,

    /**
     * System message (e.g., session info, errors).
     */
    SYSTEM
}
