package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.quotation.*;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.common.ErrorResponse;
import com.example.ecommerce.marketplace.web.model.quotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;

/**
 * REST controller for Quotation operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotation", description = "Quotation API")
public class QuotationController {

    private final CreateQuotationRequestUseCase createQuotationRequestUseCase;
    private final SubmitQuotationOfferUseCase submitQuotationOfferUseCase;
    private final ListQuotationRequestsUseCase listQuotationRequestsUseCase;
    private final GetQuotationRequestUseCase getQuotationRequestUseCase;
    private final UpdateQuotationRequestStatusUseCase updateQuotationRequestStatusUseCase;
    private final ListQuotationOffersUseCase listQuotationOffersUseCase;
    private final GetQuotationOfferUseCase getQuotationOfferUseCase;
    private final UpdateQuotationOfferStatusUseCase updateQuotationOfferStatusUseCase;

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
                request.getRequestItems().stream()
                        .map(item -> new CreateQuotationRequestCommand.RequestItem(
                                item.getProductId(),
                                item.getVariantId(),
                                item.getQuantity(),
                                item.getQuotedPrice(),
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
     * List quotation requests with pagination and filtering.
     * GET /api/v1/quotations/requests
     */
    @GetMapping("/requests")
    public ResponseEntity<QuotationRequestListResponse> listRequests(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {

        ListQuotationRequestsCommand command = ListQuotationRequestsCommand.create(
                retailerId, supplierId, status, page, size, sort);

        ListQuotationRequestsResult result = listQuotationRequestsUseCase.execute(command);

        if (result.isSuccess()) {
            QuotationRequestListResponse response = mapToRequestListResponse(result.getRequests());
            return ResponseEntity.ok(response);
        }

        HttpStatus httpStatus = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(httpStatus).build();
    }

    /**
     * Get quotation request by ID.
     * GET /api/v1/quotations/requests/{requestId}
     */
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<QuotationRequestDetailResponse> getRequest(@PathVariable Long requestId) {

        GetQuotationRequestResult result = getQuotationRequestUseCase.execute(requestId);

        if (result.isSuccess()) {
            QuotationRequestDetailResponse response = mapToRequestDetailResponse(result.getRequest());
            return ResponseEntity.ok(response);
        }

        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Update quotation request status.
     * PATCH /api/v1/quotations/requests/{requestId}
     */
    @PatchMapping("/requests/{requestId}")
    public ResponseEntity<QuotationRequestStatusResponse> updateRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody UpdateQuotationRequestStatusRequest request) {

        UpdateQuotationRequestStatusCommand command = new UpdateQuotationRequestStatusCommand(
                requestId, request.getStatus());

        UpdateQuotationRequestStatusResult result = updateQuotationRequestStatusUseCase.execute(command);

        if (result.isSuccess()) {
            QuotationRequestStatusResponse response = new QuotationRequestStatusResponse(
                    result.getRequestId(), result.getStatus(), result.getUpdatedAt());
            return ResponseEntity.ok(response);
        }

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
                request.getOfferItems().stream()
                        .map(item -> new SubmitQuotationOfferCommand.OfferItem(
                                item.getProductId(),
                                item.getVariantId(),
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
     * List quotation offers with pagination and filtering.
     * GET /api/v1/quotations/offers
     */
    @GetMapping("/offers")
    public ResponseEntity<QuotationOfferListResponse> listOffers(
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {

        ListQuotationOffersCommand command = ListQuotationOffersCommand.create(
                requestId, supplierId, status, page, size, sort);

        ListQuotationOffersResult result = listQuotationOffersUseCase.execute(command);

        if (result.isSuccess()) {
            QuotationOfferListResponse response = mapToOfferListResponse(result.getOffers());
            return ResponseEntity.ok(response);
        }

        HttpStatus status1 = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status1).build();
    }

    /**
     * Get quotation offer by ID.
     * GET /api/v1/quotations/offers/{offerId}
     */
    @GetMapping("/offers/{offerId}")
    public ResponseEntity<QuotationOfferDetailResponse> getOffer(@PathVariable Long offerId) {

        GetQuotationOfferResult result = getQuotationOfferUseCase.execute(offerId);

        if (result.isSuccess()) {
            QuotationOfferDetailResponse response = mapToOfferDetailResponse(result.getOffer());
            return ResponseEntity.ok(response);
        }

        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Update quotation offer status.
     * PATCH /api/v1/quotations/offers/{offerId}
     */
    @PatchMapping("/offers/{offerId}")
    public ResponseEntity<?> updateOfferStatus(
            @PathVariable Long offerId,
            @Valid @RequestBody UpdateQuotationOfferStatusRequest request) {

        UpdateQuotationOfferStatusCommand command = new UpdateQuotationOfferStatusCommand(
                offerId, request.getStatus());

        UpdateQuotationOfferStatusResult result = updateQuotationOfferStatusUseCase.execute(command);

        if (result.isSuccess()) {
            QuotationOfferStatusResponse response = new QuotationOfferStatusResponse(
                    result.getOfferId(), result.getStatus(), result.getUpdatedAt());
            return ResponseEntity.ok(response);
        }

        // Return detailed error message
        ErrorResponse errorResponse = ErrorResponse.of(
                400,
                "BAD_REQUEST",
                result.getErrorMessage(),
                "/api/v1/quotations/offers/" + offerId
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Private mapping methods
    private QuotationRequestListResponse mapToRequestListResponse(Page<QuotationRequest> requests) {
        QuotationRequestListResponse response = new QuotationRequestListResponse();
        
        response.setContent(requests.getContent().stream()
                .map(this::mapToRequestSummary)
                .collect(Collectors.toList()));
        
        response.setPage(new QuotationRequestListResponse.PageInfo(
                requests.getSize(),
                requests.getNumber(),
                requests.getTotalElements(),
                requests.getTotalPages()
        ));
        
        return response;
    }

    private QuotationRequestListResponse.QuotationRequestSummary mapToRequestSummary(QuotationRequest request) {
        return new QuotationRequestListResponse.QuotationRequestSummary(
                request.getId(),
                request.getRequestNumber(),
                request.getRetailerId(),
                request.getSupplierId(),
                mapDomainStatusToApiStatus(request.getStatus().toString()),
                request.getValidUntil(),
                request.getCreatedAt()
        );
    }

    private QuotationRequestDetailResponse mapToRequestDetailResponse(QuotationRequest request) {
        QuotationRequestDetailResponse response = new QuotationRequestDetailResponse();
        response.setRequestId(request.getId());
        response.setRequestNumber(request.getRequestNumber());
        response.setRetailerId(request.getRetailerId());
        response.setSupplierId(request.getSupplierId());
        response.setStatus(mapDomainStatusToApiStatus(request.getStatus().toString()));
        response.setValidUntil(request.getValidUntil());
        response.setNotes(request.getNotes());
        response.setCreatedAt(request.getCreatedAt());
        
        response.setRequestItems(request.getRequestItems().stream()
                .map(item -> new QuotationRequestDetailResponse.QuotationRequestItemDetail(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getSpecifications()
                ))
                .collect(Collectors.toList()));
        
        return response;
    }

    private QuotationOfferListResponse mapToOfferListResponse(Page<QuotationOffer> offers) {
        QuotationOfferListResponse response = new QuotationOfferListResponse();
        
        response.setContent(offers.getContent().stream()
                .map(this::mapToOfferSummary)
                .collect(Collectors.toList()));
        
        response.setPage(new QuotationRequestListResponse.PageInfo(
                offers.getSize(),
                offers.getNumber(),
                offers.getTotalElements(),
                offers.getTotalPages()
        ));
        
        return response;
    }

    private QuotationOfferListResponse.QuotationOfferSummary mapToOfferSummary(QuotationOffer offer) {
        return new QuotationOfferListResponse.QuotationOfferSummary(
                offer.getId(),
                offer.getOfferNumber(),
                offer.getQuotationRequestId(),
                offer.getSupplierId(),
                offer.getTotalAmount(),
                offer.getStatus().toString(),
                offer.getValidUntil(),
                offer.getCreatedAt()
        );
    }

    private QuotationOfferDetailResponse mapToOfferDetailResponse(QuotationOffer offer) {
        QuotationOfferDetailResponse response = new QuotationOfferDetailResponse();
        response.setOfferId(offer.getId());
        response.setOfferNumber(offer.getOfferNumber());
        response.setQuotationRequestId(offer.getQuotationRequestId());
        response.setSupplierId(offer.getSupplierId());
        response.setStatus(offer.getStatus().toString());
        response.setTotalAmount(offer.getTotalAmount());
        response.setValidUntil(offer.getValidUntil());
        response.setNotes(offer.getNotes());
        response.setTermsAndConditions(offer.getTermsAndConditions());
        response.setCreatedAt(offer.getCreatedAt());
        
        response.setOfferItems(offer.getOfferItems().stream()
                .map(item -> new QuotationOfferDetailResponse.QuotationOfferItemDetail(
                        item.getProductId(),
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getQuotedPrice(),
                        item.getSpecifications(),
                        item.getNotes()
                ))
                .collect(Collectors.toList()));
        
        return response;
    }

    private String mapDomainStatusToApiStatus(String domainStatus) {
        // Map domain statuses to API statuses for requests
        switch (domainStatus) {
            case "PENDING":
                return "PENDING";
            case "REQUEST_RECEIVED":
                return "RECEIVED";
            case "OFFERS_SENT":
                return "OFFERS_SENT";
            case "CANCELLED":
                return "CANCELLED";
            case "EXPIRED":
                return "EXPIRED";
            default:
                return domainStatus; // fallback for other statuses
        }
    }
}
