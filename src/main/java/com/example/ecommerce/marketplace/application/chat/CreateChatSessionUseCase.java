package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new chat session.
 * Handles validation and session creation logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateChatSessionUseCase {

    private final ChatSessionRepository chatSessionRepository;

    /**
     * Executes the create chat session use case.
     *
     * @param command the command containing session creation data
     * @return the result indicating success or failure with details
     * @throws IllegalArgumentException if userId is null
     * @throws CustomBusinessException if session creation fails
     */
    @Transactional
    public CreateChatSessionResult execute(CreateChatSessionCommand command) {
        // Step 1: Validate userId is provided
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        try {
            // Step 2: Create new chat session using domain factory method
            ChatSession session = ChatSession.createNew(
                command.getUserId(),
                command.getTitle()
            );

            // Step 3: Save session to repository
            ChatSession savedSession = chatSessionRepository.save(session);

            log.info("Created chat session {} for user {}", savedSession.getSessionToken(), command.getUserId());
            
            // Step 4: Return success with the created session
            return CreateChatSessionResult.success(savedSession);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error creating chat session for user {}: {}", command.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create chat session for user {}: {}", command.getUserId(), e.getMessage());
            throw new CustomBusinessException("SESSION_CREATION_FAILED", "Failed to create chat session: " + e.getMessage(), e);
        }
    }
}
