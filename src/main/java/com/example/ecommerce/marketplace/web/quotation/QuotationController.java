package com.example.ecommerce.marketplace.web.quotation;

import com.example.ecommerce.marketplace.application.quotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {
    private final CreateQuotationRequestUseCase createQuotationRequestUseCase;
    private final SubmitQuotationOfferUseCase submitQuotationOfferUseCase;

    public QuotationController(
            CreateQuotationRequestUseCase createQuotationRequestUseCase,
            SubmitQuotationOfferUseCase submitQuotationOfferUseCase) {
        this.createQuotationRequestUseCase = createQuotationRequestUseCase;
        this.submitQuotationOfferUseCase = submitQuotationOfferUseCase;
    }

    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(
            @Valid @RequestBody CreateQuotationRequestDTO request) {
        
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

        CreateQuotationRequestResult result = createQuotationRequestUseCase.execute(command);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/offers")
    public ResponseEntity<?> submitOffer(
            @Valid @RequestBody SubmitQuotationOfferDTO offer) {
        
        SubmitQuotationOfferCommand command = new SubmitQuotationOfferCommand(
                offer.getQuotationRequestId(),
                offer.getSupplierId(),
                offer.getItems().stream()
                        .map(item -> new SubmitQuotationOfferCommand.OfferItem(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getQuotedPrice(),
                                item.getSpecifications(),
                                item.getNotes()))
                        .collect(Collectors.toList()),
                offer.getValidUntil(),
                offer.getNotes(),
                offer.getTermsAndConditions()
        );

        SubmitQuotationOfferResult result = submitQuotationOfferUseCase.execute(command);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long requestId) {
        // TODO: Implement accept request use case
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<Void> cancelRequest(@PathVariable Long requestId) {
        // TODO: Implement cancel request use case
        return ResponseEntity.ok().build();
    }

    @PostMapping("/offers/{offerId}/accept")
    public ResponseEntity<Void> acceptOffer(@PathVariable Long offerId) {
        // TODO: Implement accept offer use case
        return ResponseEntity.ok().build();
    }

    @PostMapping("/offers/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId) {
        // TODO: Implement reject offer use case
        return ResponseEntity.ok().build();
    }

    @PostMapping("/offers/{offerId}/withdraw")
    public ResponseEntity<Void> withdrawOffer(@PathVariable Long offerId) {
        // TODO: Implement withdraw offer use case
        return ResponseEntity.ok().build();
    }
}
