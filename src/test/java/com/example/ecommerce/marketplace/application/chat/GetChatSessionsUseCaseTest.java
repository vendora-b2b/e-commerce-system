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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetChatSessionsUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetChatSessionsUseCase Unit Tests")
class GetChatSessionsUseCaseTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @InjectMocks
    private GetChatSessionsUseCase useCase;

    private List<ChatSession> testSessions;

    @BeforeEach
    void setUp() {
        ChatSession session1 = new ChatSession();
        session1.setId(1L);
        session1.setUserId(100L);
        session1.setTitle("Session 1");
        session1.setActive(true);
        session1.setCreatedAt(LocalDateTime.now().minusDays(1));

        ChatSession session2 = new ChatSession();
        session2.setId(2L);
        session2.setUserId(100L);
        session2.setTitle("Session 2");
        session2.setActive(true);
        session2.setCreatedAt(LocalDateTime.now());

        testSessions = Arrays.asList(session1, session2);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully get all sessions for user")
    void testExecute_GetAllSessions_Success() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L)).thenReturn(testSessions);

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSessions().size());
        assertEquals(2, result.getCount());
        verify(chatSessionRepository).findByUserIdOrderByLastMessageAtDesc(100L);
    }

    @Test
    @DisplayName("Should successfully get active sessions only")
    void testExecute_GetActiveSessions_Success() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.activeOnly(100L);
        when(chatSessionRepository.findByUserIdAndActiveTrue(100L)).thenReturn(testSessions);

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSessions().size());
        verify(chatSessionRepository).findByUserIdAndActiveTrue(100L);
    }

    @Test
    @DisplayName("Should return empty list when user has no sessions")
    void testExecute_NoSessions_ReturnsEmptyList() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L)).thenReturn(Collections.emptyList());

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getSessions().isEmpty());
        assertEquals(0, result.getCount());
    }

    @Test
    @DisplayName("Should return empty list when user has no active sessions")
    void testExecute_NoActiveSessions_ReturnsEmptyList() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.activeOnly(100L);
        when(chatSessionRepository.findByUserIdAndActiveTrue(100L)).thenReturn(Collections.emptyList());

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getSessions().isEmpty());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void testExecute_NullUserId_ThrowsException() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("User ID is required", exception.getMessage());
    }

    // ===== Repository Interaction Tests =====

    @Test
    @DisplayName("Should call correct repository method for all sessions")
    void testExecute_AllSessions_CallsCorrectMethod() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L)).thenReturn(testSessions);

        // When
        useCase.execute(command);

        // Then
        verify(chatSessionRepository).findByUserIdOrderByLastMessageAtDesc(100L);
        verify(chatSessionRepository, never()).findByUserIdAndActiveTrue(anyLong());
    }

    @Test
    @DisplayName("Should call correct repository method for active sessions")
    void testExecute_ActiveSessions_CallsCorrectMethod() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.activeOnly(100L);
        when(chatSessionRepository.findByUserIdAndActiveTrue(100L)).thenReturn(testSessions);

        // When
        useCase.execute(command);

        // Then
        verify(chatSessionRepository).findByUserIdAndActiveTrue(100L);
        verify(chatSessionRepository, never()).findByUserIdOrderByLastMessageAtDesc(anyLong());
    }

    @Test
    @DisplayName("Should not call repository if userId is null")
    void testExecute_NullUserId_DoesNotCallRepository() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verifyNoInteractions(chatSessionRepository);
    }

    // ===== Result Tests =====

    @Test
    @DisplayName("Should return success message in result")
    void testExecute_ReturnsSuccessMessage() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L)).thenReturn(testSessions);

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("Should preserve session order in result")
    void testExecute_PreservesSessionOrder() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L)).thenReturn(testSessions);

        // When
        GetChatSessionsResult result = useCase.execute(command);

        // Then
        List<ChatSession> sessions = result.getSessions();
        assertEquals(1L, sessions.get(0).getId());
        assertEquals(2L, sessions.get(1).getId());
    }

    // ===== Error Handling Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException on repository error")
    void testExecute_RepositoryError_ThrowsException() {
        // Given
        GetChatSessionsCommand command = GetChatSessionsCommand.all(100L);
        when(chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(100L))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(command)
        );
        assertEquals("SESSION_QUERY_FAILED", exception.getErrorCode());
    }
}
