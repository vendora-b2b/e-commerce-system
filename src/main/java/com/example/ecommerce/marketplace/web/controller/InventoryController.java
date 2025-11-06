package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.inventory.*;
import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.inventory.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Inventory operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory API")
public class InventoryController {

    private final RestockInventoryUseCase restockInventoryUseCase;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final ReleaseInventoryUseCase releaseInventoryUseCase;
    private final DeductInventoryUseCase deductInventoryUseCase;
    private final CheckInventoryAvailabilityUseCase checkInventoryAvailabilityUseCase;
    private final InventoryRepository inventoryRepository;

    /**
     * Get inventory for a specific product.
     * GET /api/v1/products/{productId}/inventory
     */
    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<InventoryResponse> getInventoryByProduct(@PathVariable Long productId) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);

        if (inventory.isPresent()) {
            InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get all inventory items for a supplier.
     * GET /api/v1/suppliers/{supplierId}/inventory
     */
    @GetMapping("/suppliers/{supplierId}/inventory")
    public ResponseEntity<List<InventoryResponse>> getInventoryBySupplier(@PathVariable Long supplierId) {
        List<Inventory> inventories = inventoryRepository.findBySupplierId(supplierId);
        List<InventoryResponse> responses = inventories.stream()
            .map(InventoryResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get inventory items needing reorder for a supplier.
     * GET /api/v1/suppliers/{supplierId}/inventory/reorder
     */
    @GetMapping("/suppliers/{supplierId}/inventory/reorder")
    public ResponseEntity<List<InventoryResponse>> getInventoryNeedingReorderBySupplier(@PathVariable Long supplierId) {
        List<Inventory> inventories = inventoryRepository.findInventoryNeedingReorderBySupplierId(supplierId);
        List<InventoryResponse> responses = inventories.stream()
            .map(InventoryResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Check inventory availability for a product.
     * GET /api/v1/products/{productId}/inventory/availability
     */
    @GetMapping("/products/{productId}/inventory/availability")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(
            productId,
            quantity
        );

        // Execute use case
        CheckInventoryAvailabilityResult result = checkInventoryAvailabilityUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            CheckAvailabilityResponse response = CheckAvailabilityResponse.fromResult(result);
            return ResponseEntity.ok(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Restock inventory for a product.
     * POST /api/v1/products/{productId}/inventory/restock
     */
    @PostMapping("/products/{productId}/inventory/restock")
    public ResponseEntity<InventoryResponse> restockInventory(
        @PathVariable Long productId,
        @Valid @RequestBody RestockInventoryRequest request
    ) {
        // Create command
        RestockInventoryCommand command = new RestockInventoryCommand(
            productId,
            request.getQuantity()
        );

        // Execute use case
        RestockInventoryResult result = restockInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Reserve inventory for a product.
     * POST /api/v1/products/{productId}/inventory/reserve
     */
    @PostMapping("/products/{productId}/inventory/reserve")
    public ResponseEntity<InventoryResponse> reserveInventory(
        @PathVariable Long productId,
        @Valid @RequestBody ReserveInventoryRequest request
    ) {
        // Create command
        ReserveInventoryCommand command = new ReserveInventoryCommand(
            productId,
            request.getQuantity()
        );

        // Execute use case
        ReserveInventoryResult result = reserveInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Release reserved inventory for a product.
     * POST /api/v1/products/{productId}/inventory/release
     */
    @PostMapping("/products/{productId}/inventory/release")
    public ResponseEntity<InventoryResponse> releaseInventory(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(
            productId,
            quantity
        );

        // Execute use case
        ReleaseInventoryResult result = releaseInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Deduct reserved inventory for a product (after order confirmation).
     * POST /api/v1/products/{productId}/inventory/deduct
     */
    @PostMapping("/products/{productId}/inventory/deduct")
    public ResponseEntity<InventoryResponse> deductInventory(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        DeductInventoryCommand command = new DeductInventoryCommand(
            productId,
            quantity
        );

        // Execute use case
        DeductInventoryResult result = deductInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
