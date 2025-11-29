package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.chat.*;
import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import com.example.ecommerce.marketplace.web.common.GlobalExceptionHandler;
import com.example.ecommerce.marketplace.web.model.chat.AskQuestionRequest;
import com.example.ecommerce.marketplace.web.model.chat.CreateSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChatController.
 * Uses standalone MockMvc setup for isolated controller testing.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CreateChatSessionUseCase createChatSessionUseCase;

    @Mock
    private GetChatSessionsUseCase getChatSessionsUseCase;

    @Mock
    private GetChatMessagesUseCase getChatMessagesUseCase;

    @Mock
    private AskQuestionUseCase askQuestionUseCase;

    @InjectMocks
    private ChatController chatController;

    private ChatSession testSession;
    private ChatMessage testUserMessage;
    private ChatMessage testAssistantMessage;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

        testSession = new ChatSession(
            1L,
            "chat_abc123",
            100L,
            "Test Session",
            Collections.emptyList(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        testUserMessage = new ChatMessage(
            1L,
            1L,
            ChatRole.USER,
            "What products do you have?",
            LocalDateTime.now()
        );

        testAssistantMessage = new ChatMessage(
            2L,
            1L,
            ChatRole.ASSISTANT,
            "We have a variety of products...",
            LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/chat/sessions")
    class ListSessionsTests {

        @Test
        @DisplayName("Should return empty list when user has no sessions")
        void shouldReturnEmptyListWhenNoSessions() throws Exception {
            // Arrange
            when(getChatSessionsUseCase.execute(any(GetChatSessionsCommand.class)))
                .thenReturn(GetChatSessionsResult.success(Collections.emptyList()));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions")
                    .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return sessions list for user")
        void shouldReturnSessionsForUser() throws Exception {
            // Arrange
            List<ChatSession> sessions = Arrays.asList(testSession);
            when(getChatSessionsUseCase.execute(any(GetChatSessionsCommand.class)))
                .thenReturn(GetChatSessionsResult.success(sessions));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions")
                    .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].sessionToken", is("chat_abc123")))
                .andExpect(jsonPath("$[0].userId", is(100)))
                .andExpect(jsonPath("$[0].title", is("Test Session")));
        }

        @Test
        @DisplayName("Should filter active sessions only when activeOnly=true")
        void shouldFilterActiveSessions() throws Exception {
            // Arrange
            when(getChatSessionsUseCase.execute(any(GetChatSessionsCommand.class)))
                .thenReturn(GetChatSessionsResult.success(Arrays.asList(testSession)));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions")
                    .param("userId", "100")
                    .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

            verify(getChatSessionsUseCase).execute(argThat(cmd -> 
                cmd.getUserId().equals(100L) && cmd.getActiveOnly()
            ));
        }

        @Test
        @DisplayName("Should return 400 when userId is missing")
        void shouldReturn400WhenUserIdMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when userId is invalid type")
        void shouldReturn400WhenUserIdInvalidType() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions")
                    .param("userId", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PARAMETER")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/sessions")
    class CreateSessionTests {

        @Test
        @DisplayName("Should create session successfully")
        void shouldCreateSessionSuccessfully() throws Exception {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest(100L, "My Chat");
            when(createChatSessionUseCase.execute(any(CreateChatSessionCommand.class)))
                .thenReturn(CreateChatSessionResult.success(testSession));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sessionToken", is("chat_abc123")))
                .andExpect(jsonPath("$.title", is("Test Session")));
        }

        @Test
        @DisplayName("Should create session with default title when title not provided")
        void shouldCreateSessionWithDefaultTitle() throws Exception {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest(100L, null);
            when(createChatSessionUseCase.execute(any(CreateChatSessionCommand.class)))
                .thenReturn(CreateChatSessionResult.success(testSession));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            verify(createChatSessionUseCase).execute(argThat(cmd -> 
                cmd.getUserId().equals(100L) && cmd.getTitle() == null
            ));
        }

        @Test
        @DisplayName("Should return 400 when userId is null")
        void shouldReturn400WhenUserIdNull() throws Exception {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest(null, "My Chat");

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyEmpty() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/chat/sessions/{sessionId}/messages")
    class GetMessagesTests {

        @Test
        @DisplayName("Should return messages for valid session")
        void shouldReturnMessagesForValidSession() throws Exception {
            // Arrange
            List<ChatMessage> messages = Arrays.asList(testUserMessage, testAssistantMessage);
            when(getChatMessagesUseCase.execute(any(GetChatMessagesCommand.class)))
                .thenReturn(GetChatMessagesResult.success(messages, 1L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/1/messages")
                    .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].role", is("USER")))
                .andExpect(jsonPath("$[0].content", is("What products do you have?")))
                .andExpect(jsonPath("$[1].role", is("ASSISTANT")));
        }

        @Test
        @DisplayName("Should return empty list when session has no messages")
        void shouldReturnEmptyListWhenNoMessages() throws Exception {
            // Arrange
            when(getChatMessagesUseCase.execute(any(GetChatMessagesCommand.class)))
                .thenReturn(GetChatMessagesResult.success(Collections.emptyList(), 1L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/1/messages")
                    .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should apply limit when provided")
        void shouldApplyLimitWhenProvided() throws Exception {
            // Arrange
            when(getChatMessagesUseCase.execute(any(GetChatMessagesCommand.class)))
                .thenReturn(GetChatMessagesResult.success(Arrays.asList(testUserMessage), 1L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/1/messages")
                    .param("userId", "100")
                    .param("limit", "10"))
                .andExpect(status().isOk());

            verify(getChatMessagesUseCase).execute(argThat(cmd -> 
                cmd.getLimit() != null && cmd.getLimit() == 10
            ));
        }

        @Test
        @DisplayName("Should return 404 when session not found")
        void shouldReturn404WhenSessionNotFound() throws Exception {
            // Arrange
            when(getChatMessagesUseCase.execute(any(GetChatMessagesCommand.class)))
                .thenThrow(new CustomBusinessException("SESSION_NOT_FOUND", "Session not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/999/messages")
                    .param("userId", "100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("SESSION_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Session not found")));
        }

        @Test
        @DisplayName("Should return 403 when user does not own session")
        void shouldReturn403WhenAccessDenied() throws Exception {
            // Arrange
            when(getChatMessagesUseCase.execute(any(GetChatMessagesCommand.class)))
                .thenThrow(new CustomBusinessException("ACCESS_DENIED", "Access denied to this session"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/1/messages")
                    .param("userId", "999"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCESS_DENIED")))
                .andExpect(jsonPath("$.message", is("Access denied to this session")));
        }

        @Test
        @DisplayName("Should return 400 when userId is missing")
        void shouldReturn400WhenUserIdMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/chat/sessions/1/messages"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/ask")
    class AskQuestionTests {

        @Test
        @DisplayName("Should return AI response successfully")
        void shouldReturnAiResponseSuccessfully() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                1L, 100L, "What products do you have?", "retailer", "John", "GOLD"
            );
            
            List<Map<String, Object>> sources = Arrays.asList(
                Map.of("type", "product", "id", 1L)
            );
            
            when(askQuestionUseCase.execute(any(AskQuestionCommand.class)))
                .thenReturn(AskQuestionResult.success(
                    testUserMessage, testAssistantMessage, sources, "product_query"
                ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.content", is("What products do you have?")))
                .andExpect(jsonPath("$.assistantMessage.content", is("We have a variety of products...")))
                .andExpect(jsonPath("$.queryType", is("product_query")))
                .andExpect(jsonPath("$.sources", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 400 when question is empty")
        void shouldReturn400WhenQuestionEmpty() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                1L, 100L, "", "retailer", "John", "GOLD"
            );

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
        }

        @Test
        @DisplayName("Should return 400 when sessionId is null")
        void shouldReturn400WhenSessionIdNull() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                null, 100L, "Question?", "retailer", "John", "GOLD"
            );

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
        }

        @Test
        @DisplayName("Should return 404 when session not found")
        void shouldReturn404WhenSessionNotFound() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                999L, 100L, "Question?", "retailer", "John", "GOLD"
            );
            
            when(askQuestionUseCase.execute(any(AskQuestionCommand.class)))
                .thenThrow(new CustomBusinessException("SESSION_NOT_FOUND", "Session not found"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("SESSION_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Session not found")));
        }

        @Test
        @DisplayName("Should return 403 when access denied")
        void shouldReturn403WhenAccessDenied() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                1L, 999L, "Question?", "retailer", "John", "GOLD"
            );
            
            when(askQuestionUseCase.execute(any(AskQuestionCommand.class)))
                .thenThrow(new CustomBusinessException("ACCESS_DENIED", "Access denied to this session"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCESS_DENIED")))
                .andExpect(jsonPath("$.message", is("Access denied to this session")));
        }

        @Test
        @DisplayName("Should return 503 when AI service unavailable")
        void shouldReturn503WhenAiServiceUnavailable() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                1L, 100L, "Question?", "retailer", "John", "GOLD"
            );
            
            when(askQuestionUseCase.execute(any(AskQuestionCommand.class)))
                .thenThrow(new CustomBusinessException("AI_SERVICE_ERROR", "AI service is temporarily unavailable"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("AI_SERVICE_ERROR")))
                .andExpect(jsonPath("$.message", is("AI service is temporarily unavailable")));
        }

        @Test
        @DisplayName("Should return 400 when session is inactive")
        void shouldReturn400WhenSessionInactive() throws Exception {
            // Arrange
            AskQuestionRequest request = new AskQuestionRequest(
                1L, 100L, "Question?", "retailer", "John", "GOLD"
            );
            
            when(askQuestionUseCase.execute(any(AskQuestionCommand.class)))
                .thenThrow(new CustomBusinessException("SESSION_INACTIVE", "Session is no longer active"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chat/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("SESSION_INACTIVE")))
                .andExpect(jsonPath("$.message", is("Session is no longer active")));
        }
    }
}
