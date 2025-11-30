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
 * Use case for getting homepage recommendations.
 * Supports both personalized recommendations for logged-in users and
 * generic popular/trending items for anonymous users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetHomepageRecommendationsUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the get homepage recommendations use case.
     *
     * @param command the command containing optional user ID and limit
     * @return the result containing homepage recommendations
     * @throws CustomBusinessException if recommendation service fails
     */
    public GetHomepageRecommendationsResult execute(GetHomepageRecommendationsCommand command) {
        try {
            // Step 1: Call AI service for homepage recommendations
            RecommendationResponse response = aiServiceClient.getHomepageRecommendations(
                command.getUserId(),
                command.getLimit()
            );

            // Step 2: Handle null or empty response
            if (response == null || response.getRecommendations() == null) {
                log.warn("No homepage recommendations returned for user {}", 
                        command.isAnonymous() ? "anonymous" : command.getUserId());
                return GetHomepageRecommendationsResult.success(Collections.emptyList(), false);
            }

            // Step 3: Convert AI response to result format
            List<GetHomepageRecommendationsResult.ProductRecommendation> recommendations =
                response.getRecommendations().stream()
                    .map(r -> new GetHomepageRecommendationsResult.ProductRecommendation(
                        r.getProductId(),
                        r.getSku(),
                        r.getName(),
                        r.getScore()
                    ))
                    .collect(Collectors.toList());

            // Step 4: Determine if results are personalized
            boolean personalized = !command.isAnonymous();

            log.debug("Retrieved {} homepage recommendations for {} user", 
                    recommendations.size(), 
                    personalized ? "authenticated" : "anonymous");
            
            // Step 5: Return success
            return GetHomepageRecommendationsResult.success(recommendations, personalized);

        } catch (AiServiceException e) {
            log.error("AI service error getting homepage recommendations: {}", e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR",
                "Recommendation service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error getting homepage recommendations: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to get homepage recommendations: {}", e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR",
                "Failed to get recommendations: " + e.getMessage(), e);
        }
    }
}
