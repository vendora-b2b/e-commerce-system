package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationItemStatus;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "quotation_items")
public class QuotationItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "quotation_id", nullable = false, insertable = false, updatable = false)
    private Long quotationId;
    
    @Column(name = "variant_id", nullable = false)
    private Long variantId;
    
    @Column(name = "product_id")
    private Long productId;
    
    // Retailer's request
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;
    
    @Column(name = "target_price")
    private Double targetPrice;
    
    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;
    
    @Column(name = "retailer_notes", columnDefinition = "TEXT")
    private String retailerNotes;
    
    // Supplier's response
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    private QuotationItemStatus itemStatus;
    
    @Column(name = "offered_quantity")
    private Integer offeredQuantity;
    
    @Column(name = "offered_price")
    private Double offeredPrice;
    
    @Column(name = "offered_delivery_date")
    private LocalDate offeredDeliveryDate;
    
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;
    
    @Column(name = "supplier_notes", columnDefinition = "TEXT")
    private String supplierNotes;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    // Retailer's final decision
    @Enumerated(EnumType.STRING)
    @Column(name = "retailer_action")
    private RetailerAction retailerAction;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
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

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }

    public String getRetailerNotes() {
        return retailerNotes;
    }

    public void setRetailerNotes(String retailerNotes) {
        this.retailerNotes = retailerNotes;
    }

    public QuotationItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(QuotationItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Integer getOfferedQuantity() {
        return offeredQuantity;
    }

    public void setOfferedQuantity(Integer offeredQuantity) {
        this.offeredQuantity = offeredQuantity;
    }

    public Double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(Double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public LocalDate getOfferedDeliveryDate() {
        return offeredDeliveryDate;
    }

    public void setOfferedDeliveryDate(LocalDate offeredDeliveryDate) {
        this.offeredDeliveryDate = offeredDeliveryDate;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public String getSupplierNotes() {
        return supplierNotes;
    }

    public void setSupplierNotes(String supplierNotes) {
        this.supplierNotes = supplierNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public RetailerAction getRetailerAction() {
        return retailerAction;
    }

    public void setRetailerAction(RetailerAction retailerAction) {
        this.retailerAction = retailerAction;
    }
}
