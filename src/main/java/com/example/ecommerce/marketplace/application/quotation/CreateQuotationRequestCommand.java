package com.example.ecommerce.marketplace.application.quotation;

import java.time.LocalDateTime;
import java.util.List;

public class CreateQuotationRequestCommand {
    private final Long retailerId;
    private final Long supplierId;
    private final List<RequestItem> items;
    private final LocalDateTime validUntil;
    private final String notes;

    public CreateQuotationRequestCommand(Long retailerId, Long supplierId, List<RequestItem> items,
                                        LocalDateTime validUntil, String notes) {
        this.retailerId = retailerId;
        this.supplierId = supplierId;
        this.items = items;
        this.validUntil = validUntil;
        this.notes = notes;
    }

    public Long getRetailerId() { 
        return retailerId; 
    }

    public Long getSupplierId() { 
        return supplierId; 
    }

    public List<RequestItem> getItems() { 
        return items; 
    }

    public LocalDateTime getValidUntil() { 
        return validUntil; 
    }

    public String getNotes() { 
        return notes; 
    }

    public static class RequestItem {
        private final Long productId;
        private final Integer quantity;
        private final String specifications;

        public RequestItem(Long productId, Integer quantity, String specifications) {
            this.productId = productId;
            this.quantity = quantity;
            this.specifications = specifications;
        }

        public Long getProductId() { 
            return productId; 
        }

        public Integer getQuantity() { 
            return quantity; 
        }

        public String getSpecifications() { 
            return specifications; 
        }
    }
}
