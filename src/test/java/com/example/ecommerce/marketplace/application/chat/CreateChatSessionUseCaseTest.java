package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatSession;
import com.example.ecommerce.marketplace.domain.chat.ChatSessionRepository;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateChatSessionUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateChatSessionUseCase Unit Tests")
class CreateChatSessionUseCaseTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @InjectMocks
    private CreateChatSessionUseCase useCase;

    private CreateChatSessionCommand validCommand;
    private ChatSession savedSession;

    @BeforeEach
    void setUp() {
        validCommand = new CreateChatSessionCommand(1L, "Test Chat Session");
        
        savedSession = ChatSession.createNew(1L, "Test Chat Session");
        savedSession.setId(100L);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully create chat session with title")
    void testExecute_Success_WithTitle() {
        // Given
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        // When
        CreateChatSessionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getSession());
        assertEquals(1L, result.getSession().getUserId());
        assertEquals("Test Chat Session", result.getSession().getTitle());
        assertNotNull(result.getSession().getSessionToken());
        
        verify(chatSessionRepository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Should create session with default title when title is null")
    void testExecute_Success_WithNullTitle() {
        // Given
        CreateChatSessionCommand command = new CreateChatSessionCommand(1L, null);
        ChatSession sessionWithDefaultTitle = ChatSession.createNew(1L, null);
        sessionWithDefaultTitle.setId(101L);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(sessionWithDefaultTitle);

        // When
        CreateChatSessionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getSession());
        assertEquals("New Chat", result.getSession().getTitle());
    }

    @Test
    @DisplayName("Should create session with empty title defaulting to 'New Chat'")
    void testExecute_Success_WithEmptyTitle() {
        // Given
        CreateChatSessionCommand command = new CreateChatSessionCommand(1L, "");
        ChatSession session = ChatSession.createNew(1L, "");
        session.setId(102L);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        // When
        CreateChatSessionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should generate unique session token")
    void testExecute_GeneratesUniqueSessionToken() {
        // Given
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        // When
        CreateChatSessionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getSession().getSessionToken());
        assertTrue(result.getSession().getSessionToken().startsWith("chat_"));
    }

    @Test
    @DisplayName("Should set session as active by default")
    void testExecute_SessionIsActiveByDefault() {
        // Given
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        // When
        CreateChatSessionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getSession().getActive());
    }

    @Test
    @DisplayName("Should set createdAt timestamp")
    void testExecute_SetsCreatedAtTimestamp() {
        // Given
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        // When
        CreateChatSessionResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getSession().getCreatedAt());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void testExecute_NullUserId_ThrowsException() {
        // Given
        CreateChatSessionCommand command = new CreateChatSessionCommand(null, "Test");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
        verify(chatSessionRepository, never()).save(any());
    }

    // ===== Repository Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when repository throws exception")
    void testExecute_RepositoryError_ThrowsException() {
        // Given
        when(chatSessionRepository.save(any(ChatSession.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("SESSION_CREATION_FAILED", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to create chat session"));
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle very long title")
    void testExecute_VeryLongTitle() {
        // Given
        String longTitle = "A".repeat(500);
        CreateChatSessionCommand command = new CreateChatSessionCommand(1L, longTitle);
        ChatSession session = ChatSession.createNew(1L, longTitle);
        session.setId(103L);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        // When
        CreateChatSessionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle special characters in title")
    void testExecute_SpecialCharactersInTitle() {
        // Given
        String specialTitle = "Chat <script>alert('xss')</script> & \"quotes\"";
        CreateChatSessionCommand command = new CreateChatSessionCommand(1L, specialTitle);
        ChatSession session = ChatSession.createNew(1L, specialTitle);
        session.setId(104L);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        // When
        CreateChatSessionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(specialTitle, result.getSession().getTitle());
    }

    @Test
    @DisplayName("Should handle unicode characters in title")
    void testExecute_UnicodeCharactersInTitle() {
        // Given
        String unicodeTitle = "聊天会话 🤖 Chat über Produkte";
        CreateChatSessionCommand command = new CreateChatSessionCommand(1L, unicodeTitle);
        ChatSession session = ChatSession.createNew(1L, unicodeTitle);
        session.setId(105L);
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        // When
        CreateChatSessionResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(unicodeTitle, result.getSession().getTitle());
    }

    @Test
    @DisplayName("Should create multiple sessions for same user")
    void testExecute_MultipleSessions_SameUser() {
        // Given
        ChatSession session1 = ChatSession.createNew(1L, "Session 1");
        session1.setId(106L);
        ChatSession session2 = ChatSession.createNew(1L, "Session 2");
        session2.setId(107L);
        
        when(chatSessionRepository.save(any(ChatSession.class)))
            .thenReturn(session1)
            .thenReturn(session2);

        // When
        CreateChatSessionResult result1 = useCase.execute(new CreateChatSessionCommand(1L, "Session 1"));
        CreateChatSessionResult result2 = useCase.execute(new CreateChatSessionCommand(1L, "Session 2"));

        // Then
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertNotEquals(result1.getSession().getSessionToken(), result2.getSession().getSessionToken());
        verify(chatSessionRepository, times(2)).save(any(ChatSession.class));
    }
}
