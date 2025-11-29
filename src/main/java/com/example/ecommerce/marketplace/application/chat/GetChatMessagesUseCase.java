package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatMessageRepository;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Use case for retrieving chat messages from a session.
 * Handles authorization and message retrieval logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetChatMessagesUseCase {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Executes the get chat messages use case.
     *
     * @param command the command containing query parameters
     * @return the result containing the list of messages
     * @throws IllegalArgumentException if sessionId or userId is null
     * @throws CustomBusinessException if session not found or access denied
     */
    @Transactional(readOnly = true)
    public GetChatMessagesResult execute(GetChatMessagesCommand command) {
        // Step 1: Validate sessionId is provided
        if (command.getSessionId() == null) {
            throw new IllegalArgumentException("Session ID is required");
        }

        // Step 2: Validate userId is provided
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        try {
            // Step 3: Verify session exists
            Optional<ChatSession> sessionOpt = chatSessionRepository.findById(command.getSessionId());
            if (sessionOpt.isEmpty()) {
                throw new CustomBusinessException("SESSION_NOT_FOUND", "Session not found");
            }

            ChatSession session = sessionOpt.get();

            // Step 4: Verify user owns the session (authorization)
            if (!command.getUserId().equals(session.getUserId())) {
                throw new CustomBusinessException("ACCESS_DENIED", "Access denied to this session");
            }

            // Step 5: Query messages based on limit
            List<ChatMessage> messages;
            if (command.getLimit() != null && command.getLimit() > 0) {
                messages = chatMessageRepository.findRecentBySessionId(command.getSessionId(), command.getLimit());
            } else {
                messages = chatMessageRepository.findBySessionId(command.getSessionId());
            }

            log.debug("Retrieved {} messages for session {}", messages.size(), command.getSessionId());
            
            // Step 6: Return success with messages
            return GetChatMessagesResult.success(messages, command.getSessionId());
            
        } catch (CustomBusinessException e) {
            log.warn("Business error retrieving messages for session {}: {}", command.getSessionId(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error retrieving messages for session {}: {}", command.getSessionId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve messages for session {}: {}", command.getSessionId(), e.getMessage());
            throw new CustomBusinessException("MESSAGE_QUERY_FAILED", "Failed to retrieve messages: " + e.getMessage(), e);
        }
    }
}
