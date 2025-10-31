package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

public class CreateQuotationRequestUseCase {
    private final QuotationRepository quotationRepository;

    public CreateQuotationRequestUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public CreateQuotationRequestResult execute(CreateQuotationRequestCommand command) {
        // Validate command
        if (command.getRetailerId() == null) {
            throw new IllegalStateException("Retailer ID is required");
        }
        if (command.getSupplierId() == null) {
            throw new IllegalStateException("Supplier ID is required");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalStateException("At least one request item is required");
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

        // Add items if present
        if (command.getItems() != null) {
            command.getItems().forEach(item ->
                    builder.addRequestItem(
                            item.getProductId(),
                            item.getQuantity(),
                            item.getSpecifications()
                    )
            );
        }

        // Build the request now that all items are added
        QuotationRequest request = builder.build();

        // Save to repository
        QuotationRequest savedRequest = quotationRepository.saveQuotationRequest(request);

        return new CreateQuotationRequestResult(
                savedRequest.getRequestNumber(),
                savedRequest.getId()
        );
    }

    private String generateRequestNumber() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}