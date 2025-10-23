package com.example.ecommerce.marketplace.domain.quotation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a quotation request entity in the e-commerce marketplace.
 * Quotation requests are created when retailers request price quotes from suppliers.
 */
public class QuotationRequest {

    private Long id;
    private String requestNumber;
    private Long retailerId;
    private Long supplierId;
    private List<QuotationRequestItem> requestItems;
    private String status;
    private LocalDateTime requestDate;

    /**
     * Inner class representing a quotation request item.
     */
    public static class QuotationRequestItem {
        private Long productId;
        private Integer quantity;
        private Double quotedPrice;
    }
}
