package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.quotation.*;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.quotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for Quotation operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final CreateQuotationRequestUseCase createQuotationRequestUseCase;
    private final SubmitQuotationOfferUseCase submitQuotationOfferUseCase;

    /**
     * Create a new quotation request.
     * POST /api/v1/quotations/requests
     */
    @PostMapping("/requests")
    public ResponseEntity<QuotationRequestResponse> createRequest(
            @Valid @RequestBody CreateQuotationRequest request) {

        // Convert request to command
        CreateQuotationRequestCommand command = new CreateQuotationRequestCommand(
                request.getRetailerId(),
                request.getSupplierId(),
                request.getItems().stream()
                        .map(item -> new CreateQuotationRequestCommand.RequestItem(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getSpecifications()))
                        .collect(Collectors.toList()),
                request.getValidUntil(),
                request.getNotes()
        );

        // Execute use case
        CreateQuotationRequestResult result = createQuotationRequestUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            QuotationRequestResponse response = QuotationRequestResponse.success(result.getRequestId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Submit a quotation offer.
     * POST /api/v1/quotations/offers
     */
    @PostMapping("/offers")
    public ResponseEntity<QuotationOfferResponse> submitOffer(
            @Valid @RequestBody SubmitQuotationOfferRequest request) {

        // Convert request to command
        SubmitQuotationOfferCommand command = new SubmitQuotationOfferCommand(
                request.getQuotationRequestId(),
                request.getSupplierId(),
                request.getItems().stream()
                        .map(item -> new SubmitQuotationOfferCommand.OfferItem(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getQuotedPrice(),
                                item.getSpecifications(),
                                item.getNotes()))
                        .collect(Collectors.toList()),
                request.getValidUntil(),
                request.getNotes(),
                request.getTermsAndConditions()
        );

        // Execute use case
        SubmitQuotationOfferResult result = submitQuotationOfferUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            QuotationOfferResponse response = QuotationOfferResponse.success(result.getOfferId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Accept a quotation request (placeholder for future implementation).
     * POST /api/v1/quotations/requests/{requestId}/accept
     */
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long requestId) {
        // TODO: Implement accept request use case
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel a quotation request (placeholder for future implementation).
     * POST /api/v1/quotations/requests/{requestId}/cancel
     */
    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<Void> cancelRequest(@PathVariable Long requestId) {
        // TODO: Implement cancel request use case
        return ResponseEntity.ok().build();
    }

    /**
     * Accept a quotation offer (placeholder for future implementation).
     * POST /api/v1/quotations/offers/{offerId}/accept
     */
    @PostMapping("/offers/{offerId}/accept")
    public ResponseEntity<Void> acceptOffer(@PathVariable Long offerId) {
        // TODO: Implement accept offer use case
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a quotation offer (placeholder for future implementation).
     * POST /api/v1/quotations/offers/{offerId}/reject
     */
    @PostMapping("/offers/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId) {
        // TODO: Implement reject offer use case
        return ResponseEntity.ok().build();
    }

    /**
     * Withdraw a quotation offer (placeholder for future implementation).
     * POST /api/v1/quotations/offers/{offerId}/withdraw
     */
    @PostMapping("/offers/{offerId}/withdraw")
    public ResponseEntity<Void> withdrawOffer(@PathVariable Long offerId) {
        // TODO: Implement withdraw offer use case
        return ResponseEntity.ok().build();
    }
}
