package com.example.ecommerce.marketplace.infrastructure.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryStatus;

/**
 * JPA entity for Inventory.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "inventory",
    indexes = {
        @Index(name = "idx_inventory_supplier", columnList = "supplier_id"),
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_variant", columnList = "variant_id"),
        @Index(name = "idx_inventory_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_product_variant", columnNames = {"product_id", "variant_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    private Integer reorderLevel;

    private Integer reorderQuantity;

    private String warehouseLocation;

    private LocalDateTime lastRestocked;

    private LocalDateTime lastUpdated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    /**
     * Converts JPA entity to domain model.
     */
    public Inventory toDomain() {
        Inventory inventory = new Inventory();
        inventory.setId(this.id);
        inventory.setSupplierId(this.supplierId);
        inventory.setProductId(this.productId);
        inventory.setVariantId(this.variantId);
        inventory.setAvailableQuantity(this.availableQuantity);
        inventory.setReservedQuantity(this.reservedQuantity);
        inventory.setReorderLevel(this.reorderLevel);
        inventory.setReorderQuantity(this.reorderQuantity);
        inventory.setWarehouseLocation(this.warehouseLocation);
        inventory.setLastRestocked(this.lastRestocked);
        inventory.setLastUpdated(this.lastUpdated);
        inventory.setStatus(this.status);
        return inventory;
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static InventoryEntity fromDomain(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(inventory.getId());
        entity.setSupplierId(inventory.getSupplierId());
        entity.setProductId(inventory.getProductId());
        entity.setVariantId(inventory.getVariantId());
        entity.setAvailableQuantity(inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0);
        entity.setReservedQuantity(inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0);
        entity.setReorderLevel(inventory.getReorderLevel());
        entity.setReorderQuantity(inventory.getReorderQuantity());
        entity.setWarehouseLocation(inventory.getWarehouseLocation());
        entity.setLastRestocked(inventory.getLastRestocked());
        entity.setLastUpdated(inventory.getLastUpdated());
        entity.setStatus(inventory.getStatus() != null ? inventory.getStatus() : InventoryStatus.AVAILABLE);
        return entity;
    }
}
