package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.retailer.*;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.retailer.ManageLoyaltyPointsRequest;
import com.example.ecommerce.marketplace.web.model.retailer.RegisterRetailerRequest;
import com.example.ecommerce.marketplace.web.model.retailer.RetailerResponse;
import com.example.ecommerce.marketplace.web.model.retailer.UpdateRetailerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for Retailer operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/retailers")
@RequiredArgsConstructor
public class RetailerController {

    private final RegisterRetailerUseCase registerRetailerUseCase;
    private final UpdateRetailerProfileUseCase updateRetailerProfileUseCase;
    private final ManageLoyaltyPointsUseCase manageLoyaltyPointsUseCase;
    private final RetailerRepository retailerRepository;

    /**
     * Register a new retailer.
     * POST /api/v1/retailers
     */
    @PostMapping
    public ResponseEntity<RetailerResponse> registerRetailer(
        @Valid @RequestBody RegisterRetailerRequest request
    ) {
        // Convert request to command
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getProfilePicture(),
            request.getProfileDescription(),
            request.getBusinessLicense(),
            request.getCreditLimit()
        );

        // Execute use case
        RegisterRetailerResult result = registerRetailerUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the created retailer to return full details
            Optional<Retailer> retailer = retailerRepository.findById(result.getRetailerId());
            if (retailer.isPresent()) {
                RetailerResponse response = RetailerResponse.fromDomain(retailer.get());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Get retailer by ID.
     * GET /api/v1/retailers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RetailerResponse> getRetailer(@PathVariable Long id) {
        Optional<Retailer> retailer = retailerRepository.findById(id);

        if (retailer.isPresent()) {
            RetailerResponse response = RetailerResponse.fromDomain(retailer.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Update retailer profile.
     * PUT /api/v1/retailers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RetailerResponse> updateRetailer(
        @PathVariable Long id,
        @Valid @RequestBody UpdateRetailerRequest request
    ) {
        // Convert request to command
        UpdateRetailerProfileCommand command = new UpdateRetailerProfileCommand(
            id,
            request.getName(),
            request.getPhone(),
            request.getAddress(),
            request.getProfileDescription()
        );

        // Execute use case
        UpdateRetailerProfileResult result = updateRetailerProfileUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated retailer to return full details
            Optional<Retailer> retailer = retailerRepository.findById(result.getRetailerId());
            if (retailer.isPresent()) {
                RetailerResponse response = RetailerResponse.fromDomain(retailer.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Manage loyalty points (add or redeem).
     * POST /api/v1/retailers/{id}/loyalty-points
     */
    @PostMapping("/{id}/loyalty-points")
    public ResponseEntity<RetailerResponse> manageLoyaltyPoints(
        @PathVariable Long id,
        @Valid @RequestBody ManageLoyaltyPointsRequest request
    ) {
        // Convert request operation type to command operation type
        ManageLoyaltyPointsCommand.OperationType operationType;
        if (request.getOperationType() == ManageLoyaltyPointsRequest.OperationType.ADD) {
            operationType = ManageLoyaltyPointsCommand.OperationType.ADD;
        } else {
            operationType = ManageLoyaltyPointsCommand.OperationType.REDEEM;
        }

        // Convert request to command
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            id,
            request.getPoints(),
            operationType
        );

        // Execute use case
        ManageLoyaltyPointsResult result = manageLoyaltyPointsUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated retailer to return full details
            Optional<Retailer> retailer = retailerRepository.findById(result.getRetailerId());
            if (retailer.isPresent()) {
                RetailerResponse response = RetailerResponse.fromDomain(retailer.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
