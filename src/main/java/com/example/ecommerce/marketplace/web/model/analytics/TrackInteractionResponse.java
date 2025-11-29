package com.example.ecommerce.marketplace.web.model.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for track interaction results.
 * Confirms the interaction was successfully recorded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackInteractionResponse {

    private Long interactionId;
    private InteractionType interactionType;
    private String message;

    /**
     * Creates a success response.
     *
     * @param interactionId   the ID of the recorded interaction
     * @param interactionType the type of interaction
     * @return the response DTO
     */
    public static TrackInteractionResponse success(Long interactionId, InteractionType interactionType) {
        return new TrackInteractionResponse(
            interactionId, 
            interactionType, 
            "Interaction tracked successfully"
        );
    }
}
