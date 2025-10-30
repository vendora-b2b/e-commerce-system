package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for restocking inventory when receiving new shipment.
 * Adds quantity to available stock.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class RestockInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    /**
     * Executes the restock inventory use case.
     *
     * @param command the restock command containing product ID and quantity
     * @return the result indicating success or failure with details
     */
    public RestockInventoryResult execute(RestockInventoryCommand command) {
        // 1. Validate required fields
        if (command.getProductId() == null) {
            return RestockInventoryResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getQuantity() == null || command.getQuantity() <= 0) {
            return RestockInventoryResult.failure("Quantity must be greater than 0", "INVALID_QUANTITY");
        }

        // 2. Find inventory
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
            .orElse(null);

        if (inventory == null) {
            return RestockInventoryResult.failure("Inventory not found for product", "INVENTORY_NOT_FOUND");
        }

        // 3. Restock using domain logic
        try {
            inventory.restockInventory(command.getQuantity());
        } catch (IllegalArgumentException e) {
            return RestockInventoryResult.failure(e.getMessage(), "RESTOCK_FAILED");
        }

        // 4. Save inventory
        inventoryRepository.save(inventory);

        // 5. Return success result
        return RestockInventoryResult.success();
    }
}
