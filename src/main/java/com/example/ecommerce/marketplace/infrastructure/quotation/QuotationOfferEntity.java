package com.example.ecommerce.marketplace.infrastructure.quotation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import com.example.ecommerce.marketplace.domain.quotation.QuotationOfferStatus;

@Entity
@Table(name = "quotation_offers")
public class QuotationOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String offerNumber;
    private Long quotationRequestId;
    private Long retailerId;
    private Long supplierId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private List<QuotationOfferItemEntity> offerItems = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private QuotationOfferStatus status;
    
    private LocalDateTime offerDate;
    private LocalDateTime validUntil;
    private Double totalAmount;
    private String notes;
    private String termsAndConditions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getOfferNumber() { 
        return offerNumber; 
    }
    public void setOfferNumber(String offerNumber) { 
        this.offerNumber = offerNumber; 
    }

    public Long getQuotationRequestId() { 
        return quotationRequestId; 
    }

    public void setQuotationRequestId(Long quotationRequestId) { 
        this.quotationRequestId = quotationRequestId; 
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

    public List<QuotationOfferItemEntity> getOfferItems() { 
        return offerItems; 
    }

    public void setOfferItems(List<QuotationOfferItemEntity> offerItems) { 
        this.offerItems = offerItems; 
    }

    public QuotationOfferStatus getStatus() { 
        return status; 
    }

    public void setStatus(QuotationOfferStatus status) { 
        this.status = status; 
    }

    public LocalDateTime getOfferDate() { 
        return offerDate; 
    }

    public void setOfferDate(LocalDateTime offerDate) { 
        this.offerDate = offerDate; 
    }
    
    public LocalDateTime getValidUntil() { 
        return validUntil; 
    }
    public void setValidUntil(LocalDateTime validUntil) { 
        this.validUntil = validUntil; 
    }

    public Double getTotalAmount() { 
        return totalAmount; 
    }
    public void setTotalAmount(Double totalAmount) { 
        this.totalAmount = totalAmount; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    public String getTermsAndConditions() { 
        return termsAndConditions; 
    }

    public void setTermsAndConditions(String termsAndConditions) { 
        this.termsAndConditions = termsAndConditions; 
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
