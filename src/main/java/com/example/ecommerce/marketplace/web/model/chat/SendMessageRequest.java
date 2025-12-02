package com.example.ecommerce.marketplace.web.model.chat;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for sending a message in a chat session.
 * Used with POST /api/v1/chat/sessions/{sessionId}/messages endpoint
 * where sessionId is provided in the path, not the body.
 * 
 * This is a flexible DTO that accepts message content via different field names
 * to accommodate various frontend implementations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "You must log in before using this feature")
    private Long userId;

    /**
     * The message/question content.
     * Accepts "question", "message", or "content" from frontend for flexibility.
     */
    @NotBlank(message = "The message must not be empty")
    @Size(max = 10000, message = "The message is too long (maximum 10,000 characters)")
    private String question;
    
    /**
     * Alternative field for question - maps to question internally.
     */
    @JsonAlias({"message", "content"})
    public void setMessage(String message) {
        if (this.question == null || this.question.isEmpty()) {
            this.question = message;
        }
    }

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
