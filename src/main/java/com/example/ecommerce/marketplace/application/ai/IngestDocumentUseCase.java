package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Use case for ingesting a knowledge document into the AI vector database.
 * This enables semantic search for tax info, contracts, policies, FAQs, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestDocumentUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the ingest document use case.
     *
     * @param command the command containing document data
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if required fields are missing
     * @throws CustomBusinessException if AI service ingestion fails
     */
    public IngestDocumentResult execute(IngestDocumentCommand command) {
        // Step 1: Validate required fields
        if (command.getDocumentId() == null || command.getDocumentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID is required");
        }
        if (command.getTitle() == null || command.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (command.getContent() == null || command.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }
        if (command.getDocumentType() == null || command.getDocumentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Document type is required");
        }

        try {
            // Step 2: Build document request
            Map<String, Object> documentRequest = new HashMap<>();
            documentRequest.put("document_id", command.getDocumentId());
            documentRequest.put("title", command.getTitle());
            documentRequest.put("content", command.getContent());
            documentRequest.put("document_type", command.getDocumentType());
            
            if (command.getCategory() != null) {
                documentRequest.put("category", command.getCategory());
            }
            if (command.getSource() != null) {
                documentRequest.put("source", command.getSource());
            }

            // Step 3: Call AI service
            Map<String, Object> response = aiServiceClient.ingestDocument(documentRequest);

            log.info("Ingested document {} ({}) into AI service", 
                    command.getDocumentId(), command.getDocumentType());

            // Step 4: Return success
            return IngestDocumentResult.success(command.getDocumentId(), command.getDocumentType());

        } catch (AiServiceException e) {
            log.error("AI service error ingesting document {}: {}", command.getDocumentId(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error ingesting document {}: {}", command.getDocumentId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to ingest document {}: {}", command.getDocumentId(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "Failed to ingest document: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget ingestion.
     * Failures are logged but not thrown.
     *
     * @param command the command containing document data
     */
    @Async
    public void executeAsync(IngestDocumentCommand command) {
        try {
            IngestDocumentResult result = execute(command);
            if (!result.isSuccess()) {
                log.warn("Async document ingestion failed for {}: {}", 
                        command.getDocumentId(), result.getMessage());
            }
        } catch (Exception e) {
            log.warn("Async document ingestion failed for {}: {}", 
                    command.getDocumentId(), e.getMessage());
        }
    }
}
