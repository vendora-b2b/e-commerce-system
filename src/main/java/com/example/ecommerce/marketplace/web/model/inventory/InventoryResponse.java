package com.example.ecommerce.marketplace.web.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryStatus;

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
    private Long productId;
    private Long variantId;
    private Long supplierId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private String warehouseLocation;
    private LocalDateTime lastRestocked;
    private LocalDateTime lastUpdated;
    private InventoryStatus status;

    /**
     * Creates an InventoryResponse from a domain Inventory entity.
     */
    public static InventoryResponse fromDomain(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getProductId(),
            inventory.getVariantId(),
            inventory.getSupplierId(),
            inventory.getAvailableQuantity(),
            inventory.getReservedQuantity(),
            inventory.getReorderLevel(),
            inventory.getReorderQuantity(),
            inventory.getWarehouseLocation(),
            inventory.getLastRestocked(),
            inventory.getLastUpdated(),
            inventory.getStatus()
        );
    }
}
