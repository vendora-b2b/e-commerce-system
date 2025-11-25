package com.example.ecommerce.marketplace.web.model.quotation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating quotation request status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuotationRequestStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACCEPTED|CANCELLED", message = "Status must be either ACCEPTED or CANCELLED")
    private String status;
}