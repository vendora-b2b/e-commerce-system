package com.example.ecommerce.marketplace.web.model.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for quotation request creation.
 * Represents the result of creating a quotation request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequestResponse {

    private Long quotationRequestId;
    private String message;

    /**
     * Creates a successful response.
     */
    public static QuotationRequestResponse success(Long quotationRequestId) {
        return new QuotationRequestResponse(
            quotationRequestId,
            "Quotation request created successfully"
        );
    }
}
