package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.chat.*;
import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.web.model.chat.AskQuestionRequest;
import com.example.ecommerce.marketplace.web.model.chat.AskQuestionResponse;
import com.example.ecommerce.marketplace.web.model.chat.ChatMessageResponse;
import com.example.ecommerce.marketplace.web.model.chat.ChatSessionResponse;
import com.example.ecommerce.marketplace.web.model.chat.CreateSessionRequest;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Chat operations.
 * Handles HTTP requests for chat sessions and messages.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat API for AI-powered conversations")
public class ChatController {

    private final CreateChatSessionUseCase createChatSessionUseCase;
    private final GetChatSessionsUseCase getChatSessionsUseCase;
    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final AskQuestionUseCase askQuestionUseCase;

    /**
     * List chat sessions for a user.
     * GET /api/v1/chat/sessions
     *
     * @param userId     the user ID
     * @param activeOnly filter to show only active sessions
     * @return 200 OK with list of sessions
     */
    @Operation(summary = "List chat sessions", description = "Retrieve chat sessions for a user, ordered by most recent activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - user ID is required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/sessions")
    public ResponseEntity<?> listSessions(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        // Build command
        GetChatSessionsCommand command = activeOnly 
            ? GetChatSessionsCommand.activeOnly(userId)
            : GetChatSessionsCommand.all(userId);

        // Execute use case
        GetChatSessionsResult result = getChatSessionsUseCase.execute(command);

        // Convert to response
        List<ChatSessionResponse> sessions = result.getSessions().stream()
            .map(ChatSessionResponse::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    /**
     * Create a new chat session.
     * POST /api/v1/chat/sessions
     *
     * @param request the session creation request
     * @return 201 CREATED with the created session
     */
    @Operation(summary = "Create chat session", description = "Create a new chat session for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Session created successfully",
            content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(
        @Valid @RequestBody CreateSessionRequest request
    ) {
        // Build command
        CreateChatSessionCommand command = new CreateChatSessionCommand(
            request.getUserId(),
            request.getTitle()
        );

        // Execute use case
        CreateChatSessionResult result = createChatSessionUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            ChatSessionResponse response = ChatSessionResponse.fromDomain(result.getSession());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Get messages in a chat session.
     * GET /api/v1/chat/sessions/{sessionId}/messages
     *
     * @param sessionId the session ID
     * @param userId    the user ID (for authorization)
     * @param limit     optional limit on number of messages
     * @return 200 OK with list of messages
     */
    @Operation(summary = "Get chat messages", description = "Retrieve messages from a chat session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to this session",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> getMessages(
        @PathVariable Long sessionId,
        @RequestParam Long userId,
        @RequestParam(required = false) Integer limit
    ) {
        // Build command
        GetChatMessagesCommand command;
        if (limit != null && limit > 0) {
            command = new GetChatMessagesCommand(sessionId, userId, limit, null);
        } else {
            command = GetChatMessagesCommand.all(sessionId, userId);
        }

        // Execute use case
        GetChatMessagesResult result = getChatMessagesUseCase.execute(command);

        // Convert to response
        List<ChatMessageResponse> messages = result.getMessages().stream()
            .map(ChatMessageResponse::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    /**
     * Ask a question in a chat session.
     * POST /api/v1/chat/ask
     *
     * @param request the question request
     * @return 200 OK with the AI response
     */
    @Operation(summary = "Ask a question", description = "Send a question to the AI assistant and receive a response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Question answered successfully",
            content = @Content(schema = @Schema(implementation = AskQuestionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied to this session",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "AI service temporarily unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(
        @Valid @RequestBody AskQuestionRequest request
    ) {
        // Build command
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(request.getSessionId())
            .userId(request.getUserId())
            .question(request.getQuestion())
            .userType(request.getUserType())
            .userName(request.getUserName())
            .loyaltyTier(request.getLoyaltyTier())
            .build();

        // Execute use case
        AskQuestionResult result = askQuestionUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            AskQuestionResponse response = AskQuestionResponse.from(
                result.getUserMessage(),
                result.getAssistantMessage(),
                result.getSources(),
                result.getQueryType()
            );
            return ResponseEntity.ok(response);
        }

        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Maps error codes to appropriate HTTP status codes.
     */
    private HttpStatus mapErrorToStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (errorCode) {
            case "SESSION_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "SESSION_INACTIVE" -> HttpStatus.BAD_REQUEST;
            case "AI_SERVICE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
