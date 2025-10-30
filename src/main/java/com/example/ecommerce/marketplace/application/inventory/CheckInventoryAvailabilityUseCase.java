package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for checking if inventory is available for ordering.
 * Returns availability information for a product.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class CheckInventoryAvailabilityUseCase {

    private final InventoryRepository inventoryRepository;

    /**
     * Executes the check inventory availability use case.
     *
     * @param command the availability check command containing product ID and requested quantity
     * @return the result containing availability information
     */
    public CheckInventoryAvailabilityResult execute(CheckInventoryAvailabilityCommand command) {
        // 1. Validate required fields
        if (command.getProductId() == null) {
            return CheckInventoryAvailabilityResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Find inventory
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
            .orElse(null);

        if (inventory == null) {
            return CheckInventoryAvailabilityResult.failure("Inventory not found for product", "INVENTORY_NOT_FOUND");
        }

        // 3. Check availability using domain logic
        boolean isAvailable = inventory.isAvailableForOrder();
        boolean hasSufficientStock = command.getRequestedQuantity() != null &&
            inventory.hasSufficientStock(command.getRequestedQuantity());

        // 4. Return success result with availability information
        return CheckInventoryAvailabilityResult.success(
            isAvailable,
            hasSufficientStock,
            inventory.getAvailableQuantity(),
            inventory.getStatus().name()
        );
    }
}
