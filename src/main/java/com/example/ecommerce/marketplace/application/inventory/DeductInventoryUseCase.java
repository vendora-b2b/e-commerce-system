package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case for deducting inventory after order confirmation/shipment.
 * Removes stock from reserved quantity.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class DeductInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    /**
     * Executes the deduct inventory use case.
     *
     * @param command the deduction command containing product ID and quantity
     * @return the result indicating success or failure with details
     */
    public DeductInventoryResult execute(DeductInventoryCommand command) {
        // 1. Validate required fields
        if (command.getProductId() == null) {
            return DeductInventoryResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getQuantity() == null || command.getQuantity() <= 0) {
            return DeductInventoryResult.failure("Quantity must be greater than 0", "INVALID_QUANTITY");
        }

        // 2. Find inventory
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
            .orElse(null);

        if (inventory == null) {
            return DeductInventoryResult.failure("Inventory not found for product", "INVENTORY_NOT_FOUND");
        }

        // 3. Deduct stock using domain logic
        boolean deducted = inventory.deductStock(command.getQuantity());
        if (!deducted) {
            return DeductInventoryResult.failure(
                "Insufficient reserved stock. Reserved: " + inventory.getReservedQuantity() +
                    ", Requested: " + command.getQuantity(),
                "INSUFFICIENT_RESERVED_STOCK"
            );
        }

        // 4. Save inventory
        inventoryRepository.save(inventory);

        // 5. Return success result
        return DeductInventoryResult.success();
    }
}
