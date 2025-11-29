package com.example.ecommerce.marketplace.application.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import com.example.ecommerce.marketplace.domain.chat.ChatMessageRepository;
import com.example.ecommerce.marketplace.domain.chat.ChatRole;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetChatMessagesUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetChatMessagesUseCase Unit Tests")
class GetChatMessagesUseCaseTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private GetChatMessagesUseCase useCase;

    private ChatSession testSession;
    private List<ChatMessage> testMessages;

    @BeforeEach
    void setUp() {
        testSession = new ChatSession();
        testSession.setId(1L);
        testSession.setUserId(100L);
        testSession.setTitle("Test Session");
        testSession.setCreatedAt(LocalDateTime.now());

        ChatMessage msg1 = new ChatMessage();
        msg1.setId(1L);
        msg1.setSessionId(1L);
        msg1.setRole(ChatRole.USER);
        msg1.setContent("Hello");
        msg1.setCreatedAt(LocalDateTime.now());

        ChatMessage msg2 = new ChatMessage();
        msg2.setId(2L);
        msg2.setSessionId(1L);
        msg2.setRole(ChatRole.ASSISTANT);
        msg2.setContent("Hi there!");
        msg2.setCreatedAt(LocalDateTime.now());

        testMessages = Arrays.asList(msg1, msg2);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully get all messages from session")
    void testExecute_GetAllMessages_Success() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 100L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getMessages().size());
        assertEquals(1L, result.getSessionId());
        verify(chatMessageRepository).findBySessionId(1L);
    }

    @Test
    @DisplayName("Should successfully get recent messages with limit")
    void testExecute_GetRecentMessages_Success() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.recent(1L, 100L, 10);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findRecentBySessionId(1L, 10)).thenReturn(testMessages);

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getCount());
        verify(chatMessageRepository).findRecentBySessionId(1L, 10);
    }

    @Test
    @DisplayName("Should return empty list when session has no messages")
    void testExecute_EmptySession_ReturnsEmptyList() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 100L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(Collections.emptyList());

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getMessages().isEmpty());
        assertEquals(0, result.getCount());
    }

    @Test
    @DisplayName("Should successfully get messages with limit of 1")
    void testExecute_LimitOfOne_Success() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.recent(1L, 100L, 1);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findRecentBySessionId(1L, 1)).thenReturn(testMessages.subList(0, 1));

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getCount());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when sessionId is null")
    void testExecute_NullSessionId_ThrowsException() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(null, 100L);

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
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
    }

    // ===== Business Rule Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when session not found")
    void testExecute_SessionNotFound_ThrowsException() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(999L, 100L);
        when(chatSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("SESSION_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when user does not own session")
    void testExecute_AccessDenied_ThrowsException() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 999L); // Different user
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession)); // Session owned by user 100

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
    }

    // ===== Repository Interaction Tests =====

    @Test
    @DisplayName("Should verify session exists before fetching messages")
    void testExecute_VerifiesSessionFirst() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 100L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        useCase.execute(command);

        // Then
        verify(chatSessionRepository).findById(1L);
        verify(chatMessageRepository).findBySessionId(1L);
    }

    @Test
    @DisplayName("Should not fetch messages if session not found")
    void testExecute_DoesNotFetchMessagesIfSessionNotFound() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(999L, 100L);
        when(chatSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomBusinessException.class, () -> useCase.execute(command));
        verify(chatMessageRepository, never()).findBySessionId(anyLong());
        verify(chatMessageRepository, never()).findRecentBySessionId(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should not fetch messages if access denied")
    void testExecute_DoesNotFetchMessagesIfAccessDenied() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 999L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // When & Then
        assertThrows(CustomBusinessException.class, () -> useCase.execute(command));
        verify(chatMessageRepository, never()).findBySessionId(anyLong());
        verify(chatMessageRepository, never()).findRecentBySessionId(anyLong(), anyInt());
    }

    // ===== Result Tests =====

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 100L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("Should preserve message order in result")
    void testExecute_PreservesMessageOrder() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.all(1L, 100L);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        GetChatMessagesResult result = useCase.execute(command);

        // Then
        List<ChatMessage> messages = result.getMessages();
        assertEquals(ChatRole.USER, messages.get(0).getRole());
        assertEquals(ChatRole.ASSISTANT, messages.get(1).getRole());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should use findBySessionId when limit is null")
    void testExecute_NullLimit_UsesFindBySessionId() {
        // Given - use constructor with null limit
        GetChatMessagesCommand command = new GetChatMessagesCommand(1L, 100L, null, null);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        useCase.execute(command);

        // Then
        verify(chatMessageRepository).findBySessionId(1L);
        verify(chatMessageRepository, never()).findRecentBySessionId(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should use findBySessionId when limit is zero")
    void testExecute_ZeroLimit_UsesFindBySessionId() {
        // Given
        GetChatMessagesCommand command = new GetChatMessagesCommand(1L, 100L, 0, null);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findBySessionId(1L)).thenReturn(testMessages);

        // When
        useCase.execute(command);

        // Then
        verify(chatMessageRepository).findBySessionId(1L);
        verify(chatMessageRepository, never()).findRecentBySessionId(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should use findRecentBySessionId when limit is positive")
    void testExecute_PositiveLimit_UsesFindRecentBySessionId() {
        // Given
        GetChatMessagesCommand command = GetChatMessagesCommand.recent(1L, 100L, 5);
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.findRecentBySessionId(1L, 5)).thenReturn(testMessages);

        // When
        useCase.execute(command);

        // Then
        verify(chatMessageRepository).findRecentBySessionId(1L, 5);
        verify(chatMessageRepository, never()).findBySessionId(anyLong());
    }
}
