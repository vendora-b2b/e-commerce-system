package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ProductIngestRequest;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for bulk ingesting products into the AI vector database.
 * Used for initial sync or batch updates when migrating existing products.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkIngestProductsUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the bulk ingest products use case.
     *
     * @param command the command containing list of products
     * @return the result indicating success counts and any failures
     * @throws IllegalArgumentException if command is invalid
     * @throws CustomBusinessException if AI service bulk ingestion fails completely
     */
    public BulkIngestProductsResult execute(BulkIngestProductsCommand command) {
        // Step 1: Validate command
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.isEmpty()) {
            log.info("No products to ingest");
            return BulkIngestProductsResult.empty();
        }

        // Step 2: Validate each product in command
        for (IngestProductCommand product : command.getProducts()) {
            if (product.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for all products");
            }
            if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                throw new IllegalArgumentException("SKU is required for all products");
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required for all products");
            }
        }

        try {
            log.info("Starting bulk ingestion of {} products", command.getProductCount());

            // Step 3: Convert commands to AI service requests
            List<ProductIngestRequest> requests = command.getProducts().stream()
                .map(this::toProductIngestRequest)
                .collect(Collectors.toList());

            // Step 4: Call AI service bulk endpoint
            Map<String, Object> response = aiServiceClient.ingestProductsBulk(requests);

            // Step 5: Parse response
            int ingestedCount = parseIngestedCount(response);
            
            log.info("Bulk ingestion completed: {}/{} products ingested", 
                    ingestedCount, command.getProductCount());

            // Step 6: Return result
            return BulkIngestProductsResult.success(command.getProductCount(), ingestedCount);

        } catch (AiServiceException e) {
            log.error("AI service error during bulk ingestion: {}", e.getMessage());
            throw new CustomBusinessException("BULK_INGESTION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error during bulk ingestion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to bulk ingest products: {}", e.getMessage());
            throw new CustomBusinessException("BULK_INGESTION_FAILED",
                "Failed to bulk ingest products: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget bulk ingestion.
     * Use this for background sync operations.
     * Failures are logged but not thrown.
     *
     * @param command the command containing list of products
     */
    @Async
    public void executeAsync(BulkIngestProductsCommand command) {
        try {
            BulkIngestProductsResult result = execute(command);
            if (!result.isSuccess()) {
                log.warn("Async bulk ingestion failed: {}", result.getMessage());
            } else if (result.hasFailures()) {
                log.warn("Async bulk ingestion completed with {} failures", result.getFailedCount());
            } else {
                log.info("Async bulk ingestion completed: {}/{} products", 
                        result.getSuccessCount(), result.getTotalCount());
            }
        } catch (Exception e) {
            log.warn("Async bulk ingestion failed: {}", e.getMessage());
        }
    }

    /**
     * Converts IngestProductCommand to ProductIngestRequest.
     */
    private ProductIngestRequest toProductIngestRequest(IngestProductCommand command) {
        return ProductIngestRequest.builder()
            .productId(command.getProductId())
            .sku(command.getSku())
            .name(command.getName())
            .description(command.getDescription())
            .categoryName(command.getCategoryName())
            .supplierId(command.getSupplierId())
            .tags(command.getTags())
            .build();
    }

    /**
     * Parses the ingested count from AI service response.
     */
    private int parseIngestedCount(Map<String, Object> response) {
        if (response == null) {
            return 0;
        }
        Object count = response.get("ingested_count");
        if (count instanceof Number) {
            return ((Number) count).intValue();
        }
        return 0;
    }
}
