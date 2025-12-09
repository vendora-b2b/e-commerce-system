package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for updating a chat session title.
 * Handles validation and authorization before updating.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateChatSessionTitleUseCase {

    private final ChatSessionRepository chatSessionRepository;

    /**
     * Executes the update chat session title use case.
     *
     * @param command the command containing session ID, user ID, and new title
     * @return the result indicating success or failure with details
     * @throws IllegalArgumentException if required inputs are invalid
     * @throws CustomBusinessException  if session not found or access denied
     */
    @Transactional
    public UpdateChatSessionTitleResult execute(UpdateChatSessionTitleCommand command) {
        // Step 1: Validate inputs
        if (command.getSessionId() == null) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (command.getNewTitle() == null || command.getNewTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("New title cannot be empty");
        }

        try {
            // Step 2: Verify session exists
            Optional<ChatSession> sessionOpt = chatSessionRepository.findById(command.getSessionId());
            if (sessionOpt.isEmpty()) {
                throw new CustomBusinessException("SESSION_NOT_FOUND", "Session not found");
            }

            ChatSession session = sessionOpt.get();

            // Step 3: Verify user owns the session (authorization)
            if (!command.getUserId().equals(session.getUserId())) {
                throw new CustomBusinessException("ACCESS_DENIED", "Access denied to this session");
            }

            // Step 4: Update the title
            session.setTitle(command.getNewTitle().trim());
            session.setUpdatedAt(LocalDateTime.now());

            // Step 5: Validate the new title
            if (!session.validateTitle()) {
                throw new IllegalArgumentException("Title is invalid or too long (max 200 characters)");
            }

            // Step 6: Save the updated session
            ChatSession updatedSession = chatSessionRepository.save(session);

            log.info("Updated title for chat session {} to: {}", session.getId(), command.getNewTitle());

            // Step 7: Return success
            return UpdateChatSessionTitleResult.success(updatedSession);

        } catch (CustomBusinessException e) {
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error updating chat session {}: {}", command.getSessionId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update chat session {}: {}", command.getSessionId(), e.getMessage());
            throw new CustomBusinessException("SESSION_UPDATE_FAILED", "Failed to update session: " + e.getMessage(), e);
        }
    }
}
