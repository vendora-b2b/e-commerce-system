package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for creating a new quotation request in the marketplace.
 * Handles validation, supplier/retailer checks, and initial request setup.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class CreateQuotationRequestUseCase {
    
    private final QuotationRepository quotationRepository;
    private final RetailerRepository retailerRepository;
    private final SupplierRepository supplierRepository;

    /**
     * Executes the quotation request creation use case.
     *
     * @param command the creation command containing request data
     * @return the result indicating success or failure with details
     */
    public CreateQuotationRequestResult execute(CreateQuotationRequestCommand command) {
        // Validate required fields
        if (command.getRetailerId() == null) {
            return CreateQuotationRequestResult.failure("Retailer ID is required", "INVALID_RETAILER_ID");
        }
        if (command.getSupplierId() == null) {
            return CreateQuotationRequestResult.failure("Supplier ID is required", "INVALID_SUPPLIER_ID");
        }
        if (command.getRequestItems() == null || command.getRequestItems().isEmpty()) {
            return CreateQuotationRequestResult.failure("At least one request item is required", "INVALID_ITEMS");
        }

        // Verify retailer exists
        if (retailerRepository.findById(command.getRetailerId()).isEmpty()) {
            return CreateQuotationRequestResult.failure("Retailer not found", "RETAILER_NOT_FOUND");
        }

        // Verify supplier exists
        if (supplierRepository.findById(command.getSupplierId()).isEmpty()) {
            return CreateQuotationRequestResult.failure("Supplier not found", "SUPPLIER_NOT_FOUND");
        }

        // Generate unique request number
        String requestNumber = generateRequestNumber();

        // Create quotation request using builder
        QuotationRequest.Builder builder = QuotationRequest.builder()
                .requestNumber(requestNumber)
                .retailerId(command.getRetailerId())
                .supplierId(command.getSupplierId())
                .validUntil(command.getValidUntil())
                .notes(command.getNotes());

        // Add items
        command.getRequestItems().forEach(item ->
                builder.addRequestItem(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getQuotedPrice(),
                        item.getSpecifications()
                )
        );

        // Build and save the request
        QuotationRequest request = builder.build();
        QuotationRequest savedRequest = quotationRepository.saveQuotationRequest(request);

        return CreateQuotationRequestResult.success(
                savedRequest.getRequestNumber(),
                savedRequest.getId()
        );
    }

    private String generateRequestNumber() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
