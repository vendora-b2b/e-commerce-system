package com.example.ecommerce.marketplace.domain.inventory;

/**
 * Represents the status of inventory items.
 * Status is automatically determined based on stock levels.
 */
public enum InventoryStatus {
    /**
     * Inventory is available and above reorder level
     */
    AVAILABLE,

    /**
     * Inventory is below reorder level but still has stock
     */
    LOW_STOCK,

    /**
     * Inventory has no available stock
     */
    OUT_OF_STOCK,

    /**
     * Product is discontinued and no longer available
     */
    DISCONTINUED
}
