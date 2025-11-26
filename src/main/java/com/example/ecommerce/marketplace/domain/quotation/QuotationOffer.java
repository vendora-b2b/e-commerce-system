package com.example.ecommerce.marketplace.domain.quotation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a quotation offer entity in the e-commerce marketplace.
 * Quotation offers are created by suppliers in response to quotation requests from retailers.
 */
public class QuotationOffer {
    private Long id;
    private String offerNumber;
    private Long quotationRequestId;
    private Long retailerId;
    private Long supplierId;
    private List<QuotationOfferItem> offerItems;
    private QuotationOfferStatus status;
    private LocalDateTime offerDate;
    private LocalDateTime validUntil;
    private Double totalAmount;
    private String notes;
    private String termsAndConditions;
    private LocalDateTime createdAt;

    // Private constructor for builder
    private QuotationOffer() {
        this.offerItems = new ArrayList<>();
        this.offerDate = LocalDateTime.now();
        this.status = QuotationOfferStatus.PENDING;
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { 
        return id; 
    }

    public String getOfferNumber() { 
        return offerNumber; 
    }

    public Long getQuotationRequestId() { 
        return quotationRequestId; 
    }

    public Long getRetailerId() { 
        return retailerId; 
    }

    public Long getSupplierId() { 
        return supplierId; 
    }

    public List<QuotationOfferItem> getOfferItems() { 
        return new ArrayList<>(offerItems); 
    }

    public QuotationOfferStatus getStatus() { 
        return status; 
    }

    public LocalDateTime getOfferDate() { 
        return offerDate; 
    }

    public LocalDateTime getValidUntil() { 
        return validUntil; 
    }

    public Double getTotalAmount() { 
        return totalAmount; 
    }

    public String getNotes() { 
        return notes; 
    }

    public String getTermsAndConditions() { 
        return termsAndConditions; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public void submit() {
        validateForSubmission();
        calculateTotalAmount();
        // Status remains PENDING after submission - waiting for retailer response
    }

    public void accept() {
        if (isExpired()) {
            throw new IllegalStateException("Cannot accept expired offer");
        }
        if (status != QuotationOfferStatus.PENDING) {
            throw new IllegalStateException("Only pending offers can be accepted");
        }
        this.status = QuotationOfferStatus.ACCEPTED;
    }

    public void reject() {
        if (status != QuotationOfferStatus.PENDING) {
            throw new IllegalStateException("Only pending offers can be rejected");
        }
        this.status = QuotationOfferStatus.REJECTED;
    }

    // Note: Can be modified to allow withdrawal from other statuses with extra charges
    public void withdraw() {
        if (this.status == QuotationOfferStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot withdraw ACCEPTED offer");
        }
        this.status = QuotationOfferStatus.WITHDRAWN;
    }

    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    public void expire() {
        if (this.status == QuotationOfferStatus.ACCEPTED || 
            this.status == QuotationOfferStatus.REJECTED ||
            this.status == QuotationOfferStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot expire offer that has been " + this.status.toString().toLowerCase());
        }
        this.status = QuotationOfferStatus.EXPIRED;
    }

    /**
     * Validates that offer items match the corresponding request items for maintenance purposes.
     * Throws IllegalStateException if there are mismatches that require maintenance attention.
     */
    public void validateAttributeMatchingWithRequest(QuotationRequest request) {
        if (!Objects.equals(this.quotationRequestId, request.getId())) {
            throw new IllegalStateException("Offer does not correspond to the provided request - MAINTENANCE REQUIRED");
        }
        
        if (!Objects.equals(this.retailerId, request.getRetailerId())) {
            throw new IllegalStateException("Retailer ID mismatch between offer and request - MAINTENANCE REQUIRED");
        }
        
        if (!Objects.equals(this.supplierId, request.getSupplierId())) {
            throw new IllegalStateException("Supplier ID mismatch between offer and request - MAINTENANCE REQUIRED");
        }

        List<QuotationRequest.QuotationRequestItem> requestItems = request.getRequestItems();
        
        // Check if offer has items not in the request
        for (QuotationOfferItem offerItem : this.offerItems) {
            boolean found = requestItems.stream().anyMatch(requestItem -> 
                Objects.equals(offerItem.getProductId(), requestItem.getProductId()) &&
                Objects.equals(offerItem.getVariantId(), requestItem.getVariantId())
            );
            
            if (!found) {
                throw new IllegalStateException(String.format(
                    "Offer contains item (Product ID: %s, Variant ID: %s) not present in request - MAINTENANCE REQUIRED",
                    offerItem.getProductId(), offerItem.getVariantId()));
            }
        }
        
        // Check for quantity mismatches
        for (QuotationOfferItem offerItem : this.offerItems) {
            requestItems.stream()
                .filter(requestItem -> 
                    Objects.equals(offerItem.getProductId(), requestItem.getProductId()) &&
                    Objects.equals(offerItem.getVariantId(), requestItem.getVariantId()))
                .findFirst()
                .ifPresent(requestItem -> {
                    if (!Objects.equals(offerItem.getQuantity(), requestItem.getQuantity())) {
                        throw new IllegalStateException(String.format(
                            "Quantity mismatch for Product ID %s: Request=%d, Offer=%d - MAINTENANCE REQUIRED",
                            offerItem.getProductId(), requestItem.getQuantity(), offerItem.getQuantity()));
                    }
                });
        }
    }

    private void calculateTotalAmount() {
        this.totalAmount = offerItems.stream()
                .mapToDouble(item -> item.quotedPrice * item.quantity)
                .sum();
    }

    // Validation methods
    private void validateForSubmission() {
        if (quotationRequestId == null) {
            throw new IllegalStateException("Quotation Request ID is required");
        }
        if (retailerId == null) {
            throw new IllegalStateException("Retailer ID is required");
        }
        if (supplierId == null) {
            throw new IllegalStateException("Supplier ID is required");
        }
        if (offerItems == null || offerItems.isEmpty()) {
            throw new IllegalStateException("At least one offer item is required");
        }
        // Set a default validity period
        if (validUntil == null) {
            validUntil = LocalDateTime.now().plusDays(7);
        }
        if (validUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Validity period cannot be in the past");
        }
        // Terms and conditions are now optional as per API specification
        offerItems.forEach(QuotationOfferItem::validate);
    }

    /**
     * Inner class representing a quotation offer item.
     */
    public static class QuotationOfferItem {
        private final Long productId;
        private final Long variantId;
        private final Integer quantity;
        private final Double quotedPrice;
        private final String specifications;
        private final String notes;

        private QuotationOfferItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, 
                                String specifications, String notes) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.quotedPrice = quotedPrice;
            this.specifications = specifications;
            this.notes = notes;
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

        public String getNotes() { 
            return notes; 
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
        private final QuotationOffer offer;

        private Builder() {
            offer = new QuotationOffer();
        }

        public Builder offerNumber(String offerNumber) {
            offer.offerNumber = offerNumber;
            return this;
        }

        public Builder quotationRequestId(Long quotationRequestId) {
            offer.quotationRequestId = quotationRequestId;
            return this;
        }

        public Builder retailerId(Long retailerId) {
            offer.retailerId = retailerId;
            return this;
        }

        public Builder supplierId(Long supplierId) {
            offer.supplierId = supplierId;
            return this;
        }

        public Builder addOfferItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, 
                                  String specifications) {
            QuotationOfferItem item = new QuotationOfferItem(productId, variantId, quantity, quotedPrice, 
                                                          specifications, null);
            offer.offerItems.add(item);
            return this;
        }

        public Builder addOfferItem(Long productId, Long variantId, Integer quantity, Double quotedPrice, 
                                  String specifications, String notes) {
            QuotationOfferItem item = new QuotationOfferItem(productId, variantId, quantity, quotedPrice, 
                                                          specifications, notes);
            offer.offerItems.add(item);
            return this;
        }
        
        public Builder addOfferItem(Long productId, Integer quantity, Double quotedPrice, 
                                  String specifications) {
            return addOfferItem(productId, null, quantity, quotedPrice, specifications);
        }

        public Builder addOfferItem(Long productId, Integer quantity, Double quotedPrice, 
                                  String specifications, String notes) {
            return addOfferItem(productId, null, quantity, quotedPrice, specifications, notes);
        }

        public Builder validUntil(LocalDateTime validUntil) {
            offer.validUntil = validUntil;
            return this;
        }

        public Builder notes(String notes) {
            offer.notes = notes;
            return this;
        }

        public Builder termsAndConditions(String termsAndConditions) {
            offer.termsAndConditions = termsAndConditions;
            return this;
        }

        public QuotationOffer build() {
            Objects.requireNonNull(offer.quotationRequestId, "Quotation Request ID is required");
            Objects.requireNonNull(offer.retailerId, "Retailer ID is required");
            Objects.requireNonNull(offer.supplierId, "Supplier ID is required");
            if (offer.offerItems.isEmpty()) {
                throw new IllegalStateException("At least one offer item is required");
            }
            return offer;
        }
    }
}
