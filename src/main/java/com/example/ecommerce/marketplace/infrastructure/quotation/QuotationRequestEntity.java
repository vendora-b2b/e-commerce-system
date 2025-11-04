package com.example.ecommerce.marketplace.infrastructure.quotation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequestStatus;

@Entity
@Table(name = "quotation_requests")
public class QuotationRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String requestNumber;
    private Long retailerId;
    private Long supplierId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "request_id")
    private List<QuotationRequestItemEntity> requestItems = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private QuotationRequestStatus status;
    
    private LocalDateTime requestDate;
    private LocalDateTime validUntil;
    private String notes;

    // Getters and Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getRequestNumber() { 
        return requestNumber; 
    }

    public void setRequestNumber(String requestNumber) { 
        this.requestNumber = requestNumber; 
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

    public List<QuotationRequestItemEntity> getRequestItems() { 
        return requestItems; 
    }

    public void setRequestItems(List<QuotationRequestItemEntity> requestItems) { 
        this.requestItems = requestItems; 
    }

    public QuotationRequestStatus getStatus() { 
        return status; 
    }

    public void setStatus(QuotationRequestStatus status) { 
        this.status = status; 
    }

    public LocalDateTime getRequestDate() { 
        return requestDate; 
    }

    public void setRequestDate(LocalDateTime requestDate) { 
        this.requestDate = requestDate; 
    }

    public LocalDateTime getValidUntil() { 
        return validUntil; 
    }

    public void setValidUntil(LocalDateTime validUntil) { 
        this.validUntil = validUntil; 
    }

    public String getNotes() { 
        return notes; 
    }

    public void setNotes(String notes) { 
        this.notes = notes; 
    }
}
