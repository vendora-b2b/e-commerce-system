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

    @NotNull(message = "Please specify which quotation request you are responding to")
    private Long quotationRequestId;

    @NotNull(message = "You must be logged in as a supplier to submit an offer")
    private Long supplierId;

    @NotEmpty(message = "Please add at least one product to your offer")
    @Valid
    private List<QuotationOfferItem> offerItems;

    @Future(message = "The validity date must be in the future")
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

        @NotNull(message = "Please select a product for this offer item")
        private Long productId;
        
        private Long variantId;

        @NotNull(message = "Please specify the quantity you can provide")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Please enter your quoted price for this item")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        private Double quotedPrice;

        private String specifications;
        private String notes;
    }
}
