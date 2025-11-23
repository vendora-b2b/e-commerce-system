package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for updating inventory information.
 * Handles business validation and ensures data integrity.
 */
public class UpdateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public UpdateInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Executes the inventory update operation.
     *
     * Business rules:
     * - Inventory must exist for the given variant
     * - Available quantity must be non-negative
     * - Available quantity must be >= reserved quantity (cannot set available below what's already reserved)
     * - Reorder level and quantity must be non-negative if provided
     *
     * @param command the update command containing inventory data
     * @return result indicating success or failure with error details
     */
    public UpdateInventoryResult execute(UpdateInventoryCommand command) {
        // Validate command
        if (command.getVariantId() == null) {
            return UpdateInventoryResult.failure("INVALID_VARIANT_ID", "Variant ID is required");
        }

        if (command.getAvailableQuantity() == null) {
            return UpdateInventoryResult.failure("INVALID_AVAILABLE_QUANTITY", "Available quantity is required");
        }

        if (command.getAvailableQuantity() < 0) {
            return UpdateInventoryResult.failure("NEGATIVE_AVAILABLE_QUANTITY", "Available quantity cannot be negative");
        }

        // Validate optional fields
        if (command.getReorderLevel() != null && command.getReorderLevel() < 0) {
            return UpdateInventoryResult.failure("NEGATIVE_REORDER_LEVEL", "Reorder level cannot be negative");
        }

        if (command.getReorderQuantity() != null && command.getReorderQuantity() < 0) {
            return UpdateInventoryResult.failure("NEGATIVE_REORDER_QUANTITY", "Reorder quantity cannot be negative");
        }

        // Find existing inventory
        Optional<Inventory> optionalInventory = inventoryRepository.findByVariantId(command.getVariantId());

        if (optionalInventory.isEmpty()) {
            return UpdateInventoryResult.failure(
                "INVENTORY_NOT_FOUND",
                String.format("Inventory not found for variant %d", command.getVariantId())
            );
        }

        Inventory inventory = optionalInventory.get();

        // CRITICAL VALIDATION: Available quantity must be >= reserved quantity
        Integer reservedQuantity = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        if (command.getAvailableQuantity() < reservedQuantity) {
            return UpdateInventoryResult.failure(
                "AVAILABLE_LESS_THAN_RESERVED",
                String.format("Available quantity (%d) cannot be less than reserved quantity (%d). " +
                    "Please release reservations first or set available quantity to at least %d",
                    command.getAvailableQuantity(), reservedQuantity, reservedQuantity)
            );
        }

        // Update inventory fields
        inventory.setAvailableQuantity(command.getAvailableQuantity());

        if (command.getReorderLevel() != null) {
            inventory.setReorderLevel(command.getReorderLevel());
        }

        if (command.getReorderQuantity() != null) {
            inventory.setReorderQuantity(command.getReorderQuantity());
        }

        if (command.getWarehouseLocation() != null) {
            inventory.setWarehouseLocation(command.getWarehouseLocation());
        }

        // Update metadata
        inventory.setLastUpdated(LocalDateTime.now());
        inventory.updateStatus();

        // Save and return
        Inventory savedInventory = inventoryRepository.save(inventory);
        return UpdateInventoryResult.success(savedInventory);
    }
}
