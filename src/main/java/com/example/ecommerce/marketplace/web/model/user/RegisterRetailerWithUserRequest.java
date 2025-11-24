package com.example.ecommerce.marketplace.web.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for retailer registration with user account.
 * Contains both user credentials and retailer information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRetailerWithUserRequest {

    // User credentials
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    // Retailer information
    @NotBlank(message = "Retailer name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Business license is required")
    private String businessLicense;

    // Optional fields
    private String phone;
    private String address;
    private String profilePicture;
    private String profileDescription;
    private Double creditLimit;
}
