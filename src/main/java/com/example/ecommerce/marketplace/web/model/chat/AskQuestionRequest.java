package com.example.ecommerce.marketplace.web.model.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for asking a question in a chat session.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AskQuestionRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 10000, message = "Question cannot exceed 10000 characters")
    private String question;

    /**
     * Type of user: "retailer" or "supplier".
     * Used for personalized responses.
     */
    private String userType;

    /**
     * Display name of the user.
     * Used for personalized responses.
     */
    private String userName;

    /**
     * Loyalty tier for retailers.
     * Used for personalized pricing recommendations.
     */
    private String loyaltyTier;
}
