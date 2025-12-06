package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDetailResponse {
    
    private Long quotationId;
    private String quotationNumber;
    private Long retailerId;
    private String retailerName;
    private Long supplierId;
    private String supplierName;
    private String status;
    private List<QuotationItemDetail> items;
    private String retailerNotes;
    private String supplierNotes;
    private String termsAndConditions;
    private LocalDateTime validUntil;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime finalizedAt;
    
    public static QuotationDetailResponse from(Quotation quotation,
                                               SupplierRepository supplierRepository,
                                               RetailerRepository retailerRepository) {
        String supplierName = supplierRepository.findById(quotation.getSupplierId())
                .map(s -> s.getName())
                .orElse("Unknown Supplier");
        
        String retailerName = retailerRepository.findById(quotation.getRetailerId())
                .map(r -> r.getName())
                .orElse("Unknown Retailer");
        
        List<QuotationItemDetail> items = quotation.getItems().stream()
                .map(QuotationItemDetail::from)
                .collect(Collectors.toList());
        
        return new QuotationDetailResponse(
                quotation.getId(),
                quotation.getQuotationNumber(),
                quotation.getRetailerId(),
                retailerName,
                quotation.getSupplierId(),
                supplierName,
                quotation.getStatus().name(),
                items,
                quotation.getRetailerNotes(),
                quotation.getSupplierNotes(),
                quotation.getTermsAndConditions(),
                quotation.getValidUntil(),
                quotation.getOrderId(),
                quotation.getCreatedAt(),
                quotation.getRespondedAt(),
                quotation.getFinalizedAt()
        );
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationItemDetail {
        private Long quotationItemId;
        private Long variantId;
        private Long productId;
        private String productName;
        private String variantName;
        private String imageUrl;
        
        // Retailer's request
        private Integer requestedQuantity;
        private Double targetPrice;
        private LocalDate requestedDeliveryDate;
        private String retailerNotes;
        
        // Supplier's response
        private String itemStatus;
        private Integer offeredQuantity;
        private Double offeredPrice;
        private LocalDate offeredDeliveryDate;
        private Integer leadTimeDays;
        private String supplierNotes;
        private String rejectionReason;
        
        // Retailer's final decision
        private String retailerAction;
        
        public static QuotationItemDetail from(Quotation.QuotationItem item) {
            return new QuotationItemDetail(
                    item.getId(),
                    item.getVariantId(),
                    item.getProductId(),
                    "Product Name",  // Would fetch from repository
                    "Variant Name",  // Would fetch from repository
                    null,
                    item.getRequestedQuantity(),
                    item.getTargetPrice(),
                    item.getRequestedDeliveryDate(),
                    item.getRetailerNotes(),
                    item.getItemStatus() != null ? item.getItemStatus().name() : null,
                    item.getOfferedQuantity(),
                    item.getOfferedPrice(),
                    item.getOfferedDeliveryDate(),
                    item.getLeadTimeDays(),
                    item.getSupplierNotes(),
                    item.getRejectionReason(),
                    item.getRetailerAction() != null ? item.getRetailerAction().name() : null
            );
        }
    }
}
