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
