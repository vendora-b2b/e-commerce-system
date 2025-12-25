package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.application.quotation.RespondToQuotationCommand;
import com.example.ecommerce.marketplace.domain.quotation.QuotationItemStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class RespondToQuotationRequest {
    
    @NotEmpty(message = "Items are required")
    @Valid
    private List<ItemResponse> items;
    
    private LocalDateTime validUntil;
    private String supplierNotes;
    private String termsAndConditions;
    
    public RespondToQuotationCommand toCommand(Long quotationId) {
        List<RespondToQuotationCommand.ItemResponse> commandItems = items.stream()
                .map(item -> new RespondToQuotationCommand.ItemResponse(
                        item.getQuotationItemId(),
                        item.getItemStatus(),
                        item.getOfferedQuantity(),
                        item.getOfferedPrice(),
                        item.getOfferedDeliveryDate(),
                        item.getLeadTimeDays(),
                        item.getSupplierNotes(),
                        item.getRejectionReason()
                ))
                .collect(Collectors.toList());
        
        return new RespondToQuotationCommand(quotationId, commandItems, validUntil, supplierNotes, termsAndConditions);
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        @NotNull(message = "Quotation item ID is required")
        private Long quotationItemId;
        
        @NotNull(message = "Item status is required")
        private QuotationItemStatus itemStatus;
        
        private Integer offeredQuantity;
        private Double offeredPrice;
        private LocalDate offeredDeliveryDate;
        private Integer leadTimeDays;
        private String supplierNotes;
        private String rejectionReason;
    }
}
