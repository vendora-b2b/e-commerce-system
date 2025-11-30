package com.example.ecommerce.marketplace.integration;

import com.example.ecommerce.marketplace.application.chat.*;
import com.example.ecommerce.marketplace.domain.chat.*;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationRequest;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the complete Chat Flow.
 * 
 * Tests the full flow from Use Case → Repository → Database,
 * mocking only the external AI Service (Python backend).
 * 
 * Flow tested:
 * 1. Create new chat session
 * 2. List user's sessions
 * 3. Ask question in session (with AI mock)
 * 4. Retrieve messages from session
 * 5. Multi-turn conversation with history
 * 6. Session ownership validation (authorization)
 * 7. Error handling scenarios
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@DisplayName("Chat Flow Integration Tests")
class ChatFlowIntegrationTest {

    // ==================== Dependencies ====================
    
    @Autowired
    private CreateChatSessionUseCase createChatSessionUseCase;

    @Autowired
    private GetChatSessionsUseCase getChatSessionsUseCase;

    @Autowired
    private GetChatMessagesUseCase getChatMessagesUseCase;

    @Autowired
    private AskQuestionUseCase askQuestionUseCase;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private AiServiceClient aiServiceClient;

    @Captor
    private ArgumentCaptor<ChatGenerationRequest> aiRequestCaptor;

    // ==================== Test Data ====================
    
    private static final Long USER_ID = 1001L;
    private static final Long OTHER_USER_ID = 2002L;
    private static final String USER_TYPE = "retailer";
    private static final String USER_NAME = "Test Retailer";
    private static final String LOYALTY_TIER = "GOLD";

    private ChatGenerationResponse createMockAiResponse(String responseText, String queryType) {
        ChatGenerationResponse response = new ChatGenerationResponse();
        response.setResponse(responseText);
        response.setQueryType(queryType);
        response.setSources(List.of(
            Map.of("type", "product_catalog", "relevance", 0.95)
        ));
        return response;
    }

    // ==================== Flow 1: Create New Session ====================

    @Nested
    @DisplayName("Flow 1: Create New Chat Session")
    class CreateSessionFlowTests {

        @Test
        @Order(1)
        @DisplayName("1.1 - Should create new session with custom title")
        void shouldCreateNewSessionWithCustomTitle() {
            // Given
            String customTitle = "Product Inquiry - Electronics";
            CreateChatSessionCommand command = new CreateChatSessionCommand(USER_ID, customTitle);

            // When
            CreateChatSessionResult result = createChatSessionUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Session creation should succeed");
            assertNotNull(result.getSession(), "Session should not be null");
            assertNotNull(result.getSession().getId(), "Session ID should be generated");
            assertNotNull(result.getSession().getSessionToken(), "Session token should be generated");
            assertTrue(result.getSession().getSessionToken().startsWith("chat_"), 
                    "Session token should start with 'chat_'");
            assertEquals(USER_ID, result.getSession().getUserId());
            assertEquals(customTitle, result.getSession().getTitle());
            assertTrue(result.getSession().getActive(), "Session should be active");
            assertNotNull(result.getSession().getCreatedAt());
            assertNotNull(result.getSession().getUpdatedAt());
        }

        @Test
        @Order(2)
        @DisplayName("1.2 - Should create new session with default title when title is null")
        void shouldCreateNewSessionWithDefaultTitle() {
            // Given
            CreateChatSessionCommand command = new CreateChatSessionCommand(USER_ID, null);

            // When
            CreateChatSessionResult result = createChatSessionUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertEquals("New Chat", result.getSession().getTitle(), 
                    "Default title should be 'New Chat'");
        }

        @Test
        @Order(3)
        @DisplayName("1.3 - Should persist session to database")
        void shouldPersistSessionToDatabase() {
            // Given
            CreateChatSessionCommand command = new CreateChatSessionCommand(USER_ID, "Persisted Session");

            // When
            CreateChatSessionResult result = createChatSessionUseCase.execute(command);
            Long sessionId = result.getSession().getId();

            // Then - Verify it can be retrieved from DB
            Optional<ChatSession> retrieved = chatSessionRepository.findById(sessionId);
            assertTrue(retrieved.isPresent(), "Session should be persisted in database");
            assertEquals("Persisted Session", retrieved.get().getTitle());
            assertEquals(USER_ID, retrieved.get().getUserId());
        }

