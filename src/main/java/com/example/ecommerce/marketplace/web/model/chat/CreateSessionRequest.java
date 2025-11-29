package com.example.ecommerce.marketplace.web.model.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for creating a new chat session.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Optional title for the chat session.
     * If not provided, a default title will be used.
     */
    private String title;
}
