package com.example.ecommerce.marketplace.web.model.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for quotation offer submission.
 * Represents the result of submitting a quotation offer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationOfferResponse {

    private Long quotationOfferId;
    private String message;

    /**
     * Creates a successful response.
     */
    public static QuotationOfferResponse success(Long quotationOfferId) {
        return new QuotationOfferResponse(
            quotationOfferId,
            "Quotation offer submitted successfully"
        );
    }
}
