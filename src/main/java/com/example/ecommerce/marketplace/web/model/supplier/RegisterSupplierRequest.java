package com.example.ecommerce.marketplace.web.model.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for supplier registration.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSupplierRequest {

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
}
