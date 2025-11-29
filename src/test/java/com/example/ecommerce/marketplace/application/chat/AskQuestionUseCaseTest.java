package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatMessageRepository;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationRequest;
import com.example.ecommerce.marketplace.service.ai.ChatGenerationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AskQuestionUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AskQuestionUseCase Unit Tests")
class AskQuestionUseCaseTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private AskQuestionUseCase useCase;

    private ChatSession testSession;
    private AskQuestionCommand validCommand;
    private ChatGenerationResponse aiResponse;

    @BeforeEach
    void setUp() {
        testSession = new ChatSession(
            1L, "chat_token123", 100L, "Test Session",
            new ArrayList<>(), LocalDateTime.now(),
            LocalDateTime.now(), LocalDateTime.now(), true
        );

        validCommand = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(100L)
            .question("What products do you recommend?")
            .userType("retailer")
            .userName("John Doe")
            .loyaltyTier("GOLD")
            .build();

        aiResponse = new ChatGenerationResponse();
        aiResponse.setResponse("Based on your profile, I recommend checking out our electronics section.");
        aiResponse.setQueryType("recommendation");
        
        // Create proper source format: List<Map<String, Object>>
        List<Map<String, Object>> sources = new ArrayList<>();
        Map<String, Object> source1 = new HashMap<>();
        source1.put("type", "products");
        sources.add(source1);
        Map<String, Object> source2 = new HashMap<>();
        source2.put("type", "categories");
        sources.add(source2);
        aiResponse.setSources(sources);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully process question and return AI response")
    void testExecute_Success() {
        // Given
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime()); // Simulate ID generation
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt()))
            .thenReturn(Collections.emptyList());
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenReturn(aiResponse);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        AskQuestionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getUserMessage());
        assertNotNull(result.getAssistantMessage());
        assertEquals(ChatRole.USER, result.getUserMessage().getRole());
        assertEquals(ChatRole.ASSISTANT, result.getAssistantMessage().getRole());
        assertEquals("recommendation", result.getQueryType());
        
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
        verify(aiServiceClient).generateChatResponse(any(ChatGenerationRequest.class));
        verify(chatSessionRepository).save(testSession);
    }

    @Test
    @DisplayName("Should include conversation history in AI request")
    void testExecute_IncludesHistory() {
        // Given
        List<ChatMessage> history = List.of(
            new ChatMessage(1L, 1L, ChatRole.USER, "Hello", LocalDateTime.now().minusMinutes(5)),
            new ChatMessage(2L, 1L, ChatRole.ASSISTANT, "Hi there!", LocalDateTime.now().minusMinutes(4))
        );
        
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime());
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt())).thenReturn(history);
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenReturn(aiResponse);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        AskQuestionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        verify(chatMessageRepository).findRecentBySessionId(eq(1L), anyInt());
    }

    @Test
    @DisplayName("Should auto-generate title for new chat")
    void testExecute_AutoGeneratesTitle() {
        // Given
        ChatSession newChatSession = new ChatSession(
            1L, "chat_token123", 100L, "New Chat",
            new ArrayList<>(), LocalDateTime.now(),
            LocalDateTime.now(), LocalDateTime.now(), true
        );
        
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(newChatSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime());
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt()))
            .thenReturn(Collections.emptyList());
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenReturn(aiResponse);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(newChatSession);

        // When
        AskQuestionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        verify(chatSessionRepository).save(any(ChatSession.class));
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when sessionId is null")
    void testExecute_NullSessionId_ThrowsException() {
        // Given
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(null)
            .userId(100L)
            .question("Test question")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Session ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void testExecute_NullUserId_ThrowsException() {
        // Given
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(null)
            .question("Test question")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when question is null")
    void testExecute_NullQuestion_ThrowsException() {
        // Given
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(100L)
            .question(null)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Question cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when question is empty")
    void testExecute_EmptyQuestion_ThrowsException() {
        // Given
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(100L)
            .question("   ")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Question cannot be empty", exception.getMessage());
    }

    // ===== Business Logic Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when session not found")
    void testExecute_SessionNotFound_ThrowsException() {
        // Given
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("SESSION_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when user doesn't own session")
    void testExecute_AccessDenied_ThrowsException() {
        // Given
        ChatSession otherUserSession = new ChatSession(
            1L, "chat_token123", 999L, "Other User Session", // Different userId
            new ArrayList<>(), LocalDateTime.now(),
            LocalDateTime.now(), LocalDateTime.now(), true
        );
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(otherUserSession));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when session is inactive")
    void testExecute_InactiveSession_ThrowsException() {
        // Given
        ChatSession inactiveSession = new ChatSession(
            1L, "chat_token123", 100L, "Inactive Session",
            new ArrayList<>(), LocalDateTime.now(),
            LocalDateTime.now(), LocalDateTime.now(), false // Inactive
        );
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(inactiveSession));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("SESSION_INACTIVE", exception.getErrorCode());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime());
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt()))
            .thenReturn(Collections.emptyList());
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenThrow(new AiServiceException("AI service unavailable"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("AI_SERVICE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("AI service is temporarily unavailable"));
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle very long question")
    void testExecute_VeryLongQuestion() {
        // Given
        String longQuestion = "A".repeat(5000);
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(100L)
            .question(longQuestion)
            .build();
        
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime());
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt()))
            .thenReturn(Collections.emptyList());
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenReturn(aiResponse);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        AskQuestionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle unicode characters in question")
    void testExecute_UnicodeQuestion() {
        // Given
        AskQuestionCommand command = AskQuestionCommand.builder()
            .sessionId(1L)
            .userId(100L)
            .question("你好，推荐什么产品？🤔")
            .build();
        
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> {
                ChatMessage msg = invocation.getArgument(0);
                msg.setId(System.nanoTime());
                return msg;
            });
        when(chatMessageRepository.findRecentBySessionId(eq(1L), anyInt()))
            .thenReturn(Collections.emptyList());
        when(aiServiceClient.generateChatResponse(any(ChatGenerationRequest.class)))
            .thenReturn(aiResponse);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        AskQuestionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }
}
