package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class SubmitQuotationOfferUseCase {
    private final QuotationRepository quotationRepository;

    public SubmitQuotationOfferUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public SubmitQuotationOfferResult execute(SubmitQuotationOfferCommand command) {
        // Verify quotation request exists
        QuotationRequest request = quotationRepository.findRequestById(command.getQuotationRequestId());
        if (request == null) {
            throw new IllegalArgumentException("Quotation request not found");
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
        offer.submit(); // This will validate and calculate total amount

        // Save to repository
        QuotationOffer savedOffer = quotationRepository.saveQuotationOffer(offer);

        // Update request status to indicate offer received
        request.markOfferReceived();
        quotationRepository.saveQuotationRequest(request);

        return new SubmitQuotationOfferResult(
                savedOffer.getOfferNumber(),
                savedOffer.getId(),
                savedOffer.getTotalAmount()
        );
    }

    private String generateOfferNumber() {
        return "QO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
