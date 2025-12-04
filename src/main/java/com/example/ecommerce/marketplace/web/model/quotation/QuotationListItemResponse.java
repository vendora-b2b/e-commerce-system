package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationListItemResponse {
    
    private Long quotationId;
    private String quotationNumber;
    private Long retailerId;
    private String retailerName;
    private Long supplierId;
    private String supplierName;
    private int itemCount;
    private double totalEstimatedAmount;
    private String status;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    
    public static QuotationListItemResponse from(Quotation quotation, 
                                                 SupplierRepository supplierRepository,
                                                 RetailerRepository retailerRepository) {
        String supplierName = supplierRepository.findById(quotation.getSupplierId())
                .map(s -> s.getName())
                .orElse("Unknown Supplier");
        
        String retailerName = retailerRepository.findById(quotation.getRetailerId())
                .map(r -> r.getName())
                .orElse("Unknown Retailer");
        
        return new QuotationListItemResponse(
                quotation.getId(),
                quotation.getQuotationNumber(),
                quotation.getRetailerId(),
                retailerName,
                quotation.getSupplierId(),
                supplierName,
                quotation.getItemCount(),
                quotation.getTotalEstimatedAmount(),
                quotation.getStatus().name(),
                quotation.getValidUntil(),
                quotation.getCreatedAt(),
                quotation.getRespondedAt()
        );
    }
}
