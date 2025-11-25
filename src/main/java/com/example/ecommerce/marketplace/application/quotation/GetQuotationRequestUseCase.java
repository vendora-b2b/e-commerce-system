package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.RequiredArgsConstructor;

/**
 * Use case for retrieving a specific quotation request by ID.
 */
@RequiredArgsConstructor
public class GetQuotationRequestUseCase {
    
    private final QuotationRepository quotationRepository;

    public GetQuotationRequestResult execute(Long requestId) {
        if (requestId == null) {
            return GetQuotationRequestResult.failure("Request ID is required", "INVALID_REQUEST_ID");
        }

        QuotationRequest request = quotationRepository.findRequestById(requestId);
        if (request == null) {
            return GetQuotationRequestResult.failure("Quotation request not found", "REQUEST_NOT_FOUND");
        }

        return GetQuotationRequestResult.success(request);
    }
}