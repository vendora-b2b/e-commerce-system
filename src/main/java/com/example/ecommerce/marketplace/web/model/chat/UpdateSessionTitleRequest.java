package com.example.ecommerce.marketplace.web.model.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for updating a chat session title.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionTitleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;
}
