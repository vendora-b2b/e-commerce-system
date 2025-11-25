package com.example.ecommerce.marketplace.application.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command for listing quotation offers with pagination and filtering.
 */
@Getter
@AllArgsConstructor
public class ListQuotationOffersCommand {
    private final Long requestId;
    private final Long supplierId;
    private final String status;
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    public static ListQuotationOffersCommand create(
            Long requestId,
            Long supplierId, 
            String status,
            Integer page, 
            Integer size, 
            String sort) {
        
        // Default values
        int pageNum = page != null ? page : 0;
        int sizeNum = size != null ? Math.min(size, 100) : 20; // Max 100 items per page
        
        // Parse sort parameter (e.g., "createdAt,desc" or "offerNumber,asc")
        String sortBy = "createdAt"; // default
        String sortDirection = "desc"; // default
        
        if (sort != null && sort.contains(",")) {
            String[] sortParts = sort.split(",");
            sortBy = sortParts[0];
            if (sortParts.length > 1) {
                sortDirection = sortParts[1];
            }
        } else if (sort != null) {
            sortBy = sort;
        }
        
        return new ListQuotationOffersCommand(
            requestId, supplierId, status, pageNum, sizeNum, sortBy, sortDirection);
    }
}