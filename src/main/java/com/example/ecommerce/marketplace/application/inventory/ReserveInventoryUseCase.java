package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for reserving inventory for a pending order.
 * Moves stock from available to reserved to prevent overselling.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class ReserveInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    /**
     * Executes the reserve inventory use case.
     *
     * @param command the reservation command containing product ID and quantity
     * @return the result indicating success or failure with details
     */
    public ReserveInventoryResult execute(ReserveInventoryCommand command) {
        // 1. Validate required fields
        if (command.getProductId() == null) {
            return ReserveInventoryResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getQuantity() == null || command.getQuantity() <= 0) {
            return ReserveInventoryResult.failure("Quantity must be greater than 0", "INVALID_QUANTITY");
        }

        // 2. Find inventory
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
            .orElse(null);

        if (inventory == null) {
            return ReserveInventoryResult.failure("Inventory not found for product", "INVENTORY_NOT_FOUND");
        }

        // 3. Check if product is available for order
        if (!inventory.isAvailableForOrder()) {
            return ReserveInventoryResult.failure(
                "Product is not available for ordering (status: " + inventory.getStatus() + ")",
                "PRODUCT_NOT_AVAILABLE"
            );
        }

        // 4. Reserve stock using domain logic
        boolean reserved = inventory.reserveStock(command.getQuantity());
        if (!reserved) {
            return ReserveInventoryResult.failure(
                "Insufficient stock. Available: " + inventory.getAvailableQuantity() +
                    ", Requested: " + command.getQuantity(),
                "INSUFFICIENT_STOCK"
            );
        }

        // 5. Save inventory
        inventoryRepository.save(inventory);

        // 6. Return success result
        return ReserveInventoryResult.success();
    }
}
