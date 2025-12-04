package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
public class QuotationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "quotation_number", unique = true, nullable = false)
    private String quotationNumber;
    
    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "quotation_id", nullable = false)
    private List<QuotationItemEntity> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuotationStatus status;
    
    @Column(name = "retailer_notes", columnDefinition = "TEXT")
    private String retailerNotes;
    
    @Column(name = "supplier_notes", columnDefinition = "TEXT")
    private String supplierNotes;
    
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }

    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(Long retailerId) {
        this.retailerId = retailerId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public List<QuotationItemEntity> getItems() {
        return items;
    }

    public void setItems(List<QuotationItemEntity> items) {
        this.items = items;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public String getRetailerNotes() {
        return retailerNotes;
    }

    public void setRetailerNotes(String retailerNotes) {
        this.retailerNotes = retailerNotes;
    }

    public String getSupplierNotes() {
        return supplierNotes;
    }

    public void setSupplierNotes(String supplierNotes) {
        this.supplierNotes = supplierNotes;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
