package com.example.ecommerce.marketplace.application.recommendation;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.RecommendationResponse;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for getting personalized product recommendations for a user.
 * Retrieves recommendations from the AI service based on user interaction history.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetProductRecommendationsUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the get product recommendations use case.
     *
     * @param command the command containing user ID and limit
     * @return the result containing product recommendations
     * @throws IllegalArgumentException if userId is null
     * @throws CustomBusinessException if recommendation service fails
     */
    public GetProductRecommendationsResult execute(GetProductRecommendationsCommand command) {
        // Step 1: Validate userId
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        try {
            // Step 2: Call AI service for recommendations
            RecommendationResponse response = aiServiceClient.getUserRecommendations(
                command.getUserId(),
                command.getLimit()
            );

            // Step 3: Handle null or empty response
            if (response == null || response.getRecommendations() == null) {
                log.warn("No recommendations returned for user {}", command.getUserId());
                return GetProductRecommendationsResult.success(Collections.emptyList());
            }

            // Step 4: Convert AI response to result format
            List<GetProductRecommendationsResult.ProductRecommendation> recommendations =
                response.getRecommendations().stream()
                    .map(r -> new GetProductRecommendationsResult.ProductRecommendation(
                        r.getProductId(),
                        r.getSku(),
                        r.getName(),
                        r.getScore()
                    ))
                    .collect(Collectors.toList());

            log.debug("Retrieved {} recommendations for user {}", recommendations.size(), command.getUserId());
            
            // Step 5: Return success
            return GetProductRecommendationsResult.success(recommendations);

        } catch (AiServiceException e) {
            log.error("AI service error getting recommendations for user {}: {}", command.getUserId(), e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR", 
                "Recommendation service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error getting recommendations for user {}: {}", command.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to get recommendations for user {}: {}", command.getUserId(), e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR",
                "Failed to get recommendations: " + e.getMessage(), e);
        }
    }
}
