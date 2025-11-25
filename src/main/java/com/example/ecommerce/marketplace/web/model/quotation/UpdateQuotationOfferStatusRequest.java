package com.example.ecommerce.marketplace.web.model.quotation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating quotation offer status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuotationOfferStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACCEPTED|REJECTED|WITHDRAWN", message = "Status must be ACCEPTED, REJECTED, or WITHDRAWN")
    private String status;
}