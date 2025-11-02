package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for submitting a new quotation offer in the marketplace.
 * Handles validation, request verification, and offer creation.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class SubmitQuotationOfferUseCase {
    
    private final QuotationRepository quotationRepository;
    private final SupplierRepository supplierRepository;

    /**
     * Executes the quotation offer submission use case.
     *
     * @param command the submission command containing offer data
     * @return the result indicating success or failure with details
     */
    public SubmitQuotationOfferResult execute(SubmitQuotationOfferCommand command) {
        // Validate required fields
        if (command.getQuotationRequestId() == null) {
            return SubmitQuotationOfferResult.failure("Quotation request ID is required", "INVALID_REQUEST_ID");
        }
        if (command.getSupplierId() == null) {
            return SubmitQuotationOfferResult.failure("Supplier ID is required", "INVALID_SUPPLIER_ID");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            return SubmitQuotationOfferResult.failure("At least one offer item is required", "INVALID_ITEMS");
        }

        // Verify quotation request exists
        QuotationRequest request = quotationRepository.findRequestById(command.getQuotationRequestId());
        if (request == null) {
            return SubmitQuotationOfferResult.failure("Quotation request not found", "REQUEST_NOT_FOUND");
        }

        // Verify supplier exists
        if (supplierRepository.findById(command.getSupplierId()).isEmpty()) {
            return SubmitQuotationOfferResult.failure("Supplier not found", "SUPPLIER_NOT_FOUND");
        }

        // Generate unique offer number
        String offerNumber = generateOfferNumber();

        // Create quotation offer using builder
        QuotationOffer.Builder builder = QuotationOffer.builder()
                .offerNumber(offerNumber)
                .quotationRequestId(command.getQuotationRequestId())
                .retailerId(request.getRetailerId())
                .supplierId(command.getSupplierId())
                .validUntil(command.getValidUntil())
                .notes(command.getNotes())
                .termsAndConditions(command.getTermsAndConditions());

        // Add items
        command.getItems().forEach(item ->
                builder.addOfferItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getQuotedPrice(),
                        item.getSpecifications()
                )
        );

        // Build and submit the offer
        QuotationOffer offer = builder.build();
        offer.submit(); // validate and calculate total amount

        // Save to repository
        QuotationOffer savedOffer = quotationRepository.saveQuotationOffer(offer);

        // Update request status to indicate offer received
        request.markOfferReceived();
        quotationRepository.saveQuotationRequest(request);

        return SubmitQuotationOfferResult.success(
                savedOffer.getOfferNumber(),
                savedOffer.getId(),
                savedOffer.getTotalAmount()
        );
    }

    private String generateOfferNumber() {
        return "QO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
