package com.example.ecommerce.marketplace.web.quotation;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class SubmitQuotationOfferDTO {
    @NotNull(message = "Quotation Request ID is required")
    private Long quotationRequestId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OfferItemDTO> items;

    @NotNull(message = "Validity period is required")
    @Future(message = "Validity period must be in the future")
    private LocalDateTime validUntil;

    private String notes;

    @NotBlank(message = "Terms and conditions are required")
    private String termsAndConditions;

    // Getters and Setters
    public Long getQuotationRequestId() { return quotationRequestId; }
    public void setQuotationRequestId(Long quotationRequestId) { this.quotationRequestId = quotationRequestId; }
    
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    
    public List<OfferItemDTO> getItems() { return items; }
    public void setItems(List<OfferItemDTO> items) { this.items = items; }
    
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public static class OfferItemDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        private Integer quantity;

        @NotNull(message = "Quoted price is required")
        @DecimalMin(value = "0.01", message = "Quoted price must be greater than zero")
        private Double quotedPrice;

        private String specifications;
        private String notes;

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

        public Double getQuotedPrice() { 
            return quotedPrice; 
        }

        public void setQuotedPrice(Double quotedPrice) { 
            this.quotedPrice = quotedPrice; 
        }

        public String getSpecifications() { 
            return specifications; 
        }

        public void setSpecifications(String specifications) { 
            this.specifications = specifications; 
        }

        public String getNotes() { 
            return notes; 
        }
        
        public void setNotes(String notes) { 
            this.notes = notes; 
        }
    }
}
