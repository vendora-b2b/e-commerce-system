package com.example.ecommerce.marketplace.application.quotation;

/**
 * Result object returned after quotation offer submission attempt.
 */
public class SubmitQuotationOfferResult {
    
    private final boolean success;
    private final String offerNumber;
    private final Long offerId;
    private final Double totalAmount;
    private final String message;
    private final String errorCode;

    private SubmitQuotationOfferResult(boolean success, String offerNumber, Long offerId, Double totalAmount, String message, String errorCode) {
        this.success = success;
        this.offerNumber = offerNumber;
        this.offerId = offerId;
        this.totalAmount = totalAmount;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static SubmitQuotationOfferResult success(String offerNumber, Long offerId, Double totalAmount) {
        return new SubmitQuotationOfferResult(true, offerNumber, offerId, totalAmount, "Quotation offer submitted successfully", null);
    }

    public static SubmitQuotationOfferResult failure(String message, String errorCode) {
        return new SubmitQuotationOfferResult(false, null, null, null, message, errorCode);
    }

    public boolean isSuccess() { return success; }
    public String getOfferNumber() { return offerNumber; }
    public Long getOfferId() { return offerId; }
    public Double getTotalAmount() { return totalAmount; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
}
