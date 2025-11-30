package com.example.ecommerce.marketplace.application.analytics;

import com.example.ecommerce.marketplace.domain.analytics.UserInteraction;
import com.example.ecommerce.marketplace.domain.analytics.UserInteractionRepository;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.TrackInteractionRequest;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for tracking user interactions with products.
 * Saves interaction locally and forwards to AI service for recommendation learning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrackUserInteractionUseCase {

    private final UserInteractionRepository userInteractionRepository;
    private final ProductRepository productRepository;
    private final AiServiceClient aiServiceClient;

    /**
     * Executes the track user interaction use case.
     *
     * @param command the command containing interaction data
     * @return the result indicating success or failure
     * @throws IllegalArgumentException if required fields are missing
     * @throws CustomBusinessException if product not found or tracking fails
     */
    @Transactional
    public TrackUserInteractionResult execute(TrackUserInteractionCommand command) {
        // Step 1: Validate required fields
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (command.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (command.getInteractionType() == null) {
            throw new IllegalArgumentException("Interaction type is required");
        }

        // Step 2: Verify product exists
        if (!productRepository.existsById(command.getProductId())) {
            throw new CustomBusinessException("PRODUCT_NOT_FOUND", "Product not found");
        }

        try {
            // Step 3: Create and save interaction locally
            UserInteraction interaction = UserInteraction.create(
                command.getUserId(),
                command.getProductId(),
                command.getVariantId(),
                command.getInteractionType(),
                command.getSessionId(),
                command.getMetadata()
            );

            UserInteraction savedInteraction = userInteractionRepository.save(interaction);

            // Step 4: Forward to AI service asynchronously for recommendation learning
            forwardToAiServiceAsync(command);

            log.debug("Tracked {} interaction for user {} on product {}", 
                    command.getInteractionType(), command.getUserId(), command.getProductId());

            // Step 5: Return success
            return TrackUserInteractionResult.success(
                savedInteraction.getId(), 
                savedInteraction.getInteractionType()
            );

        } catch (CustomBusinessException e) {
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error tracking interaction: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to track interaction: {}", e.getMessage());
            throw new CustomBusinessException("TRACKING_FAILED",
                "Failed to track interaction: " + e.getMessage(), e);
        }
    }

    /**
     * Forwards interaction to AI service asynchronously.
     * Failures are logged but don't affect the main operation.
     */
    @Async
    protected void forwardToAiServiceAsync(TrackUserInteractionCommand command) {
        try {
            TrackInteractionRequest request = TrackInteractionRequest.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .variantId(command.getVariantId())
                .action(command.getInteractionType().name())
                .build();

            aiServiceClient.trackInteraction(request);
            
            log.debug("Forwarded interaction to AI service: user={}, product={}, type={}", 
                    command.getUserId(), command.getProductId(), command.getInteractionType());
                    
        } catch (Exception e) {
            // Log but don't throw - AI service tracking is non-critical
            log.warn("Failed to forward interaction to AI service: {}", e.getMessage());
        }
    }
}
