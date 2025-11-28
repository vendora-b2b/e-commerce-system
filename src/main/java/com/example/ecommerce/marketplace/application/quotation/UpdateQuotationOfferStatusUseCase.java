package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for updating quotation offer status.
 */
@RequiredArgsConstructor
public class UpdateQuotationOfferStatusUseCase {
    
    private final QuotationRepository quotationRepository;

    public UpdateQuotationOfferStatusResult execute(UpdateQuotationOfferStatusCommand command) {
        if (command.getOfferId() == null) {
            return UpdateQuotationOfferStatusResult.failure("Offer ID is required", "INVALID_OFFER_ID");
        }

        QuotationOffer offer = quotationRepository.findOfferById(command.getOfferId());
        if (offer == null) {
            return UpdateQuotationOfferStatusResult.failure("Quotation offer not found", "OFFER_NOT_FOUND");
        }

        if (offer.isExpired()) {
            return UpdateQuotationOfferStatusResult.failure("Cannot update expired offer", "OFFER_EXPIRED");
        }

        try {
            // Map API status to domain actions
            switch (command.getStatus().toUpperCase()) {
                case "ACCEPTED":
                    offer.accept();
                    break;
                case "REJECTED":
                    offer.reject();
                    break;
                case "WITHDRAWN":
                    offer.withdraw();
                    break;
                case "EXPIRED":
                    offer.expire();
                    break;
                default:
                    return UpdateQuotationOfferStatusResult.failure("Invalid status", "INVALID_STATUS");
            }

            QuotationOffer savedOffer = quotationRepository.saveQuotationOffer(offer);
            return UpdateQuotationOfferStatusResult.success(savedOffer);

        } catch (IllegalStateException e) {
            return UpdateQuotationOfferStatusResult.failure(e.getMessage(), "STATUS_CHANGE_NOT_ALLOWED");
        }
    }
}