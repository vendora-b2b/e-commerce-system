package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IngestDocumentUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IngestDocumentUseCase Unit Tests")
class IngestDocumentUseCaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private IngestDocumentUseCase useCase;

    private IngestDocumentCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = IngestDocumentCommand.builder()
            .documentId("DOC_001")
            .title("Return Policy")
            .content("Our return policy allows returns within 30 days of purchase...")
            .documentType("policy")
            .category("customer_service")
            .source("internal")
            .build();
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully ingest document with all fields")
    void testExecute_Success() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("DOC_001", result.getDocumentId());
        verify(aiServiceClient).ingestDocument(anyMap());
    }

    @Test
    @DisplayName("Should send correct data to AI service")
    @SuppressWarnings("unchecked")
    void testExecute_SendsCorrectData() {
        // Given
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        useCase.execute(validCommand);

        // Then
        verify(aiServiceClient).ingestDocument(captor.capture());
        Map<String, Object> captured = captor.getValue();
        assertEquals("DOC_001", captured.get("document_id"));
        assertEquals("Return Policy", captured.get("title"));
        assertTrue(((String) captured.get("content")).contains("return policy"));
        assertEquals("policy", captured.get("document_type"));
    }

    @Test
    @DisplayName("Should successfully ingest document without optional category")
    void testExecute_WithoutCategory_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_002")
            .title("Privacy Policy")
            .content("This is our privacy policy content...")
            .documentType("legal")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should successfully ingest document without source")
    void testExecute_WithoutSource_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_003")
            .title("FAQ")
            .content("Frequently asked questions...")
            .documentType("faq")
            .category("help")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Validation Error Tests =====

    @Test
    @DisplayName("Should throw IllegalArgumentException when documentId is null")
    void testExecute_NullDocumentId_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId(null)
            .title("Test Title")
            .content("Test Content")
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Document ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when documentId is empty")
    void testExecute_EmptyDocumentId_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("   ")
            .title("Test Title")
            .content("Test Content")
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Document ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when title is null")
    void testExecute_NullTitle_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title(null)
            .content("Test Content")
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Title is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when title is empty")
    void testExecute_EmptyTitle_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title("")
            .content("Test Content")
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Title is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when content is null")
    void testExecute_NullContent_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title("Test Title")
            .content(null)
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Content is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when content is empty")
    void testExecute_EmptyContent_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title("Test Title")
            .content("   ")
            .documentType("policy")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Content is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when documentType is null")
    void testExecute_NullDocumentType_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title("Test Title")
            .content("Test Content")
            .documentType(null)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Document type is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when documentType is empty")
    void testExecute_EmptyDocumentType_ThrowsException() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_123")
            .title("Test Title")
            .content("Test Content")
            .documentType("  ")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(command)
        );
        assertEquals("Document type is required", exception.getMessage());
    }

    // ===== AI Service Error Tests =====

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service fails")
    void testExecute_AiServiceError_ThrowsException() {
        // Given
        when(aiServiceClient.ingestDocument(anyMap()))
            .thenThrow(new AiServiceException("Service unavailable"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("AI service is temporarily unavailable"));
    }

    @Test
    @DisplayName("Should throw CustomBusinessException when AI service returns error")
    void testExecute_AiServiceReturnsError_ThrowsException() {
        // Given
        when(aiServiceClient.ingestDocument(anyMap()))
            .thenThrow(new AiServiceException("Invalid document format"));

        // When & Then
        CustomBusinessException exception = assertThrows(
            CustomBusinessException.class,
            () -> useCase.execute(validCommand)
        );
        assertEquals("INGESTION_FAILED", exception.getErrorCode());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle very long content")
    void testExecute_LongContent_Success() {
        // Given
        String longContent = "Long content paragraph. ".repeat(1000);
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_LONG")
            .title("Long Document")
            .content(longContent)
            .documentType("article")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle unicode characters in document")
    void testExecute_UnicodeCharacters_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_INTL")
            .title("日本語タイトル")
            .content("日本語の内容です。中文内容。한국어 내용.")
            .documentType("international")
            .category("international")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle HTML content")
    void testExecute_HtmlContent_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_HTML")
            .title("HTML Document")
            .content("<h1>Title</h1><p>Paragraph with <strong>bold</strong> text.</p>")
            .documentType("html")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle markdown content")
    void testExecute_MarkdownContent_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_MD")
            .title("Markdown Document")
            .content("# Heading\n\n## Subheading\n\n- Item 1\n- Item 2\n\n**Bold** and *italic*")
            .documentType("markdown")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle special characters in title")
    void testExecute_SpecialCharactersInTitle_Success() {
        // Given
        IngestDocumentCommand command = IngestDocumentCommand.builder()
            .documentId("DOC_SPECIAL")
            .title("Title with \"quotes\" and 'apostrophes' & ampersand")
            .content("Test content")
            .documentType("test")
            .build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        // When
        IngestDocumentResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle various document types")
    void testExecute_VariousDocumentTypes() {
        // Given
        String[] documentTypes = {"policy", "faq", "contract", "tax", "legal", "guide"};
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        when(aiServiceClient.ingestDocument(anyMap())).thenReturn(response);

        for (String docType : documentTypes) {
            IngestDocumentCommand command = IngestDocumentCommand.builder()
                .documentId("DOC_" + docType.toUpperCase())
                .title("Document: " + docType)
                .content("Content for " + docType + " document")
                .documentType(docType)
                .build();

            // When
            IngestDocumentResult result = useCase.execute(command);

            // Then
            assertTrue(result.isSuccess(), "Failed for documentType: " + docType);
        }
    }
}
