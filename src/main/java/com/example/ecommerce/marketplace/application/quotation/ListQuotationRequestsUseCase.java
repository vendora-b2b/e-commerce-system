package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Use case for listing quotation requests with pagination and filtering.
 */
@RequiredArgsConstructor
public class ListQuotationRequestsUseCase {
    
    private final QuotationRepository quotationRepository;

    public ListQuotationRequestsResult execute(ListQuotationRequestsCommand command) {
        PageRequest pageRequest = PageRequest.of(
            command.getPage(),
            command.getSize(),
            Sort.by(Sort.Direction.fromString(command.getSortDirection()), command.getSortBy())
        );

        Page<QuotationRequest> requests = quotationRepository.findRequestsByFilter(
            command.getRetailerId(),
            command.getSupplierId(),
            command.getStatus(),
            pageRequest
        );

        return ListQuotationRequestsResult.success(requests);
    }
}