package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.recommendation.*;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;
import com.example.ecommerce.marketplace.web.model.recommendation.RecommendationResponse;
import com.example.ecommerce.marketplace.web.model.recommendation.RecommendationResponse.ProductRecommendationItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Recommendation operations.
 * Handles HTTP requests for product recommendations.
 * API Version: v1
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "Product Recommendation API powered by AI")
public class RecommendationController {

    private final GetProductRecommendationsUseCase getProductRecommendationsUseCase;
    private final GetHomepageRecommendationsUseCase getHomepageRecommendationsUseCase;
    private final GetSimilarProductsUseCase getSimilarProductsUseCase;

    /**
     * Get similar product recommendations.
     * GET /api/v1/products/{productId}/recommendations
     *
     * @param productId the source product ID
     * @param limit     optional limit on number of recommendations
     * @return 200 OK with similar product recommendations
     */
    @Operation(summary = "Get similar products", description = "Get products similar to the specified product based on vector similarity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully",
            content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Recommendation service temporarily unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/products/{productId}/recommendations")
    public ResponseEntity<?> getSimilarProducts(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "6") int limit
    ) {
        // Build command
        GetSimilarProductsCommand command = new GetSimilarProductsCommand(productId, limit);

        // Execute use case
        GetSimilarProductsResult result = getSimilarProductsUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            List<ProductRecommendationItem> items = result.getSimilarProducts().stream()
                .map(r -> ProductRecommendationItem.of(
                    r.getProductId(),
                    r.getSku(),
                    r.getName(),
                    r.getSimilarityScore()
                ))
                .collect(Collectors.toList());
            
            RecommendationResponse response = RecommendationResponse.forSimilar(items);
            return ResponseEntity.ok(response);
        }

        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Get homepage recommendations.
     * GET /api/v1/recommendations/homepage
     *
     * @param userId optional user ID for personalized recommendations
     * @param limit  optional limit on number of recommendations
     * @return 200 OK with homepage recommendations
     */
    @Operation(summary = "Get homepage recommendations", description = "Get product recommendations for the homepage. Personalized if user ID is provided, otherwise returns popular products.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully",
            content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
        @ApiResponse(responseCode = "503", description = "Recommendation service temporarily unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/recommendations/homepage")
    public ResponseEntity<?> getHomepageRecommendations(
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "12") int limit
    ) {
        // Build command based on whether user is logged in
        GetHomepageRecommendationsCommand command;
        if (userId != null) {
            command = new GetHomepageRecommendationsCommand(userId, limit);
        } else {
            command = new GetHomepageRecommendationsCommand(null, limit);
        }

        // Execute use case
        GetHomepageRecommendationsResult result = getHomepageRecommendationsUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            List<ProductRecommendationItem> items = result.getRecommendations().stream()
                .map(r -> ProductRecommendationItem.of(
                    r.getProductId(),
                    r.getSku(),
                    r.getName(),
                    r.getScore()
                ))
                .collect(Collectors.toList());
            
            RecommendationResponse response = RecommendationResponse.forHomepage(items);
            return ResponseEntity.ok(response);
        }

        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Get personalized product recommendations for a user.
     * GET /api/v1/recommendations/user/{userId}
     *
     * @param userId the user ID
     * @param limit  optional limit on number of recommendations
     * @return 200 OK with personalized recommendations
     */
    @Operation(summary = "Get user recommendations", description = "Get personalized product recommendations based on user's interaction history")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully",
            content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Recommendation service temporarily unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/recommendations/user/{userId}")
    public ResponseEntity<?> getUserRecommendations(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        // Build command
        GetProductRecommendationsCommand command = new GetProductRecommendationsCommand(userId, limit);

        // Execute use case
        GetProductRecommendationsResult result = getProductRecommendationsUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            List<ProductRecommendationItem> items = result.getRecommendations().stream()
                .map(r -> ProductRecommendationItem.of(
                    r.getProductId(),
                    r.getSku(),
                    r.getName(),
                    r.getScore()
                ))
                .collect(Collectors.toList());
            
            RecommendationResponse response = RecommendationResponse.forUser(items);
            return ResponseEntity.ok(response);
        }

        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Maps error codes to appropriate HTTP status codes.
     */
    private HttpStatus mapErrorToStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (errorCode) {
            case "RECOMMENDATION_SERVICE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "PRODUCT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
