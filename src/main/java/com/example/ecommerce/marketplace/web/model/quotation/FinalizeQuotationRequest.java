package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.application.quotation.FinalizeQuotationCommand;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeQuotationRequest {
    
    @NotEmpty(message = "Items are required")
    @Valid
    private List<ItemDecision> items;
    
    private boolean createOrder;
    private String notes;
    
    public FinalizeQuotationCommand toCommand(Long quotationId) {
        List<FinalizeQuotationCommand.ItemDecision> commandItems = items.stream()
                .map(item -> new FinalizeQuotationCommand.ItemDecision(
                        item.getQuotationItemId(),
                        item.getRetailerAction()
                ))
                .collect(Collectors.toList());
        
        return new FinalizeQuotationCommand(quotationId, commandItems, createOrder, notes);
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDecision {
        @NotNull(message = "Quotation item ID is required")
        private Long quotationItemId;
        
        @NotNull(message = "Retailer action is required")
        private RetailerAction retailerAction;
    }
}
