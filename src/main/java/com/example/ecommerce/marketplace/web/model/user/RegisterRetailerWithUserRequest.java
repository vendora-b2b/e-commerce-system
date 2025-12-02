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
    @NotBlank(message = "Please choose a username")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
    private String username;

    @NotBlank(message = "Please create a password")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    // Retailer information
    @NotBlank(message = "Please enter your business name")
    private String name;

    @NotBlank(message = "Please enter your email address")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Please enter your business license number")
    private String businessLicense;

    // Optional fields
    private String phone;
    private String address;
    private String profilePicture;
    private String profileDescription;
    private Double creditLimit;
}
