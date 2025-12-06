package com.example.ecommerce.marketplace.domain.quotation;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a quotation entity in the e-commerce marketplace.
 * A quotation is a single-entity communication between one retailer and one supplier.
 * Uses line-item level status tracking for granular control.
 */
public class Quotation {
    private Long id;
    private String quotationNumber;
    private Long retailerId;
    private Long supplierId;
    private List<QuotationItem> items;
    private QuotationStatus status;
    private String retailerNotes;
    private String supplierNotes;
    private String termsAndConditions;
    private LocalDateTime validUntil;
    private Long orderId;  // Set when converted to order
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime finalizedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Private constructor for builder
    private Quotation() {
        this.items = new ArrayList<>();
        this.status = QuotationStatus.PENDING_SUPPLIER;
        this.createdAt = LocalDateTime.now();
        // Default validUntil to 30 days from now
        this.validUntil = LocalDateTime.now().plusDays(30);
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public List<QuotationItem> getItems() {
        return new ArrayList<>(items);
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public String getRetailerNotes() {
        return retailerNotes;
    }

    public String getSupplierNotes() {
        return supplierNotes;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public Long getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public int getItemCount() {
        return items.size();
    }

    public double getTotalEstimatedAmount() {
        return items.stream()
                .filter(item -> item.getOfferedPrice() != null && item.getOfferedQuantity() != null)
                .mapToDouble(item -> item.getOfferedPrice() * item.getOfferedQuantity())
                .sum();
    }

    /**
     * Supplier responds to the quotation request
     */
    public void respond(List<QuotationItemResponse> itemResponses, String supplierNotes, 
                       String termsAndConditions, LocalDateTime validUntil) {
        // Validate current status
        if (this.status != QuotationStatus.PENDING_SUPPLIER) {
            throw new IllegalStateException("Can only respond to quotations with PENDING_SUPPLIER status");
        }
        
        // Check not expired
        if (isExpired()) {
            throw new IllegalStateException("Cannot respond to expired quotation");
        }
        
        // Validate all items are included in response
        if (itemResponses.size() != this.items.size()) {
            throw new IllegalStateException("Response must include all quotation items");
        }
        
        // Update each item with supplier response
        for (QuotationItemResponse response : itemResponses) {
            QuotationItem item = findItemById(response.getQuotationItemId());
            if (item == null) {
                throw new IllegalStateException("Invalid quotation item ID: " + response.getQuotationItemId());
            }
            item.updateSupplierResponse(response);
        }
        
        // Update quotation level fields
        this.supplierNotes = supplierNotes;
        this.termsAndConditions = termsAndConditions;
        if (validUntil != null) {
            this.validUntil = validUntil;
        }
        this.respondedAt = LocalDateTime.now();
        this.status = QuotationStatus.PENDING_RETAILER;
    }

    /**
     * Retailer finalizes the quotation
     */
    public void finalize(List<RetailerItemDecision> itemDecisions, boolean createOrder) {
        // Validate current status
        if (this.status != QuotationStatus.PENDING_RETAILER) {
            throw new IllegalStateException("Can only finalize quotations with PENDING_RETAILER status");
        }
        
        // Check not expired
        if (isExpired()) {
            throw new IllegalStateException("Cannot finalize expired quotation");
        }
        
        // Validate all non-rejected items have decisions
        for (RetailerItemDecision decision : itemDecisions) {
            QuotationItem item = findItemById(decision.getQuotationItemId());
            if (item == null) {
                throw new IllegalStateException("Invalid quotation item ID: " + decision.getQuotationItemId());
            }
            item.setRetailerAction(decision.getRetailerAction());
        }
        
        this.finalizedAt = LocalDateTime.now();
        this.status = QuotationStatus.FINALIZED;
        
        // Note: Order creation is handled by the use case layer
    }

    /**
     * Cancel the quotation
     */
    public void cancel(String reason) {
        // Validate status
        if (this.status == QuotationStatus.FINALIZED) {
            throw new IllegalStateException("Cannot cancel finalized quotation");
        }
        if (this.status == QuotationStatus.CANCELLED) {
            throw new IllegalStateException("Quotation is already cancelled");
        }
        
        this.status = QuotationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Mark quotation as expired
     */
    public void expire() {
        if (this.status == QuotationStatus.FINALIZED || this.status == QuotationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot expire quotation with status: " + this.status);
        }
        this.status = QuotationStatus.EXPIRED;
    }

    /**
     * Check if quotation is expired
     */
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    /**
     * Set order ID when quotation is converted to order
     */
    public void setOrderId(Long orderId) {
        if (this.status != QuotationStatus.FINALIZED) {
            throw new IllegalStateException("Can only set order ID for finalized quotations");
        }
        this.orderId = orderId;
    }

    private QuotationItem findItemById(Long itemId) {
        return items.stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Inner class representing a quotation item.
     * Contains both retailer request and supplier response data.
     */
    public static class QuotationItem {
        private Long id;
        private Long variantId;
        private Long productId;
        
        // Retailer's request
        private Integer requestedQuantity;
        private Double targetPrice;
        private LocalDate requestedDeliveryDate;
        private String retailerNotes;
        
        // Supplier's response
        private QuotationItemStatus itemStatus;
        private Integer offeredQuantity;
        private Double offeredPrice;
        private LocalDate offeredDeliveryDate;
        private Integer leadTimeDays;
        private String supplierNotes;
        private String rejectionReason;
        
        // Retailer's final decision
        private RetailerAction retailerAction;

        private QuotationItem(Long variantId, Integer requestedQuantity, Double targetPrice,
                            LocalDate requestedDeliveryDate, String retailerNotes) {
            this.variantId = variantId;
            this.requestedQuantity = requestedQuantity;
            this.targetPrice = targetPrice;
            this.requestedDeliveryDate = requestedDeliveryDate;
            this.retailerNotes = retailerNotes;
            this.itemStatus = QuotationItemStatus.PENDING;
            validate();
        }

        public Long getId() {
            return id;
        }

        public Long getVariantId() {
            return variantId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getRequestedQuantity() {
            return requestedQuantity;
        }

        public Double getTargetPrice() {
            return targetPrice;
        }

        public LocalDate getRequestedDeliveryDate() {
            return requestedDeliveryDate;
        }

        public String getRetailerNotes() {
            return retailerNotes;
        }

        public QuotationItemStatus getItemStatus() {
            return itemStatus;
        }

        public Integer getOfferedQuantity() {
            return offeredQuantity;
        }

        public Double getOfferedPrice() {
            return offeredPrice;
        }

        public LocalDate getOfferedDeliveryDate() {
            return offeredDeliveryDate;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public String getSupplierNotes() {
            return supplierNotes;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public RetailerAction getRetailerAction() {
            return retailerAction;
        }

        void setRetailerAction(RetailerAction retailerAction) {
            this.retailerAction = retailerAction;
        }

        void updateSupplierResponse(QuotationItemResponse response) {
            this.itemStatus = response.getItemStatus();
            
            if (response.getItemStatus() == QuotationItemStatus.ACCEPTED || 
                response.getItemStatus() == QuotationItemStatus.PARTIAL) {
                
                if (response.getOfferedQuantity() == null || response.getOfferedQuantity() <= 0) {
                    throw new IllegalStateException("Offered quantity is required for ACCEPTED or PARTIAL items");
                }
                if (response.getOfferedPrice() == null || response.getOfferedPrice() <= 0) {
                    throw new IllegalStateException("Offered price is required for ACCEPTED or PARTIAL items");
                }
                
                this.offeredQuantity = response.getOfferedQuantity();
                this.offeredPrice = response.getOfferedPrice();
                this.offeredDeliveryDate = response.getOfferedDeliveryDate();
                this.leadTimeDays = response.getLeadTimeDays();
                this.supplierNotes = response.getSupplierNotes();
            } else if (response.getItemStatus() == QuotationItemStatus.REJECTED) {
                if (response.getRejectionReason() == null || response.getRejectionReason().trim().isEmpty()) {
                    throw new IllegalStateException("Rejection reason is required for REJECTED items");
                }
                this.rejectionReason = response.getRejectionReason();
                this.supplierNotes = response.getSupplierNotes();
            }
        }

        private void validate() {
            if (variantId == null) {
                throw new IllegalStateException("Variant ID is required");
            }
            if (requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalStateException("Valid requested quantity is required");
            }
            if (targetPrice != null && targetPrice <= 0) {
                throw new IllegalStateException("Target price must be positive if provided");
            }
            if (requestedDeliveryDate != null && requestedDeliveryDate.isBefore(LocalDate.now())) {
                throw new IllegalStateException("Requested delivery date cannot be in the past");
            }
        }
    }

    /**
     * Helper class for supplier response to a quotation item
     */
    public static class QuotationItemResponse {
        private final Long quotationItemId;
        private final QuotationItemStatus itemStatus;
        private final Integer offeredQuantity;
        private final Double offeredPrice;
        private final LocalDate offeredDeliveryDate;
        private final Integer leadTimeDays;
        private final String supplierNotes;
        private final String rejectionReason;

        public QuotationItemResponse(Long quotationItemId, QuotationItemStatus itemStatus,
                                    Integer offeredQuantity, Double offeredPrice,
                                    LocalDate offeredDeliveryDate, Integer leadTimeDays,
                                    String supplierNotes, String rejectionReason) {
            this.quotationItemId = quotationItemId;
            this.itemStatus = itemStatus;
            this.offeredQuantity = offeredQuantity;
            this.offeredPrice = offeredPrice;
            this.offeredDeliveryDate = offeredDeliveryDate;
            this.leadTimeDays = leadTimeDays;
            this.supplierNotes = supplierNotes;
            this.rejectionReason = rejectionReason;
        }

        public Long getQuotationItemId() { return quotationItemId; }
        public QuotationItemStatus getItemStatus() { return itemStatus; }
        public Integer getOfferedQuantity() { return offeredQuantity; }
        public Double getOfferedPrice() { return offeredPrice; }
        public LocalDate getOfferedDeliveryDate() { return offeredDeliveryDate; }
        public Integer getLeadTimeDays() { return leadTimeDays; }
        public String getSupplierNotes() { return supplierNotes; }
        public String getRejectionReason() { return rejectionReason; }
    }

    /**
     * Helper class for retailer's decision on a quotation item
     */
    public static class RetailerItemDecision {
        private final Long quotationItemId;
        private final RetailerAction retailerAction;

        public RetailerItemDecision(Long quotationItemId, RetailerAction retailerAction) {
            this.quotationItemId = quotationItemId;
            this.retailerAction = retailerAction;
        }

        public Long getQuotationItemId() { return quotationItemId; }
        public RetailerAction getRetailerAction() { return retailerAction; }
    }

    // Builder class
    public static class Builder {
        private final Quotation quotation;

        private Builder() {
            quotation = new Quotation();
        }

        public Builder quotationNumber(String quotationNumber) {
            quotation.quotationNumber = quotationNumber;
            return this;
        }

        public Builder retailerId(Long retailerId) {
            quotation.retailerId = retailerId;
            return this;
        }

        public Builder supplierId(Long supplierId) {
            quotation.supplierId = supplierId;
            return this;
        }

        public Builder addItem(Long variantId, Long productId, Integer requestedQuantity, 
                      Double targetPrice, LocalDate requestedDeliveryDate, String retailerNotes) {
            System.out.println("Builder.addItem called - variantId: " + variantId + ", qty: " + requestedQuantity);
            System.out.println("Items list before add: " + quotation.items);
            System.out.println("Items list size before: " + quotation.items.size());
            QuotationItem item = new QuotationItem(variantId, requestedQuantity, targetPrice,
                    requestedDeliveryDate, retailerNotes);
            item.setProductId(productId);
            System.out.println("Item created successfully");
            quotation.items.add(item);
            System.out.println("Items list size after: " + quotation.items.size());
            return this;
        }

        public Builder retailerNotes(String retailerNotes) {
            quotation.retailerNotes = retailerNotes;
            return this;
        }

        public Builder validUntil(LocalDateTime validUntil) {
            quotation.validUntil = validUntil;
            return this;
        }

        public Quotation build() {
            System.out.println("=== Builder.build() called ===");
            System.out.println("Items list: " + quotation.items);
            System.out.println("Items count: " + quotation.items.size());
            Objects.requireNonNull(quotation.retailerId, "Retailer ID is required");
            Objects.requireNonNull(quotation.supplierId, "Supplier ID is required");
            if (quotation.items.isEmpty()) {
                System.out.println("ERROR: Items list is empty!");
                throw new IllegalStateException("At least one quotation item is required");
            }
            return quotation;
        }
    }
}
