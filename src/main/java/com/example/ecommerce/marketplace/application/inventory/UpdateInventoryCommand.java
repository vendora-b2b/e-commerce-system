package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for updating inventory.
 * Contains all data needed to update inventory for a product variant.
 */
public class UpdateInventoryCommand {

    private final Long variantId;
    private final Integer availableQuantity;
    private final Integer reorderLevel;
    private final Integer reorderQuantity;
    private final String warehouseLocation;

    public UpdateInventoryCommand(
        Long variantId,
        Integer availableQuantity,
        Integer reorderLevel,
        Integer reorderQuantity,
        String warehouseLocation
    ) {
        this.variantId = variantId;
        this.availableQuantity = availableQuantity;
        this.reorderLevel = reorderLevel;
        this.reorderQuantity = reorderQuantity;
        this.warehouseLocation = warehouseLocation;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }
}
