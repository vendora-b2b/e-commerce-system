package com.example.ecommerce.marketplace.infrastructure.inventory;

import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for InventoryEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findByProductId(Long productId);

    Optional<InventoryEntity> findByVariantId(Long variantId);

    Optional<InventoryEntity> findBySupplierIdAndProductId(Long supplierId, Long productId);

    List<InventoryEntity> findBySupplierId(Long supplierId);

    List<InventoryEntity> findByStatus(InventoryStatus status);

    /**
     * Finds all inventory items that need reordering.
     * Items where (availableQuantity + reservedQuantity) <= reorderLevel.
     */
    @Query("SELECT i FROM InventoryEntity i WHERE (i.availableQuantity + i.reservedQuantity) <= i.reorderLevel AND i.reorderLevel IS NOT NULL AND i.status <> 'DISCONTINUED'")
    List<InventoryEntity> findInventoryNeedingReorder();

    /**
     * Finds all inventory items that need reordering for a specific supplier.
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.supplierId = ?1 AND (i.availableQuantity + i.reservedQuantity) <= i.reorderLevel AND i.reorderLevel IS NOT NULL AND i.status <> 'DISCONTINUED'")
    List<InventoryEntity> findInventoryNeedingReorderBySupplierId(Long supplierId);

    boolean existsByProductId(Long productId);

    void deleteByVariantId(Long variantId);

    long countByStatus(InventoryStatus status);
}
