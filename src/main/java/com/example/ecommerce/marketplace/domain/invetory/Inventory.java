package com.example.ecommerce.marketplace.domain.invetory;

import java.time.LocalDateTime;

/**
 * Represents an inventory entity in the e-commerce marketplace.
 * Inventory tracks product stock levels and availability for suppliers.
 * This is an aggregate root that manages product quantities and stock movements.
 */
public class Inventory {

    private Long id;
    private Long supplierId;
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private String warehouseLocation;
    private LocalDateTime lastRestocked;
    private LocalDateTime lastUpdated;
    private InventoryStatus status;

    /**
     * Checks if the inventory has sufficient stock for a given quantity.
     * @param quantity the quantity to check
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean hasSufficientStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        if (availableQuantity == null) {
            return false;
        }
        return availableQuantity >= quantity;
    }

    /**
     * Reserves inventory for a pending order.
     * Moves quantity from available to reserved.
     * @param quantity the quantity to reserve
     * @return true if reservation successful, false otherwise
     */
    public boolean reserveStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        if (!hasSufficientStock(quantity)) {
            return false;
        }

        availableQuantity -= quantity;
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        reservedQuantity += quantity;

        this.lastUpdated = LocalDateTime.now();
        updateStatus();
        return true;
    }

    /**
     * Releases reserved inventory (e.g., when order is cancelled).
     * Moves quantity from reserved back to available.
     * @param quantity the quantity to release
     */
    public void releaseReservedStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (reservedQuantity == null || reservedQuantity < quantity) {
            throw new IllegalStateException("Not enough reserved stock to release");
        }

        reservedQuantity -= quantity;
        if (availableQuantity == null) {
            availableQuantity = 0;
        }
        availableQuantity += quantity;

        this.lastUpdated = LocalDateTime.now();
        updateStatus();
    }

    /**
     * Deducts inventory after order confirmation/shipment.
     * Removes quantity from reserved stock.
     * @param quantity the quantity to deduct
     * @return true if deduction successful, false otherwise
     */
    public boolean deductStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        if (reservedQuantity == null || reservedQuantity < quantity) {
            return false;
        }

        reservedQuantity -= quantity;
        this.lastUpdated = LocalDateTime.now();
        updateStatus();
        return true;
    }

    /**
     * Adds stock when inventory is restocked.
     * @param quantity the quantity to add
     */
    public void restockInventory(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }

        if (availableQuantity == null) {
            availableQuantity = 0;
        }
        availableQuantity += quantity;

        this.lastRestocked = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        updateStatus();
    }

    /**
     * Checks if inventory level is below reorder threshold.
     * @return true if needs reorder, false otherwise
     */
    public boolean needsReorder() {
        if (status == InventoryStatus.DISCONTINUED) {
            return false;
        }
        if (reorderLevel == null) {
            return false;
        }
        int total = getTotalStock();
        return total <= reorderLevel;
    }

    /**
     * Calculates total stock (available + reserved).
     * @return total stock quantity
     */
    public Integer getTotalStock() {
        int available = (availableQuantity != null) ? availableQuantity : 0;
        int reserved = (reservedQuantity != null) ? reservedQuantity : 0;
        return available + reserved;
    }

    /**
     * Checks if inventory status is available for ordering.
     * @return true if available for ordering, false otherwise
     */
    public boolean isAvailableForOrder() {
        return status == InventoryStatus.AVAILABLE || status == InventoryStatus.LOW_STOCK;
    }

    /**
     * Updates inventory status based on current stock levels.
     * Status is automatically determined:
     * - DISCONTINUED: manually set, no auto-update
     * - OUT_OF_STOCK: availableQuantity is 0
     * - LOW_STOCK: availableQuantity <= reorderLevel
     * - AVAILABLE: otherwise
     */
    public void updateStatus() {
        // Don't auto-update if manually discontinued
        if (status == InventoryStatus.DISCONTINUED) {
            return;
        }

        int available = (availableQuantity != null) ? availableQuantity : 0;

        if (available == 0) {
            status = InventoryStatus.OUT_OF_STOCK;
        } else if (reorderLevel != null && available <= reorderLevel) {
            status = InventoryStatus.LOW_STOCK;
        } else {
            status = InventoryStatus.AVAILABLE;
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public LocalDateTime getLastRestocked() {
        return lastRestocked;
    }

    public void setLastRestocked(LocalDateTime lastRestocked) {
        this.lastRestocked = lastRestocked;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }
}
