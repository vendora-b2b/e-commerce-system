package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for retrieving chat sessions for a user.
 * Returns sessions ordered by most recent activity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetChatSessionsUseCase {

    private final ChatSessionRepository chatSessionRepository;

    /**
     * Executes the get chat sessions use case.
     *
     * @param command the command containing query parameters
     * @return the result containing the list of sessions
     * @throws IllegalArgumentException if userId is null
     * @throws CustomBusinessException if session retrieval fails
     */
    @Transactional(readOnly = true)
    public GetChatSessionsResult execute(GetChatSessionsCommand command) {
        // Step 1: Validate userId is provided
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        try {
            List<ChatSession> sessions;

            // Step 2: Query sessions based on active filter
            if (command.getActiveOnly()) {
                sessions = chatSessionRepository.findByUserIdAndActiveTrue(command.getUserId());
            } else {
                sessions = chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(command.getUserId());
            }

            log.debug("Retrieved {} chat sessions for user {}", sessions.size(), command.getUserId());
            
            // Step 3: Return success with sessions
            return GetChatSessionsResult.success(sessions);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error retrieving chat sessions for user {}: {}", command.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve chat sessions for user {}: {}", command.getUserId(), e.getMessage());
            throw new CustomBusinessException("SESSION_QUERY_FAILED", "Failed to retrieve sessions: " + e.getMessage(), e);
        }
    }
}
