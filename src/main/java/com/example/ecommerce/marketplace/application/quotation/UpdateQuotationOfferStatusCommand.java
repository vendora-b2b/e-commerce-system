package com.example.ecommerce.marketplace.application.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command for updating quotation offer status.
 */
@Getter
@AllArgsConstructor
public class UpdateQuotationOfferStatusCommand {
    private final Long offerId;
    private final String status;
}