package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.inventory.UpdateInventoryCommand;
import com.example.ecommerce.marketplace.application.inventory.UpdateInventoryResult;
import com.example.ecommerce.marketplace.application.inventory.UpdateInventoryUseCase;
import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.inventory.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for Inventory operations.
 * Handles HTTP requests for inventory queries.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory API")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final UpdateInventoryUseCase updateInventoryUseCase;

    /**
     * Get inventory for a specific product variant.
     * GET /api/v1/variants/{variantId}/inventory
     */
    @GetMapping("/variants/{variantId}/inventory")
    public ResponseEntity<InventoryResponse> getInventoryByVariant(
        @PathVariable Long variantId
    ) {
        Optional<Inventory> inventory = inventoryRepository.findByVariantId(variantId);

        if (inventory.isPresent()) {
            InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get all inventory items for a supplier with optional filters and pagination.
     * GET /api/v1/suppliers/{supplierId}/inventory
     *
     * Query params:
     * - productId: Filter by product ID (optional)
     * - variantId: Filter by variant ID (optional)
     * - needsReorder: Filter by reorder status (optional, true/false)
     * - page: Page number, 0-indexed (default: 0)
     * - size: Paba
     * - sort: Sort field and direction, e.g., "lastUpdated,desc" or "availableQuantity,asc" (default: "id,asc")
     */
    @GetMapping("/suppliers/{supplierId}/inventory")
    public ResponseEntity<Page<InventoryResponse>> getInventoryBySupplier(
        @PathVariable Long supplierId,
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) Long variantId,
        @RequestParam(required = false) Boolean needsReorder,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id,asc") String sort
    ) {
        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // Execute query with filters
        Page<Inventory> inventoryPage = inventoryRepository.findBySupplierIdWithFilters(
            supplierId, productId, variantId, needsReorder, pageable
        );

        // Convert to response
        Page<InventoryResponse> responsePage = inventoryPage.map(InventoryResponse::fromDomain);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Update inventory for a specific product variant.
     * PATCH /api/v1/variants/{variantId}/inventory
     */
    @PatchMapping("/variants/{variantId}/inventory")
    public ResponseEntity<InventoryResponse> updateInventory(
        @PathVariable Long variantId,
        @Valid @RequestBody UpdateInventoryRequest request
    ) {
        // Create command
        UpdateInventoryCommand command = new UpdateInventoryCommand(
            variantId,
            request.getAvailableQuantity(),
            request.getReorderLevel(),
            request.getReorderQuantity(),
            request.getWarehouseLocation()
        );

        // Execute use case
        UpdateInventoryResult result = updateInventoryUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            InventoryResponse response = InventoryResponse.fromDomain(result.getInventory());
            return ResponseEntity.ok(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
