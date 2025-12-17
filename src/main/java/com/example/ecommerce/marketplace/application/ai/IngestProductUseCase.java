package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ProductIngestRequest;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Use case for ingesting a product into the AI vector database.
 * This enables semantic search and recommendations for the product.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestProductUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the ingest product use case.
     *
     * @param command the command containing product data
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if required fields are missing
     * @throws CustomBusinessException if AI service ingestion fails
     */
    public IngestProductResult execute(IngestProductCommand command) {
        // Step 1: Validate required fields
        if (command.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (command.getSku() == null || command.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        try {
            // Step 2: Build AI service request
            ProductIngestRequest request = ProductIngestRequest.builder()
                .productId(command.getProductId())
                .sku(command.getSku())
                .name(command.getName())
                .description(command.getDescription())
                .category(command.getCategoryName())  // Use .category() - Python expects "category" field
                .supplierId(command.getSupplierId())
                .tags(command.getTags())
                .build();

            // Step 3: Call AI service
            Map<String, Object> response = aiServiceClient.ingestProduct(request);

            log.info("Ingested product {} ({}) into AI service", command.getSku(), command.getProductId());

            // Step 4: Return success
            return IngestProductResult.success(command.getProductId(), command.getSku());

        } catch (AiServiceException e) {
            log.error("AI service error ingesting product {}: {}", command.getSku(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error ingesting product {}: {}", command.getSku(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to ingest product {}: {}", command.getSku(), e.getMessage());
            throw new CustomBusinessException("INGESTION_FAILED",
                "Failed to ingest product: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget ingestion.
     * Use this when ingestion should not block the main operation.
     * Failures are logged but not thrown.
     *
     * @param command the command containing product data
     */
    @Async
    public void executeAsync(IngestProductCommand command) {
        try {
            log.info("[ASYNC] Starting product ingestion for SKU: {} (ID: {})", 
                    command.getSku(), command.getProductId());
            IngestProductResult result = execute(command);
            if (!result.isSuccess()) {
                log.error("[ASYNC] Product ingestion failed for {} ({}): {}", 
                        command.getSku(), command.getProductId(), result.getMessage());
            } else {
                log.info("[ASYNC] Product ingestion succeeded for {} ({})", 
                        command.getSku(), command.getProductId());
            }
        } catch (Exception e) {
            log.error("[ASYNC] Product ingestion exception for {} ({}): {}", 
                    command.getSku(), command.getProductId(), e.getMessage(), e);
        }
    }
}
