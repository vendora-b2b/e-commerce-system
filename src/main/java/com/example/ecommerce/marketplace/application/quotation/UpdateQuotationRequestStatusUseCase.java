package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.RequiredArgsConstructor;

/**
 * Use case for updating quotation request status.
 */
@RequiredArgsConstructor
public class UpdateQuotationRequestStatusUseCase {
    
    private final QuotationRepository quotationRepository;

    public UpdateQuotationRequestStatusResult execute(UpdateQuotationRequestStatusCommand command) {
        if (command.getRequestId() == null) {
            return UpdateQuotationRequestStatusResult.failure("Request ID is required", "INVALID_REQUEST_ID");
        }

        QuotationRequest request = quotationRepository.findRequestById(command.getRequestId());
        if (request == null) {
            return UpdateQuotationRequestStatusResult.failure("Quotation request not found", "REQUEST_NOT_FOUND");
        }

        if (request.isExpired()) {
            return UpdateQuotationRequestStatusResult.failure("Cannot update expired request", "REQUEST_EXPIRED");
        }

        try {
            // Map API status to domain actions
            switch (command.getStatus().toUpperCase()) {
                case "CANCELLED":
                    request.cancel();
                    break;
                case "EXPIRED":
                    request.expire();
                    break;
                default:
                    return UpdateQuotationRequestStatusResult.failure("Invalid status", "INVALID_STATUS");
            }

            QuotationRequest savedRequest = quotationRepository.saveQuotationRequest(request);
            return UpdateQuotationRequestStatusResult.success(savedRequest);

        } catch (IllegalStateException e) {
            return UpdateQuotationRequestStatusResult.failure(e.getMessage(), "STATUS_CHANGE_NOT_ALLOWED");
        }
    }
}