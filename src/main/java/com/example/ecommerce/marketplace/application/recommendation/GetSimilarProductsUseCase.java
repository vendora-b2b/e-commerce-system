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
 * Use case for getting similar products based on vector similarity.
 * Used for "Customers also viewed" or "Similar items" sections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetSimilarProductsUseCase {

    private final AiServiceClient aiServiceClient;

    /**
     * Executes the get similar products use case.
     *
     * @param command the command containing product ID and limit
     * @return the result containing similar products
     * @throws IllegalArgumentException if productId is null
     * @throws CustomBusinessException if recommendation service fails
     */
    public GetSimilarProductsResult execute(GetSimilarProductsCommand command) {
        // Step 1: Validate productId
        if (command.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        try {
            // Step 2: Call AI service for similar products
            RecommendationResponse response = aiServiceClient.getSimilarProducts(
                command.getProductId(),
                command.getLimit()
            );

            // Step 3: Handle null or empty response
            if (response == null || response.getRecommendations() == null) {
                log.warn("No similar products returned for product {}", command.getProductId());
                return GetSimilarProductsResult.success(command.getProductId(), Collections.emptyList());
            }

            // Step 4: Convert AI response to result format
            List<GetSimilarProductsResult.ProductRecommendation> similarProducts =
                response.getRecommendations().stream()
                    .map(r -> new GetSimilarProductsResult.ProductRecommendation(
                        r.getProductId(),
                        r.getSku(),
                        r.getName(),
                        r.getScore()
                    ))
                    .collect(Collectors.toList());

            log.debug("Retrieved {} similar products for product {}", 
                    similarProducts.size(), command.getProductId());
            
            // Step 5: Return success
            return GetSimilarProductsResult.success(command.getProductId(), similarProducts);

        } catch (AiServiceException e) {
            log.error("AI service error getting similar products for product {}: {}", 
                    command.getProductId(), e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR",
                "Recommendation service is temporarily unavailable: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error getting similar products for product {}: {}", 
                    command.getProductId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to get similar products for product {}: {}", 
                    command.getProductId(), e.getMessage());
            throw new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR",
                "Failed to get similar products: " + e.getMessage(), e);
        }
    }
}
