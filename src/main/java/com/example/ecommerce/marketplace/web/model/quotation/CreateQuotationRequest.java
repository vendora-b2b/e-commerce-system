package com.example.ecommerce.marketplace.web.model.quotation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP request DTO for creating a quotation request.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuotationRequest {

    @NotNull(message = "You must be logged in as a retailer to request a quotation")
    private Long retailerId;

    @NotNull(message = "Please select a supplier for this quotation request")
    private Long supplierId;

    @NotEmpty(message = "Please add at least one product to your quotation request")
    @Valid
    private List<QuotationRequestItem> requestItems;

    @Future(message = "The validity date must be in the future")
    private LocalDateTime validUntil;

    private String notes;

    /**
     * Nested class for quotation request items.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationRequestItem {

        @NotNull(message = "Please select a product for this quotation item")
        private Long productId;
        
        private Long variantId;

        @NotNull(message = "Please specify the quantity you need")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Please enter your desired price for this item")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        private Double quotedPrice;

        private String specifications;
    }
}
