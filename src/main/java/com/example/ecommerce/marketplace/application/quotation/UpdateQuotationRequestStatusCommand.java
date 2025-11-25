package com.example.ecommerce.marketplace.application.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command for updating quotation request status.
 */
@Getter
@AllArgsConstructor
public class UpdateQuotationRequestStatusCommand {
    private final Long requestId;
    private final String status;
}