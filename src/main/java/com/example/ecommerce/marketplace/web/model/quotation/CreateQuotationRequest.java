package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.application.quotation.CreateQuotationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuotationRequest {
    
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<QuotationItemRequest> items;
    
    private String notes;
    
    public CreateQuotationCommand toCommand() {
        List<CreateQuotationCommand.QuotationItemRequest> commandItems = items.stream()
                .map(item -> new CreateQuotationCommand.QuotationItemRequest(
                        item.getVariantId(),
                        item.getRequestedQuantity(),
                        item.getTargetPrice(),
                        item.getDeliveryDate(),
                        item.getNotes()
                ))
                .collect(Collectors.toList());
        
        return new CreateQuotationCommand(commandItems, notes);
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationItemRequest {
        @NotNull(message = "Variant ID is required")
        private Long variantId;
        
        @NotNull(message = "Requested quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer requestedQuantity;
        
        private Double targetPrice;
        private LocalDate deliveryDate;
        private String notes;
    }
}
