package com.example.ecommerce.marketplace.application.quotation;

import java.time.LocalDateTime;
import java.util.List;

public class SubmitQuotationOfferCommand {
    private final Long quotationRequestId;
    private final Long supplierId;
    private final List<OfferItem> offerItems;
    private final LocalDateTime validUntil;
    private final String notes;
    private final String termsAndConditions;

    public SubmitQuotationOfferCommand(
            Long quotationRequestId,
            Long supplierId,
            List<OfferItem> offerItems,
            LocalDateTime validUntil,
            String notes,
            String termsAndConditions) {
        this.quotationRequestId = quotationRequestId;
        this.supplierId = supplierId;
        this.offerItems = offerItems;
        this.validUntil = validUntil;
        this.notes = notes;
        this.termsAndConditions = termsAndConditions;
    }

    public Long getQuotationRequestId() { return quotationRequestId; }
    public Long getSupplierId() { return supplierId; }
    public List<OfferItem> getOfferItems() { return offerItems; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public String getNotes() { return notes; }
    public String getTermsAndConditions() { return termsAndConditions; }

    public static class OfferItem {
        private final Long productId;
        private final Long variantId;
        private final Integer quantity;
        private final Double quotedPrice;
        private final String specifications;
        private final String notes;

        public OfferItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, String specifications, String notes) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.quotedPrice = quotedPrice;
            this.specifications = specifications;
            this.notes = notes;
        }

        public Long getProductId() { return productId; }
        public Long getVariantId() { return variantId; }
        public Integer getQuantity() { return quantity; }
        public Double getQuotedPrice() { return quotedPrice; }
        public String getSpecifications() { return specifications; }
        public String getNotes() { return notes; }
    }
}
