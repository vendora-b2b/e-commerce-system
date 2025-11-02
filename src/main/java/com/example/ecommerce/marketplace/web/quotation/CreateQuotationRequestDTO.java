package com.example.ecommerce.marketplace.web.quotation;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class CreateQuotationRequestDTO {
    @NotNull(message = "Retailer ID is required")
    private Long retailerId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<RequestItemDTO> items;

    @NotNull(message = "Validity period is required")
    @Future(message = "Validity period must be in the future")
    private LocalDateTime validUntil;

    private String notes;

    // Getters and Setters
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

    public List<RequestItemDTO> getItems() { 
        return items; 
    }

    public void setItems(List<RequestItemDTO> items) { 
        this.items = items; 
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

    public static class RequestItemDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        private Integer quantity;

        private String specifications;

        // Getters and Setters
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
}
