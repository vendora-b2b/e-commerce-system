package com.example.ecommerce.marketplace.domain.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChatSession aggregate root.
 * Defines persistence operations for chat sessions following the repository pattern.
 */
public interface ChatSessionRepository {

    /**
     * Saves a new chat session or updates an existing one.
     *
     * @param session the chat session to save
     * @return the saved session with generated ID if new
     */
    ChatSession save(ChatSession session);

    /**
     * Finds a chat session by its unique identifier.
     *
     * @param id the session ID
     * @return an Optional containing the session if found, empty otherwise
     */
    Optional<ChatSession> findById(Long id);

    /**
     * Finds a chat session by its session token.
     *
     * @param sessionToken the unique session token
     * @return an Optional containing the session if found, empty otherwise
     */
    Optional<ChatSession> findBySessionToken(String sessionToken);

    /**
     * Finds all chat sessions for a user.
     *
     * @param userId the user ID
     * @return list of sessions for the user
     */
    List<ChatSession> findByUserId(Long userId);

    /**
     * Finds all active chat sessions for a user.
     *
     * @param userId the user ID
     * @return list of active sessions for the user
     */
    List<ChatSession> findByUserIdAndActiveTrue(Long userId);

    /**
     * Finds all chat sessions for a user, ordered by last message time descending.
     *
     * @param userId the user ID
     * @return list of sessions ordered by most recent activity
     */
    List<ChatSession> findByUserIdOrderByLastMessageAtDesc(Long userId);

    /**
     * Finds sessions updated after a specific time.
     *
     * @param since the timestamp to search from
     * @return list of sessions updated after the given time
     */
    List<ChatSession> findByUpdatedAtAfter(LocalDateTime since);

    /**
     * Checks if a session token exists.
     *
     * @param sessionToken the session token to check
     * @return true if the token exists
     */
    boolean existsBySessionToken(String sessionToken);

    /**
     * Deletes a chat session by its ID.
     *
     * @param id the session ID
     */
    void deleteById(Long id);

    /**
     * Counts all sessions for a user.
     *
     * @param userId the user ID
     * @return count of sessions
     */
    long countByUserId(Long userId);

    /**
     * Finds all sessions (for admin purposes).
     *
     * @return list of all sessions
     */
    List<ChatSession> findAll();
}
