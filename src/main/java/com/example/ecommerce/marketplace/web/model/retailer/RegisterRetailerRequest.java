package com.example.ecommerce.marketplace.web.model.retailer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for retailer registration.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRetailerRequest {

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
