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
 * HTTP request DTO for submitting a quotation offer.
 * Contains validation constraints at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuotationOfferRequest {

    @NotNull(message = "Quotation Request ID is required")
    private Long quotationRequestId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<QuotationOfferItem> offerItems;

    @Future(message = "Validity period must be in the future")
    private LocalDateTime validUntil;

    private String notes;

    private String termsAndConditions;

    /**
     * Nested class for quotation offer items.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationOfferItem {

        @NotNull(message = "Product ID is required")
        private Long productId;
        
        private Long variantId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        private Integer quantity;

        @NotNull(message = "Quoted price is required")
        @DecimalMin(value = "0.01", message = "Quoted price must be greater than zero")
        private Double quotedPrice;

        private String specifications;
        private String notes;
    }
}
