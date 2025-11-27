package com.example.ecommerce.marketplace.domain.quotation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private QuotationRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime validUntil;
    private String notes;

    // Private constructor for builder
    private QuotationRequest() {
        this.requestItems = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.status = QuotationRequestStatus.PENDING;
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { 
        return id; 
    }
    
    public String getRequestNumber() { 
        return requestNumber; 
    }

    public Long getRetailerId() { 
        return retailerId; 
    }
    
    public Long getSupplierId() { 
        return supplierId; 
    }

    public List<QuotationRequestItem> getRequestItems() { 
        return new ArrayList<>(requestItems); 
    }

    public QuotationRequestStatus getStatus() { 
        return status; 
    }

    public LocalDateTime getcreatedAt() { 
        return createdAt; 
    }

    public LocalDateTime getValidUntil() { 
        return validUntil; 
    }

    public String getNotes() { 
        return notes; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public void submit() {
        validateForSubmission();
        this.status = QuotationRequestStatus.PENDING;
    }

    public void markRequestReceived() {
        if (this.status != QuotationRequestStatus.PENDING) {
            throw new IllegalStateException("Can only mark request received for PENDING requests");
        }
        this.status = QuotationRequestStatus.REQUEST_RECEIVED;
    }

    public void markOffersSent() {
        if (this.status != QuotationRequestStatus.REQUEST_RECEIVED) {
            throw new IllegalStateException("Can only mark offers sent for REQUEST_RECEIVED requests");
        }
        this.status = QuotationRequestStatus.OFFERS_SENT;
        // Automatically expire the request after offers are sent
        // This prevents further modifications to the request
        this.status = QuotationRequestStatus.EXPIRED;
    }

    public void expire() {
        if (this.status == QuotationRequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot expire cancelled request");
        }
        this.status = QuotationRequestStatus.EXPIRED;
    }

    public void cancel() {
        if (this.status == QuotationRequestStatus.EXPIRED) {
            throw new IllegalStateException("Cannot cancel expired request");
        }
        if (this.status == QuotationRequestStatus.OFFERS_SENT) {
            throw new IllegalStateException("Cannot cancel request that already has offers sent");
        }
        this.status = QuotationRequestStatus.CANCELLED;
    }

    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    // Validation methods
    private void validateForSubmission() {
        if (retailerId == null) {
            throw new IllegalStateException("Retailer ID is required");
        }
        if (supplierId == null) {
            throw new IllegalStateException("Supplier ID is required");
        }
        if (requestItems == null || requestItems.isEmpty()) {
            throw new IllegalStateException("At least one request item is required");
        }
        // Set a default validity period
        if (validUntil == null) {
            validUntil = LocalDateTime.now().plusDays(30);
        }
        if (validUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Validity period cannot be in the past");
        }
        requestItems.forEach(QuotationRequestItem::validate);
    }

    public void addRequestItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, String specifications) {
        QuotationRequestItem item = new QuotationRequestItem(productId, variantId, quantity, quotedPrice, specifications);
        this.requestItems.add(item);
    }
    
    public void addRequestItem(Long productId, Integer quantity, Double quotedPrice, String specifications) {
        addRequestItem(productId, null, quantity, quotedPrice, specifications);
    }


    /**
     * Inner class representing a quotation request item.
     */
    public static class QuotationRequestItem {
        private final Long productId;
        private final Long variantId;
        private final Integer quantity;
        private final Double quotedPrice;

        // Optional specifications (color, size) or details about the product
        private final String specifications;

        private QuotationRequestItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, String specifications) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.quotedPrice = quotedPrice;
            this.specifications = specifications;
            validate();
        }

        public Long getProductId() { 
            return productId; 
        }
        
        public Long getVariantId() { 
            return variantId; 
        }
        
        public Integer getQuantity() { 
            return quantity; 
        }
        
        public Double getQuotedPrice() { 
            return quotedPrice; 
        }
        
        public String getSpecifications() { 
            return specifications; 
        }

        private void validate() {
            if (productId == null) {
                throw new IllegalStateException("Product ID is required");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException("Valid quantity is required");
            }
            if (quotedPrice == null || quotedPrice <= 0) {
                throw new IllegalStateException("Valid quoted price is required");
            }
        }
    }

    // Builder class
    public static class Builder {
        private final QuotationRequest request;

        private Builder() {
            request = new QuotationRequest();
        }

        public Builder requestNumber(String requestNumber) {
            request.requestNumber = requestNumber;
            return this;
        }

        public Builder retailerId(Long retailerId) {
            request.retailerId = retailerId;
            return this;
        }

        public Builder supplierId(Long supplierId) {
            request.supplierId = supplierId;
            return this;
        }

        public Builder addRequestItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, String specifications) {
            QuotationRequestItem item = new QuotationRequestItem(productId, variantId, quantity, quotedPrice, specifications);
            request.requestItems.add(item);
            return this;
        }
        
        public Builder addRequestItem(Long productId, Integer quantity, Double quotedPrice, String specifications) {
            return addRequestItem(productId, null, quantity, quotedPrice, specifications);
        }

        public Builder validUntil(LocalDateTime validUntil) {
            request.validUntil = validUntil;
            return this;
        }

        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }

        public QuotationRequest build() {
            try {
                Objects.requireNonNull(request.retailerId, "Retailer ID is required");
                Objects.requireNonNull(request.supplierId, "Supplier ID is required");
                if (request.requestItems.isEmpty()) {
                    throw new IllegalStateException("At least one request item is required");
                }
                return request;
            } catch (NullPointerException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }
}
