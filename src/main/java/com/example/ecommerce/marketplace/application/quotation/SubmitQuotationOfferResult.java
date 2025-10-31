package com.example.ecommerce.marketplace.application.quotation;

public class SubmitQuotationOfferResult {
    private final String offerNumber;
    private final Long offerId;
    private final Double totalAmount;

    public SubmitQuotationOfferResult(String offerNumber, Long offerId, Double totalAmount) {
        this.offerNumber = offerNumber;
        this.offerId = offerId;
        this.totalAmount = totalAmount;
    }

    public String getOfferNumber() { return offerNumber; }
    public Long getOfferId() { return offerId; }
    public Double getTotalAmount() { return totalAmount; }
}