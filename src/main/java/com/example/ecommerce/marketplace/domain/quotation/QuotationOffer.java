package com.example.ecommerce.marketplace.domain.quotation;

import java.time.LocalDateTime;
import java.util.List;

public class QuotationOffer {

    private Long id;
    private String offerNumber;
    private Long retailerId;
    private Long supplierId;
    private List<QuotationOfferItem> offerItems;
    private String status;
    private LocalDateTime offerDate;
    private LocalDateTime validUntil;
    private Double totalAmount;
    private String notes;

    /**
     * Inner class representing a quotation offer item.
     */
    public static class QuotationOfferItem {
        private Long productId;
        private Integer quantity;
        private Double quotedPrice;
    }
}
