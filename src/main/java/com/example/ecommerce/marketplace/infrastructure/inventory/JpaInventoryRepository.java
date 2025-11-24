package com.example.ecommerce.marketplace.infrastructure.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.marketplace.domain.inventory.InventoryStatus;

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

    Optional<InventoryEntity> findByProductIdAndVariantId(Long productId, Long variantId);

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

    /**
     * Finds inventory items for a supplier with optional filters and pagination.
     * Uses dynamic query to handle optional parameters.
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.supplierId = :supplierId " +
           "AND (:productId IS NULL OR i.productId = :productId) " +
           "AND (:variantId IS NULL OR i.variantId = :variantId) " +
           "AND (:needsReorder IS NULL OR " +
           "     (:needsReorder = true AND i.availableQuantity <= i.reorderLevel AND i.reorderLevel IS NOT NULL AND i.status <> 'DISCONTINUED') OR " +
           "     (:needsReorder = false AND (i.availableQuantity > i.reorderLevel OR i.reorderLevel IS NULL OR i.status = 'DISCONTINUED')))")
    Page<InventoryEntity> findBySupplierIdWithFilters(
        @Param("supplierId") Long supplierId,
        @Param("productId") Long productId,
        @Param("variantId") Long variantId,
        @Param("needsReorder") Boolean needsReorder,
        Pageable pageable
    );
}
