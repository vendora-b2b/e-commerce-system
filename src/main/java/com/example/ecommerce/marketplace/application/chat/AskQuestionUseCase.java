package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatMessageRepository;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationRequest;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for asking a question in a chat session.
 * Orchestrates conversation history retrieval, AI service call, and message persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AskQuestionUseCase {

    private static final int MAX_HISTORY_MESSAGES = 10;  // Limit context window

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiServiceClient aiServiceClient;

    /**
     * Executes the ask question use case.
     *
     * @param command the command containing the question and context
     * @return the result containing the AI response
     * @throws IllegalArgumentException if required inputs are invalid
     * @throws CustomBusinessException if session not found, access denied, or AI service fails
     */
    @Transactional
    public AskQuestionResult execute(AskQuestionCommand command) {
        // Step 1: Validate inputs
        if (command.getSessionId() == null) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (command.getQuestion() == null || command.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

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

        // Step 4: Verify session is active
        if (session.getActive() != null && !session.getActive()) {
            throw new CustomBusinessException("SESSION_INACTIVE", "Session is no longer active");
        }

        try {
            // Step 5: Save user message
            ChatMessage userMessage = session.addUserMessage(command.getQuestion());
            ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);

            // Step 6: Retrieve recent conversation history for context
            List<ChatMessage> history = chatMessageRepository.findRecentBySessionId(
                command.getSessionId(), MAX_HISTORY_MESSAGES
            );

            // Step 7: Build AI request with history and user profile
            ChatGenerationRequest aiRequest = buildAiRequest(command, history);

            // Step 8: Call AI service
            ChatGenerationResponse aiResponse;
            try {
                aiResponse = aiServiceClient.generateChatResponse(aiRequest);
            } catch (AiServiceException e) {
                log.error("AI service error in session {}: {}", command.getSessionId(), e.getMessage());
                throw new CustomBusinessException("AI_SERVICE_ERROR", "AI service is temporarily unavailable: " + e.getMessage(), e);
            }

            // Step 9: Save assistant response
            ChatMessage assistantMessage = session.addAssistantMessage(aiResponse.getResponse());
            ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

            // Step 10: Update session metadata
            session.setLastMessageAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            
            // Auto-generate title from first question if title is default
            if ("New Chat".equals(session.getTitle())) {
                session.autoGenerateTitle();
            }
            
            chatSessionRepository.save(session);

            log.info("Processed question in session {}: query_type={}", 
                    command.getSessionId(), aiResponse.getQueryType());

            // Step 11: Return success with both messages
            return AskQuestionResult.success(
                savedUserMessage,
                savedAssistantMessage,
                aiResponse.getSources(),
                aiResponse.getQueryType()
            );

        } catch (CustomBusinessException | AiServiceException e) {
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error processing question in session {}: {}", command.getSessionId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to process question in session {}: {}", command.getSessionId(), e.getMessage());
            throw new CustomBusinessException("QUESTION_PROCESSING_FAILED", "Failed to process question: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the AI service request from command and history.
     */
    private ChatGenerationRequest buildAiRequest(AskQuestionCommand command, List<ChatMessage> history) {
        // Convert domain messages to AI request format
        List<ChatGenerationRequest.ChatMessage> historyMessages = history.stream()
            .map(msg -> ChatGenerationRequest.ChatMessage.builder()
                .role(msg.getRole().name().toLowerCase())
                .content(msg.getContent())
                .build())
            .collect(Collectors.toList());

        // Build user profile if available
        ChatGenerationRequest.UserProfile userProfile = null;
        if (command.getUserId() != null) {
            userProfile = ChatGenerationRequest.UserProfile.builder()
                .userId(command.getUserId())
                .userType(command.getUserType())
                .name(command.getUserName())
                .loyaltyTier(command.getLoyaltyTier())
                .build();
        }

        return ChatGenerationRequest.builder()
            .query(command.getQuestion())
            .history(historyMessages)
            .userProfile(userProfile)
            .build();
    }
}
