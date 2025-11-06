package com.example.ecommerce.marketplace.web.model.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * HTTP response DTO for inventory information.
 * Represents an inventory entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long supplierId;
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalStock;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private String warehouseLocation;
    private LocalDateTime lastRestocked;
    private LocalDateTime lastUpdated;
    private InventoryStatus status;
    private Boolean needsReorder;

    /**
     * Creates an InventoryResponse from a domain Inventory entity.
     */
    public static InventoryResponse fromDomain(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getSupplierId(),
            inventory.getProductId(),
            inventory.getAvailableQuantity(),
            inventory.getReservedQuantity(),
            inventory.getTotalStock(),
            inventory.getReorderLevel(),
            inventory.getReorderQuantity(),
            inventory.getWarehouseLocation(),
            inventory.getLastRestocked(),
            inventory.getLastUpdated(),
            inventory.getStatus(),
            inventory.needsReorder()
        );
    }
}
