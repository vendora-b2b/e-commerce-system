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
 * Use case for deleting a supplier from the AI vector database.
 * This should be called when a supplier is deleted or deactivated from the system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteSupplierFromAiUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the delete supplier from AI use case.
     *
     * @param command the command containing supplier ID
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if supplier ID is missing
     * @throws CustomBusinessException if AI service deletion fails
     */
    public DeleteSupplierFromAiResult execute(DeleteSupplierFromAiCommand command) {
        // Step 1: Validate required fields
        if (command.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier ID is required");
        }

        try {
            // Step 2: Call AI service to delete supplier
            Map<String, Object> response = aiServiceClient.deleteSupplier(command.getSupplierId());

            log.info("Deleted supplier {} from AI service", command.getSupplierId());

            // Step 3: Return success
            return DeleteSupplierFromAiResult.success(command.getSupplierId());

        } catch (AiServiceException e) {
            log.error("AI service error deleting supplier {}: {}", 
                    command.getSupplierId(), e.getMessage());
            throw new CustomBusinessException("DELETION_FAILED",
                "AI service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error deleting supplier {}: {}", 
                    command.getSupplierId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete supplier {} from AI service: {}", 
                    command.getSupplierId(), e.getMessage());
            throw new CustomBusinessException("DELETION_FAILED",
                "Failed to delete supplier from AI: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous version for fire-and-forget deletion.
     * Failures are logged but not thrown.
     *
     * @param command the command containing supplier ID
     */
    @Async
    public void executeAsync(DeleteSupplierFromAiCommand command) {
        try {
            DeleteSupplierFromAiResult result = execute(command);
            if (!result.isSuccess()) {
                log.warn("Async supplier deletion from AI failed for {}: {}", 
                        command.getSupplierId(), result.getMessage());
            }
        } catch (Exception e) {
            log.warn("Async supplier deletion from AI failed for {}: {}", 
                    command.getSupplierId(), e.getMessage());
        }
    }
}
