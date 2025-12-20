package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.SupplierIngestRequest;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Use case for ingesting a supplier into the AI vector database.
 * This enables semantic search for suppliers in the search bar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestSupplierUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the ingest supplier use case.
     *
     * @param command the command containing supplier data
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if required fields are missing
     * @throws CustomBusinessException if AI service ingestion fails
     */
    public IngestSupplierResult execute(IngestSupplierCommand command) {
        // Step 1: Validate required fields
        if (command.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier ID is required");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }

        try {
            // Step 2: Build AI service request
            SupplierIngestRequest request = SupplierIngestRequest.builder()
                .supplierId(command.getSupplierId())
                .name(command.getName())
                .email(command.getEmail())
                .phone(command.getPhone())
                .address(command.getAddress())
                .businessLicense(command.getBusinessLicense())
                .build();

            // Step 3: Call AI service
            Map<String, Object> response = aiServiceClient.ingestSupplier(request);

            log.info("Ingested supplier {} ({}) into AI service", 
                    command.getName(), command.getSupplierId());

            // Step 4: Return success
            return IngestSupplierResult.success(command.getSupplierId());

        } catch (AiServiceException e) {
            log.error("AI service error ingesting supplier {}: {}", 
                    command.getSupplierId(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error ingesting supplier {}: {}", 
                    command.getSupplierId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to ingest supplier {}: {}", 
                    command.getSupplierId(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "Failed to ingest supplier: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget ingestion.
     * Use this when ingestion should not block the main operation.
     * Failures are logged but not thrown.
     *
     * @param command the command containing supplier data
     */
    @Async
    public void executeAsync(IngestSupplierCommand command) {
        try {
            log.info("[ASYNC] Starting supplier ingestion for: {} (ID: {})", 
                    command.getName(), command.getSupplierId());
            IngestSupplierResult result = execute(command);
            if (!result.isSuccess()) {
                log.error("[ASYNC] Supplier ingestion failed for {}: {}", 
                        command.getSupplierId(), result.getMessage());
            } else {
                log.info("[ASYNC] Supplier ingestion succeeded for {} (ID: {})", 
                        command.getName(), command.getSupplierId());
            }
        } catch (Exception e) {
            log.error("[ASYNC] Supplier ingestion exception for {}: {}", 
                    command.getSupplierId(), e.getMessage(), e);
        }
    }
}
