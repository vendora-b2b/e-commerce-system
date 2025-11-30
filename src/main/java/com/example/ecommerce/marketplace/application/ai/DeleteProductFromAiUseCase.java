package com.example.ecommerce.marketplace.application.ai;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Use case for deleting a product from the AI vector database.
 * This should be called when a product is deleted from the main system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteProductFromAiUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the delete product from AI use case.
     *
     * @param command the command containing product ID
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if product ID is missing
     * @throws CustomBusinessException if AI service deletion fails
     */
    public DeleteProductFromAiResult execute(DeleteProductFromAiCommand command) {
        // Step 1: Validate required fields
        if (command.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        try {
            // Step 2: Call AI service to delete product
            Map<String, Object> response = aiServiceClient.deleteProduct(command.getProductId());

            log.info("Deleted product {} from AI service", command.getProductId());

            // Step 3: Return success
            return DeleteProductFromAiResult.success(command.getProductId());

        } catch (AiServiceException e) {
            log.error("AI service error deleting product {}: {}", command.getProductId(), e.getMessage());
            throw new CustomBusinessException("DELETION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error deleting product {}: {}", command.getProductId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete product {} from AI service: {}", 
                    command.getProductId(), e.getMessage());
            throw new CustomBusinessException("DELETION_FAILED",
                "Failed to delete product from AI: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget deletion.
     * Failures are logged but not thrown.
     *
     * @param command the command containing product ID
     */
    @Async
    public void executeAsync(DeleteProductFromAiCommand command) {
        try {
            DeleteProductFromAiResult result = execute(command);
            if (!result.isSuccess()) {
                log.warn("Async product deletion from AI failed for {}: {}", 
                        command.getProductId(), result.getMessage());
            }
        } catch (Exception e) {
            log.warn("Async product deletion from AI failed for {}: {}", 
                    command.getProductId(), e.getMessage());
        }
    }
}
