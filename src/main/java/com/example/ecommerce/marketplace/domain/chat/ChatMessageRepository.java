package com.example.ecommerce.marketplace.domain.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChatMessage entity.
 * Defines persistence operations for chat messages following the repository pattern.
 */
public interface ChatMessageRepository {

    /**
     * Saves a new chat message or updates an existing one.
     *
     * @param message the chat message to save
     * @return the saved message with generated ID if new
     */
    ChatMessage save(ChatMessage message);

    /**
     * Saves multiple chat messages.
     *
     * @param messages the list of messages to save
     * @return the saved messages
     */
    List<ChatMessage> saveAll(List<ChatMessage> messages);

    /**
     * Finds a chat message by its unique identifier.
     *
     * @param id the message ID
     * @return an Optional containing the message if found, empty otherwise
     */
    Optional<ChatMessage> findById(Long id);

    /**
     * Finds all messages for a chat session.
     *
     * @param sessionId the chat session ID
     * @return list of messages in the session
     */
    List<ChatMessage> findBySessionId(Long sessionId);

    /**
     * Finds all messages for a session, ordered by creation time ascending.
     *
     * @param sessionId the chat session ID
     * @return list of messages in chronological order
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    /**
     * Finds messages by session and role.
     *
     * @param sessionId the chat session ID
     * @param role      the chat role (USER, ASSISTANT, SYSTEM)
     * @return list of messages with the specified role
     */
    List<ChatMessage> findBySessionIdAndRole(Long sessionId, ChatRole role);

    /**
     * Finds the most recent messages in a session.
     *
     * @param sessionId the chat session ID
     * @param limit     maximum number of messages to return
     * @return list of recent messages
     */
    List<ChatMessage> findRecentBySessionId(Long sessionId, int limit);

    /**
     * Finds messages created after a specific time.
     *
     * @param sessionId the chat session ID
     * @param since     the timestamp to search from
     * @return list of messages created after the given time
     */
    List<ChatMessage> findBySessionIdAndCreatedAtAfter(Long sessionId, LocalDateTime since);

    /**
     * Counts messages in a session.
     *
     * @param sessionId the chat session ID
     * @return count of messages
     */
    long countBySessionId(Long sessionId);

    /**
     * Deletes all messages in a session.
     *
     * @param sessionId the chat session ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * Deletes a message by its ID.
     *
     * @param id the message ID
     */
    void deleteById(Long id);
}
