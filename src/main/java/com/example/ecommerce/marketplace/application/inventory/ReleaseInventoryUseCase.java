package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for releasing reserved inventory when order is cancelled.
 * Moves stock from reserved back to available.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class ReleaseInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    /**
     * Executes the release inventory use case.
     *
     * @param command the release command containing product ID and quantity
     * @return the result indicating success or failure with details
     */
    public ReleaseInventoryResult execute(ReleaseInventoryCommand command) {
        // 1. Validate required fields
        if (command.getProductId() == null) {
            return ReleaseInventoryResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getQuantity() == null || command.getQuantity() <= 0) {
            return ReleaseInventoryResult.failure("Quantity must be greater than 0", "INVALID_QUANTITY");
        }

        // 2. Find inventory
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
            .orElse(null);

        if (inventory == null) {
            return ReleaseInventoryResult.failure("Inventory not found for product", "INVENTORY_NOT_FOUND");
        }

        // 3. Release stock using domain logic
        try {
            inventory.releaseReservedStock(command.getQuantity());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ReleaseInventoryResult.failure(e.getMessage(), "RELEASE_FAILED");
        }

        // 4. Save inventory
        inventoryRepository.save(inventory);

        // 5. Return success result
        return ReleaseInventoryResult.success();
    }
}
