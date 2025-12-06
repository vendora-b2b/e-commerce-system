package com.example.ecommerce.marketplace.application.quotation;

/**
 * Result of finalizing a quotation.
 */
public class FinalizeQuotationResult {
    
    private final Long quotationId;
    private final String status;
    private final Long orderId;
    private final int acceptedItemCount;
    private final int rejectedItemCount;
    private final String message;
    
    public FinalizeQuotationResult(Long quotationId, String status, Long orderId, 
                                  int acceptedItemCount, int rejectedItemCount, String message) {
        this.quotationId = quotationId;
        this.status = status;
        this.orderId = orderId;
        this.acceptedItemCount = acceptedItemCount;
        this.rejectedItemCount = rejectedItemCount;
        this.message = message;
    }
    
    public Long getQuotationId() {
        return quotationId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public int getAcceptedItemCount() {
        return acceptedItemCount;
    }
    
    public int getRejectedItemCount() {
        return rejectedItemCount;
    }
    
    public String getMessage() {
        return message;
    }
}
