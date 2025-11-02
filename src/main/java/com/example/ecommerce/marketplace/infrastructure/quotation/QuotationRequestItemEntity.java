package com.example.ecommerce.marketplace.infrastructure.quotation;

import jakarta.persistence.*;

@Entity
@Table(name = "quotation_request_items")
public class QuotationRequestItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;
    private Integer quantity;
    private String specifications;
    
    // Getters and Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Long getProductId() { 
        return productId; 
    }

    public void setProductId(Long productId) { 
        this.productId = productId; 
    }

    public Integer getQuantity() { 
        return quantity; 
    }

    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
    }

    public String getSpecifications() { 
        return specifications; 
    }

    public void setSpecifications(String specifications) { 
        this.specifications = specifications; 
    }
}
