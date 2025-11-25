package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for retrieving a specific quotation offer by ID.
 */
@RequiredArgsConstructor
public class GetQuotationOfferUseCase {
    
    private final QuotationRepository quotationRepository;

    public GetQuotationOfferResult execute(Long offerId) {
        if (offerId == null) {
            return GetQuotationOfferResult.failure("Offer ID is required", "INVALID_OFFER_ID");
        }

        QuotationOffer offer = quotationRepository.findOfferById(offerId);
        if (offer == null) {
            return GetQuotationOfferResult.failure("Quotation offer not found", "OFFER_NOT_FOUND");
        }

        return GetQuotationOfferResult.success(offer);
    }
}