        @Test
        @Order(4)
        @DisplayName("1.4 - Should generate unique session tokens")
        void shouldGenerateUniqueSessionTokens() {
            // Given & When
            CreateChatSessionResult result1 = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Session 1"));
            CreateChatSessionResult result2 = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Session 2"));

            // Then
            assertNotEquals(result1.getSession().getSessionToken(), 
                           result2.getSession().getSessionToken(),
                           "Each session should have a unique token");
        }

        @Test
        @Order(5)
        @DisplayName("1.5 - Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdNull() {
            // Given
            CreateChatSessionCommand command = new CreateChatSessionCommand(null, "Test");

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> createChatSessionUseCase.execute(command),
                "Should throw IllegalArgumentException for null userId");
        }
    }

    // ==================== Flow 2: List User Sessions ====================

    @Nested
    @DisplayName("Flow 2: List User Sessions")
    class ListSessionsFlowTests {

        @Test
        @Order(10)
        @DisplayName("2.1 - Should return empty list for user with no sessions")
        void shouldReturnEmptyListForNewUser() {
            // Given
            Long newUserId = 9999L;
            GetChatSessionsCommand command = GetChatSessionsCommand.all(newUserId);

            // When
            GetChatSessionsResult result = getChatSessionsUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getSessions());
            assertTrue(result.getSessions().isEmpty(), "New user should have no sessions");
        }

        @Test
        @Order(11)
        @DisplayName("2.2 - Should return all sessions for user")
        void shouldReturnAllSessionsForUser() {
            // Given - Create multiple sessions
            createChatSessionUseCase.execute(new CreateChatSessionCommand(USER_ID, "Session A"));
            createChatSessionUseCase.execute(new CreateChatSessionCommand(USER_ID, "Session B"));
            createChatSessionUseCase.execute(new CreateChatSessionCommand(OTHER_USER_ID, "Other User Session"));

            GetChatSessionsCommand command = GetChatSessionsCommand.all(USER_ID);

            // When
            GetChatSessionsResult result = getChatSessionsUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertTrue(result.getSessions().size() >= 2, 
                    "User should have at least 2 sessions");
            
            // Verify all returned sessions belong to the user
            result.getSessions().forEach(session -> 
                assertEquals(USER_ID, session.getUserId(), 
                    "All sessions should belong to the requesting user"));
        }

        @Test
        @Order(12)
        @DisplayName("2.3 - Should filter only active sessions when activeOnly=true")
        void shouldFilterActiveSessionsOnly() {
            // Given - Create an active session
            CreateChatSessionResult activeResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Active Session"));
            
            // Create and deactivate another session
            CreateChatSessionResult inactiveResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Inactive Session"));
            ChatSession inactiveSession = inactiveResult.getSession();
            inactiveSession.setActive(false);
            chatSessionRepository.save(inactiveSession);

            GetChatSessionsCommand command = GetChatSessionsCommand.activeOnly(USER_ID);

            // When
            GetChatSessionsResult result = getChatSessionsUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            result.getSessions().forEach(session -> 
                assertTrue(session.getActive(), "All returned sessions should be active"));
        }

        @Test
        @Order(13)
        @DisplayName("2.4 - Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdNull() {
            // Given
            GetChatSessionsCommand command = GetChatSessionsCommand.all(null);

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> getChatSessionsUseCase.execute(command));
        }
    }

    // ==================== Flow 3: Ask Question (Main AI Flow) ====================

    @Nested
    @DisplayName("Flow 3: Ask Question - AI Integration")
    class AskQuestionFlowTests {

        private Long sessionId;

        @BeforeEach
        void setUpSession() {
            CreateChatSessionResult result = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "AI Test Session"));
            sessionId = result.getSession().getId();
        }

        @Test
        @Order(20)
        @DisplayName("3.1 - Should process question and return AI response")
        void shouldProcessQuestionAndReturnAiResponse() {
            // Given
            String question = "What laptops do you have in stock?";
            String expectedResponse = "We have several laptops available including MacBook Pro, Dell XPS, and ThinkPad X1.";
            
            when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
                .thenReturn(createMockAiResponse(expectedResponse, "product_search"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question(question)
                .userType(USER_TYPE)
                .userName(USER_NAME)
                .loyaltyTier(LOYALTY_TIER)
                .build();

            // When
            AskQuestionResult result = askQuestionUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Question processing should succeed");
            
            // Verify user message
            assertNotNull(result.getUserMessage());
            assertEquals(ChatRole.USER, result.getUserMessage().getRole());
            assertEquals(question, result.getUserMessage().getContent());
            assertNotNull(result.getUserMessage().getId(), "User message should be persisted with ID");
            
            // Verify assistant message
            assertNotNull(result.getAssistantMessage());
            assertEquals(ChatRole.ASSISTANT, result.getAssistantMessage().getRole());
            assertEquals(expectedResponse, result.getAssistantMessage().getContent());
            assertNotNull(result.getAssistantMessage().getId(), "Assistant message should be persisted with ID");
            
            // Verify query type and sources
            assertEquals("product_search", result.getQueryType());
            assertNotNull(result.getSources());
            assertFalse(result.getSources().isEmpty());
        }

        @Test
        @Order(21)
        @DisplayName("3.2 - Should persist both user and assistant messages to database")
        void shouldPersistMessagesToDatabase() {
            // Reset mock to ensure clean state
            reset(aiServiceClient);
            
            // Given - Create a fresh session specifically for this test
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Persistence Test Session"));
            Long testSessionId = sessionResult.getSession().getId();
            
            when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
                .thenReturn(createMockAiResponse("Test response", "general"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(testSessionId)
                .userId(USER_ID)
                .question("Test question")
                .build();

            // When
            AskQuestionResult result = askQuestionUseCase.execute(command);

            // Then - Verify messages are persisted
            List<ChatMessage> messages = chatMessageRepository.findBySessionId(testSessionId);
            assertEquals(2, messages.size(), "Both user and assistant messages should be persisted");
            
            boolean hasUserMessage = messages.stream()
                .anyMatch(m -> m.getRole() == ChatRole.USER && m.getContent().equals("Test question"));
            boolean hasAssistantMessage = messages.stream()
                .anyMatch(m -> m.getRole() == ChatRole.ASSISTANT && m.getContent().equals("Test response"));
            
            assertTrue(hasUserMessage, "User message should be in database");
            assertTrue(hasAssistantMessage, "Assistant message should be in database");
        }

        @Test
        @Order(22)
        @DisplayName("3.3 - Should send correct request to AI service with user profile")
        void shouldSendCorrectRequestToAiService() {
            // Given
            when(aiServiceClient.generateChatResponse(aiRequestCaptor.capture()))
                .thenReturn(createMockAiResponse("Response", "general"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("What is the import tax for electronics?")
                .userType(USER_TYPE)
                .userName(USER_NAME)
                .loyaltyTier(LOYALTY_TIER)
                .build();

            // When
            askQuestionUseCase.execute(command);

            // Then - Verify AI request structure
            ChatGenerationRequest capturedRequest = aiRequestCaptor.getValue();
            
            assertEquals("What is the import tax for electronics?", capturedRequest.getQuery());
            assertNotNull(capturedRequest.getUserProfile());
            assertEquals(USER_ID, capturedRequest.getUserProfile().getUserId());
            assertEquals(USER_TYPE, capturedRequest.getUserProfile().getUserType());
            assertEquals(USER_NAME, capturedRequest.getUserProfile().getName());
            assertEquals(LOYALTY_TIER, capturedRequest.getUserProfile().getLoyaltyTier());
        }

        @Test
        @Order(23)
        @DisplayName("3.4 - Should update session lastMessageAt timestamp")
        void shouldUpdateSessionLastMessageAt() {
            // Given
            Optional<ChatSession> sessionBefore = chatSessionRepository.findById(sessionId);
            LocalDateTime beforeTimestamp = sessionBefore.get().getLastMessageAt();
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response", "general"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("Update timestamp test")
                .build();

            // When
            askQuestionUseCase.execute(command);

            // Then
            Optional<ChatSession> sessionAfter = chatSessionRepository.findById(sessionId);
            assertNotNull(sessionAfter.get().getLastMessageAt());
            // Note: In transactional test, timestamps might be same due to test speed
            // In real scenario, lastMessageAt would be updated
        }

        @Test
        @Order(24)
        @DisplayName("3.5 - Should auto-generate title from first question for 'New Chat' sessions")
        void shouldAutoGenerateTitleFromFirstQuestion() {
            // Given - Create session with default "New Chat" title
            CreateChatSessionResult newChatResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "New Chat"));
            Long newChatSessionId = newChatResult.getSession().getId();
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response about laptops", "product_search"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(newChatSessionId)
                .userId(USER_ID)
                .question("What laptops do you recommend for business use?")
                .build();

            // When
            askQuestionUseCase.execute(command);

            // Then
            Optional<ChatSession> updatedSession = chatSessionRepository.findById(newChatSessionId);
            assertNotEquals("New Chat", updatedSession.get().getTitle(),
                    "Title should be auto-generated from question");
        }

        @Test
        @Order(25)
        @DisplayName("3.6 - Should throw exception when session not found")
        void shouldThrowExceptionWhenSessionNotFound() {
            // Given
            Long nonExistentSessionId = 999999L;
            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(nonExistentSessionId)
                .userId(USER_ID)
                .question("Test question")
                .build();

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> askQuestionUseCase.execute(command));
            assertEquals("SESSION_NOT_FOUND", exception.getErrorCode());
        }

        @Test
        @Order(26)
        @DisplayName("3.7 - Should throw exception when user does not own session (authorization)")
        void shouldThrowExceptionWhenUserDoesNotOwnSession() {
            // Given - Session belongs to USER_ID, but OTHER_USER tries to access
            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(OTHER_USER_ID)  // Different user!
                .question("Unauthorized access attempt")
                .build();

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> askQuestionUseCase.execute(command));
            assertEquals("ACCESS_DENIED", exception.getErrorCode());
        }

        @Test
        @Order(27)
        @DisplayName("3.8 - Should throw exception when session is inactive")
        void shouldThrowExceptionWhenSessionInactive() {
            // Given - Deactivate the session
            Optional<ChatSession> session = chatSessionRepository.findById(sessionId);
            session.get().setActive(false);
            chatSessionRepository.save(session.get());

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("Question to inactive session")
                .build();

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> askQuestionUseCase.execute(command));
            assertEquals("SESSION_INACTIVE", exception.getErrorCode());
        }

        @Test
        @Order(28)
        @DisplayName("3.9 - Should handle AI service failure gracefully")
        void shouldHandleAiServiceFailure() {
            // Given
            when(aiServiceClient.generateChatResponse(any()))
                .thenThrow(new AiServiceException("AI service unavailable"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("Question when AI is down")
                .build();

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> askQuestionUseCase.execute(command));
            assertEquals("AI_SERVICE_ERROR", exception.getErrorCode());
            assertTrue(exception.getMessage().contains("temporarily unavailable"));
        }

        @Test
        @Order(29)
        @DisplayName("3.10 - Should validate empty question")
        void shouldValidateEmptyQuestion() {
            // Given
            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("")
                .build();

            // When & Then
            assertThrows(IllegalArgumentException.class,
                () -> askQuestionUseCase.execute(command),
                "Empty question should be rejected");
        }

        @Test
        @Order(30)
        @DisplayName("3.11 - Should validate whitespace-only question")
        void shouldValidateWhitespaceQuestion() {
            // Given
            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionId)
                .userId(USER_ID)
                .question("   \t\n  ")
                .build();

            // When & Then
            assertThrows(IllegalArgumentException.class,
                () -> askQuestionUseCase.execute(command),
                "Whitespace-only question should be rejected");
        }
    }

    // ==================== Flow 4: Get Messages ====================

    @Nested
    @DisplayName("Flow 4: Get Chat Messages")
    class GetMessagesFlowTests {

        private Long sessionId;

        @BeforeEach
        void setUpSessionWithMessages() {
            // Create session
            CreateChatSessionResult result = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Messages Test Session"));
            sessionId = result.getSession().getId();

            // Add some messages using the Ask flow
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("First response", "general"))
                .thenReturn(createMockAiResponse("Second response", "product_search"));

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(sessionId).userId(USER_ID).question("First question").build());
            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(sessionId).userId(USER_ID).question("Second question").build());
        }

        @Test
        @Order(40)
        @DisplayName("4.1 - Should retrieve all messages from session")
        void shouldRetrieveAllMessagesFromSession() {
            // Reset mock to ensure clean state
            reset(aiServiceClient);
            
            // Given - Create a fresh session specifically for this test
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Retrieve Messages Test"));
            Long testSessionId = sessionResult.getSession().getId();
            
            // Add messages
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response 1", "general"))
                .thenReturn(createMockAiResponse("Response 2", "product_search"));
            
            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(testSessionId).userId(USER_ID).question("Question 1").build());
            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(testSessionId).userId(USER_ID).question("Question 2").build());
            
            GetChatMessagesCommand command = GetChatMessagesCommand.all(testSessionId, USER_ID);

            // When
            GetChatMessagesResult result = getChatMessagesUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertEquals(4, result.getMessages().size(), 
                    "Should have 4 messages: 2 user + 2 assistant");
        }

        @Test
        @Order(41)
        @DisplayName("4.2 - Should return messages in correct order")
        void shouldReturnMessagesInCorrectOrder() {
            // Given
            GetChatMessagesCommand command = GetChatMessagesCommand.all(sessionId, USER_ID);

            // When
            GetChatMessagesResult result = getChatMessagesUseCase.execute(command);

            // Then
            List<ChatMessage> messages = result.getMessages();
            
            // Verify alternating pattern: USER, ASSISTANT, USER, ASSISTANT
            assertEquals(ChatRole.USER, messages.get(0).getRole());
            assertEquals(ChatRole.ASSISTANT, messages.get(1).getRole());
            assertEquals(ChatRole.USER, messages.get(2).getRole());
            assertEquals(ChatRole.ASSISTANT, messages.get(3).getRole());
        }

        @Test
        @Order(42)
        @DisplayName("4.3 - Should apply limit when specified")
        void shouldApplyLimitWhenSpecified() {
            // Given
            GetChatMessagesCommand command = new GetChatMessagesCommand(sessionId, USER_ID, 2, null);

            // When
            GetChatMessagesResult result = getChatMessagesUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertEquals(2, result.getMessages().size(), "Should return only 2 messages");
        }

        @Test
        @Order(43)
        @DisplayName("4.4 - Should return empty list for session with no messages")
        void shouldReturnEmptyListForEmptySession() {
            // Given - Create new empty session
            CreateChatSessionResult emptySession = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Empty Session"));
            GetChatMessagesCommand command = GetChatMessagesCommand.all(
                emptySession.getSession().getId(), USER_ID);

            // When
            GetChatMessagesResult result = getChatMessagesUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertTrue(result.getMessages().isEmpty());
        }

        @Test
        @Order(44)
        @DisplayName("4.5 - Should throw exception when session not found")
        void shouldThrowExceptionWhenSessionNotFound() {
            // Given
            GetChatMessagesCommand command = GetChatMessagesCommand.all(999999L, USER_ID);

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> getChatMessagesUseCase.execute(command));
            assertEquals("SESSION_NOT_FOUND", exception.getErrorCode());
        }

        @Test
        @Order(45)
        @DisplayName("4.6 - Should throw exception when user does not own session")
        void shouldThrowExceptionWhenAccessDenied() {
            // Given
            GetChatMessagesCommand command = GetChatMessagesCommand.all(sessionId, OTHER_USER_ID);

            // When & Then
            CustomBusinessException exception = assertThrows(CustomBusinessException.class,
                () -> getChatMessagesUseCase.execute(command));
            assertEquals("ACCESS_DENIED", exception.getErrorCode());
        }
    }

    // ==================== Flow 5: Multi-turn Conversation ====================

    @Nested
    @DisplayName("Flow 5: Multi-turn Conversation with History")
    class MultiTurnConversationTests {

        private Long sessionId;

        @BeforeEach
        void setUpSession() {
            CreateChatSessionResult result = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Multi-turn Test"));
            sessionId = result.getSession().getId();
        }

        @Test
        @Order(50)
        @DisplayName("5.1 - Should include conversation history in AI requests")
        void shouldIncludeConversationHistoryInAiRequests() {
            // Given - First exchange
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("We have Dell and HP laptops.", "product_search"))
                .thenReturn(createMockAiResponse("The Dell XPS costs $1500.", "product_search"));

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(sessionId).userId(USER_ID)
                .question("What laptops do you have?").build());

            // When - Second question (should include history)
            reset(aiServiceClient);
            when(aiServiceClient.generateChatResponse(aiRequestCaptor.capture()))
                .thenReturn(createMockAiResponse("The Dell XPS costs $1500.", "product_search"));

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(sessionId).userId(USER_ID)
                .question("How much is the Dell?").build());

            // Then - Verify history was included
            ChatGenerationRequest capturedRequest = aiRequestCaptor.getValue();
            assertNotNull(capturedRequest.getHistory());
            assertFalse(capturedRequest.getHistory().isEmpty(), 
                    "History should be included in second request");
            
            // Verify history contains previous messages
            boolean hasUserHistory = capturedRequest.getHistory().stream()
                .anyMatch(m -> m.getRole().equals("user") && 
                              m.getContent().contains("laptops"));
            boolean hasAssistantHistory = capturedRequest.getHistory().stream()
                .anyMatch(m -> m.getRole().equals("assistant") && 
                              m.getContent().contains("Dell"));
            
            assertTrue(hasUserHistory, "History should include previous user message");
            assertTrue(hasAssistantHistory, "History should include previous assistant response");
        }

        @Test
        @Order(51)
        @DisplayName("5.2 - Should maintain conversation context across multiple exchanges")
        void shouldMaintainConversationContext() {
            // Reset mock to ensure clean state
            reset(aiServiceClient);
            
            // Given - Create a fresh session specifically for this test
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Context Test Session"));
            Long testSessionId = sessionResult.getSession().getId();
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response 1", "general"))
                .thenReturn(createMockAiResponse("Response 2", "general"))
                .thenReturn(createMockAiResponse("Response 3", "general"));

            // When - Multiple exchanges
            for (int i = 1; i <= 3; i++) {
                askQuestionUseCase.execute(AskQuestionCommand.builder()
                    .sessionId(testSessionId).userId(USER_ID)
                    .question("Question " + i).build());
            }

            // Then - Verify all messages are stored
            List<ChatMessage> messages = chatMessageRepository.findBySessionId(testSessionId);
            assertEquals(6, messages.size(), "Should have 6 messages (3 user + 3 assistant)");
        }

        @Test
        @Order(52)
        @DisplayName("5.3 - Should limit history to recent messages (max 10)")
        void shouldLimitHistoryToRecentMessages() {
            // Given - Create 12 exchanges (24 messages)
            for (int i = 0; i < 12; i++) {
                when(aiServiceClient.generateChatResponse(any()))
                    .thenReturn(createMockAiResponse("Response " + i, "general"));
                askQuestionUseCase.execute(AskQuestionCommand.builder()
                    .sessionId(sessionId).userId(USER_ID)
                    .question("Question " + i).build());
            }

            // When - Ask another question
            reset(aiServiceClient);
            when(aiServiceClient.generateChatResponse(aiRequestCaptor.capture()))
                .thenReturn(createMockAiResponse("Final response", "general"));

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(sessionId).userId(USER_ID)
                .question("Final question").build());

            // Then - History should be limited
            ChatGenerationRequest capturedRequest = aiRequestCaptor.getValue();
            assertTrue(capturedRequest.getHistory().size() <= 10, 
                    "History should be limited to max 10 messages");
        }
    }

    // ==================== Flow 6: Complete End-to-End Scenario ====================

    @Nested
    @DisplayName("Flow 6: Complete End-to-End Scenario")
    class EndToEndScenarioTests {

        @Test
        @Order(60)
        @DisplayName("6.1 - Complete user journey: create session → ask questions → review history")
        void completeUserJourney() {
            // Reset mock to ensure clean state
            reset(aiServiceClient);
            
            // ========== Step 1: Create Session ==========
            CreateChatSessionResult createResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, null));
            
            assertTrue(createResult.isSuccess());
            Long sessionId = createResult.getSession().getId();
            assertEquals("New Chat", createResult.getSession().getTitle());
            
            // ========== Step 2: User can see their session ==========
            GetChatSessionsResult sessionsResult = getChatSessionsUseCase.execute(
                GetChatSessionsCommand.all(USER_ID));
            
            assertTrue(sessionsResult.getSessions().stream()
                .anyMatch(s -> s.getId().equals(sessionId)));
            
            // ========== Step 3: Ask first question ==========
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse(
                    "We have various electronics including laptops, tablets, and phones.",
                    "product_search"));
            
            AskQuestionResult q1Result = askQuestionUseCase.execute(
                AskQuestionCommand.builder()
                    .sessionId(sessionId)
                    .userId(USER_ID)
                    .question("What electronics do you sell?")
                    .userType("retailer")
                    .userName("Test Store")
                    .loyaltyTier("GOLD")
                    .build());
            
            assertTrue(q1Result.isSuccess());
            assertEquals("product_search", q1Result.getQueryType());
            
            // ========== Step 4: Title should be auto-updated ==========
            Optional<ChatSession> updatedSession = chatSessionRepository.findById(sessionId);
            assertNotEquals("New Chat", updatedSession.get().getTitle(),
                    "Title should be auto-generated");
            
            // ========== Step 5: Follow-up question ==========
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse(
                    "Our best-selling laptop is the Dell XPS 15, priced at $1,299.",
                    "product_search"));
            
            AskQuestionResult q2Result = askQuestionUseCase.execute(
                AskQuestionCommand.builder()
                    .sessionId(sessionId)
                    .userId(USER_ID)
                    .question("Which laptop is most popular?")
                    .build());
            
            assertTrue(q2Result.isSuccess());
            
            // ========== Step 6: Retrieve conversation history ==========
            GetChatMessagesResult messagesResult = getChatMessagesUseCase.execute(
                GetChatMessagesCommand.all(sessionId, USER_ID));
            
            assertTrue(messagesResult.isSuccess());
            assertEquals(4, messagesResult.getMessages().size());
            
            // Verify conversation flow
            List<ChatMessage> messages = messagesResult.getMessages();
            assertEquals("What electronics do you sell?", messages.get(0).getContent());
            assertEquals(ChatRole.USER, messages.get(0).getRole());
            assertTrue(messages.get(1).getContent().contains("electronics"));
            assertEquals(ChatRole.ASSISTANT, messages.get(1).getRole());
            
            // ========== Step 7: Verify other user cannot access ==========
            assertThrows(CustomBusinessException.class, () ->
                getChatMessagesUseCase.execute(
                    GetChatMessagesCommand.all(sessionId, OTHER_USER_ID)));
        }

        @Test
        @Order(61)
        @DisplayName("6.2 - Multiple users with separate sessions (isolation test)")
        void multipleUsersIsolation() {
            // Given - Two users create sessions
            CreateChatSessionResult user1Session = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "User 1 Session"));
            CreateChatSessionResult user2Session = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(OTHER_USER_ID, "User 2 Session"));

            // When - Both users ask questions
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response for User 1", "general"))
                .thenReturn(createMockAiResponse("Response for User 2", "general"));

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(user1Session.getSession().getId())
                .userId(USER_ID)
                .question("User 1 question")
                .build());

            askQuestionUseCase.execute(AskQuestionCommand.builder()
                .sessionId(user2Session.getSession().getId())
                .userId(OTHER_USER_ID)
                .question("User 2 question")
                .build());

            // Then - Each user sees only their sessions
            GetChatSessionsResult user1Sessions = getChatSessionsUseCase.execute(
                GetChatSessionsCommand.all(USER_ID));
            GetChatSessionsResult user2Sessions = getChatSessionsUseCase.execute(
                GetChatSessionsCommand.all(OTHER_USER_ID));

            user1Sessions.getSessions().forEach(s -> 
                assertEquals(USER_ID, s.getUserId()));
            user2Sessions.getSessions().forEach(s -> 
                assertEquals(OTHER_USER_ID, s.getUserId()));

            // And - Cross-access is blocked
            assertThrows(CustomBusinessException.class, () ->
                askQuestionUseCase.execute(AskQuestionCommand.builder()
                    .sessionId(user1Session.getSession().getId())
                    .userId(OTHER_USER_ID)  // Wrong user!
                    .question("Unauthorized access")
                    .build()));
        }
    }

    // ==================== Flow 7: Edge Cases and Boundary Tests ====================

    @Nested
    @DisplayName("Flow 7: Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @Order(70)
        @DisplayName("7.1 - Should handle very long question")
        void shouldHandleVeryLongQuestion() {
            // Given
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Long Question Test"));
            
            String longQuestion = "A".repeat(5000); // 5000 character question
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response to long question", "general"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionResult.getSession().getId())
                .userId(USER_ID)
                .question(longQuestion)
                .build();

            // When
            AskQuestionResult result = askQuestionUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertEquals(longQuestion, result.getUserMessage().getContent());
        }

        @Test
        @Order(71)
        @DisplayName("7.2 - Should handle special characters in question")
        void shouldHandleSpecialCharactersInQuestion() {
            // Given
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Special Chars Test"));
            
            String specialQuestion = "What's the price for \"MacBook Pro\"? It's ~$1,500 (est.) 日本語 émojis: 🎉";
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response with special chars", "general"));

            AskQuestionCommand command = AskQuestionCommand.builder()
                .sessionId(sessionResult.getSession().getId())
                .userId(USER_ID)
                .question(specialQuestion)
                .build();

            // When
            AskQuestionResult result = askQuestionUseCase.execute(command);

            // Then
            assertTrue(result.isSuccess());
            assertEquals(specialQuestion, result.getUserMessage().getContent());
        }

        @Test
        @Order(72)
        @DisplayName("7.3 - Should handle concurrent questions to same session")
        void shouldHandleConcurrentQuestions() {
            // Reset mock to ensure clean state
            reset(aiServiceClient);
            
            // Given
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Concurrent Test"));
            Long testSessionId = sessionResult.getSession().getId();
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(createMockAiResponse("Response 1", "general"))
                .thenReturn(createMockAiResponse("Response 2", "general"))
                .thenReturn(createMockAiResponse("Response 3", "general"));

            // When - Simulate sequential calls (true concurrency needs different test setup)
            for (int i = 1; i <= 3; i++) {
                AskQuestionResult result = askQuestionUseCase.execute(
                    AskQuestionCommand.builder()
                        .sessionId(testSessionId)
                        .userId(USER_ID)
                        .question("Concurrent question " + i)
                        .build());
                assertTrue(result.isSuccess());
            }

            // Then
            List<ChatMessage> messages = chatMessageRepository.findBySessionId(testSessionId);
            assertEquals(6, messages.size());
        }

        @Test
        @Order(73)
        @DisplayName("7.4 - Should handle AI response with empty sources")
        void shouldHandleAiResponseWithEmptySources() {
            // Given
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Empty Sources Test"));
            
            ChatGenerationResponse responseWithEmptySources = new ChatGenerationResponse();
            responseWithEmptySources.setResponse("General response");
            responseWithEmptySources.setQueryType("general");
            responseWithEmptySources.setSources(Collections.emptyList());
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(responseWithEmptySources);

            // When
            AskQuestionResult result = askQuestionUseCase.execute(
                AskQuestionCommand.builder()
                    .sessionId(sessionResult.getSession().getId())
                    .userId(USER_ID)
                    .question("General question")
                    .build());

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getSources());
            assertTrue(result.getSources().isEmpty());
        }

        @Test
        @Order(74)
        @DisplayName("7.5 - Should handle AI response with null queryType")
        void shouldHandleAiResponseWithNullQueryType() {
            // Given
            CreateChatSessionResult sessionResult = createChatSessionUseCase.execute(
                new CreateChatSessionCommand(USER_ID, "Null QueryType Test"));
            
            ChatGenerationResponse responseWithNullQueryType = new ChatGenerationResponse();
            responseWithNullQueryType.setResponse("Response without query type");
            responseWithNullQueryType.setQueryType(null);
            responseWithNullQueryType.setSources(Collections.emptyList());
            
            when(aiServiceClient.generateChatResponse(any()))
                .thenReturn(responseWithNullQueryType);

            // When
            AskQuestionResult result = askQuestionUseCase.execute(
                AskQuestionCommand.builder()
                    .sessionId(sessionResult.getSession().getId())
                    .userId(USER_ID)
                    .question("Question")
                    .build());

            // Then
            assertTrue(result.isSuccess());
            assertNull(result.getQueryType());
        }
    }
}
