package com.example.ecommerce.marketplace.application.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;

/**
 * Result object returned after tracking a user interaction.
 */
public class TrackUserInteractionResult {

    private final boolean success;
    private final Long interactionId;
    private final InteractionType interactionType;
    private final String message;
    private final String errorCode;

    private TrackUserInteractionResult(boolean success, Long interactionId, 
                                        InteractionType interactionType,
                                        String message, String errorCode) {
        this.success = success;
        this.interactionId = interactionId;
        this.interactionType = interactionType;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static TrackUserInteractionResult success(Long interactionId, InteractionType interactionType) {
        return new TrackUserInteractionResult(
            true, interactionId, interactionType, "Interaction tracked successfully", null
        );
    }

    public static TrackUserInteractionResult failure(String message, String errorCode) {
        return new TrackUserInteractionResult(false, null, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getInteractionId() {
        return interactionId;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
