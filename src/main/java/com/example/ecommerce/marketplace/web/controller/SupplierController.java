package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierCommand;
import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierResult;
import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierUseCase;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileCommand;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileResult;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileUseCase;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.supplier.RegisterSupplierRequest;
import com.example.ecommerce.marketplace.web.model.supplier.SupplierResponse;
import com.example.ecommerce.marketplace.web.model.supplier.UpdateSupplierRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;

/**
 * REST controller for Supplier operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "Supplier API")
public class SupplierController {

    private final RegisterSupplierUseCase registerSupplierUseCase;
    private final UpdateSupplierProfileUseCase updateSupplierProfileUseCase;
    private final SupplierRepository supplierRepository;

    /**
     * Register a new supplier.
     * POST /api/v1/suppliers
     */
    @PostMapping
    public ResponseEntity<SupplierResponse> registerSupplier(
        @Valid @RequestBody RegisterSupplierRequest request
    ) {
        // Convert request to command
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getProfilePicture(),
            request.getProfileDescription(),
            request.getBusinessLicense()
        );

        // Execute use case
        RegisterSupplierResult result = registerSupplierUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the created supplier to return full details
            Optional<Supplier> supplier = supplierRepository.findById(result.getSupplierId());
            if (supplier.isPresent()) {
                SupplierResponse response = SupplierResponse.fromDomain(supplier.get());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Get supplier by ID.
     * GET /api/v1/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);

        if (supplier.isPresent()) {
            SupplierResponse response = SupplierResponse.fromDomain(supplier.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Update supplier profile.
     * PUT /api/v1/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
        @PathVariable Long id,
        @Valid @RequestBody UpdateSupplierRequest request
    ) {
        // Convert request to command
        UpdateSupplierProfileCommand command = new UpdateSupplierProfileCommand(
            id,
            request.getName(),
            request.getPhone(),
            request.getAddress(),
            request.getProfileDescription()
        );

        // Execute use case
        UpdateSupplierProfileResult result = updateSupplierProfileUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated supplier to return full details
            Optional<Supplier> supplier = supplierRepository.findById(result.getSupplierId());
            if (supplier.isPresent()) {
                SupplierResponse response = SupplierResponse.fromDomain(supplier.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
