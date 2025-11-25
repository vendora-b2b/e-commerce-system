package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Use case for listing quotation offers with pagination and filtering.
 */
@RequiredArgsConstructor
public class ListQuotationOffersUseCase {
    
    private final QuotationRepository quotationRepository;

    public ListQuotationOffersResult execute(ListQuotationOffersCommand command) {
        PageRequest pageRequest = PageRequest.of(
            command.getPage(),
            command.getSize(),
            Sort.by(Sort.Direction.fromString(command.getSortDirection()), command.getSortBy())
        );

        Page<QuotationOffer> offers = quotationRepository.findOffersByFilter(
            command.getRequestId(),
            command.getSupplierId(),
            command.getStatus(),
            pageRequest
        );

        return ListQuotationOffersResult.success(offers);
    }
}