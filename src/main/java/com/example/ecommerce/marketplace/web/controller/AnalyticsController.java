package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionCommand;
import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionResult;
import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionUseCase;
import com.example.ecommerce.marketplace.web.model.analytics.TrackInteractionRequest;
import com.example.ecommerce.marketplace.web.model.analytics.TrackInteractionResponse;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Analytics operations.
 * Handles HTTP requests for tracking user interactions.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "User Analytics Tracking API")
public class AnalyticsController {

    private final TrackUserInteractionUseCase trackUserInteractionUseCase;

    /**
     * Track a user interaction with a product.
     * POST /api/v1/analytics/track
     *
     * This endpoint is designed for fire-and-forget usage from the frontend.
     * It persists the interaction locally and forwards it to the AI service
     * for recommendation learning.
     *
     * @param request the interaction tracking request
     * @return 202 ACCEPTED on successful tracking
     */
    @Operation(summary = "Track user interaction", description = "Record a user interaction with a product for analytics and recommendation learning")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Interaction accepted for tracking",
            content = @Content(schema = @Schema(implementation = TrackInteractionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/track")
    public ResponseEntity<?> trackInteraction(
        @Valid @RequestBody TrackInteractionRequest request
    ) {
        // Build command
        TrackUserInteractionCommand command = TrackUserInteractionCommand.builder()
            .userId(request.getUserId())
            .productId(request.getProductId())
            .variantId(request.getVariantId())
            .interactionType(request.getInteractionType())
            .sessionId(request.getSessionId())
            .metadata(request.getMetadata())
            .build();

        // Execute use case
        TrackUserInteractionResult result = trackUserInteractionUseCase.execute(command);

        // Convert to response
        if (result.isSuccess()) {
            TrackInteractionResponse response = TrackInteractionResponse.success(
                result.getInteractionId(),
                result.getInteractionType()
            );
            // Return 202 Accepted since tracking is somewhat fire-and-forget
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
            case "PRODUCT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "TRACKING_FAILED" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
