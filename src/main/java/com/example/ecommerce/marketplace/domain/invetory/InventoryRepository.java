package com.example.ecommerce.marketplace.domain.invetory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Inventory aggregate root.
 * Defines persistence operations for inventory management.
 */
public interface InventoryRepository {

    /**
     * Saves a new inventory or updates an existing one.
     * @param inventory the inventory to save
     * @return the saved inventory with generated ID if new
     */
    Inventory save(Inventory inventory);

    /**
     * Finds an inventory by its unique identifier.
     * @param id the inventory ID
     * @return an Optional containing the inventory if found, empty otherwise
     */
    Optional<Inventory> findById(Long id);

    /**
     * Finds inventory by product ID.
     * @param productId the product ID
     * @return an Optional containing the inventory if found, empty otherwise
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Finds inventory by supplier ID and product ID.
     * @param supplierId the supplier ID
     * @param productId the product ID
     * @return an Optional containing the inventory if found, empty otherwise
     */
    Optional<Inventory> findBySupplierIdAndProductId(Long supplierId, Long productId);

    /**
     * Finds all inventory items for a specific supplier.
     * @param supplierId the supplier ID
     * @return list of inventory items for the supplier
     */
    List<Inventory> findBySupplierId(Long supplierId);

    /**
     * Finds all inventory items with a specific status.
     * @param status the inventory status
     * @return list of inventory items with the specified status
     */
    List<Inventory> findByStatus(InventoryStatus status);

    /**
     * Finds all inventory items that need reordering.
     * Items where (availableQuantity + reservedQuantity) <= reorderLevel.
     * @return list of inventory items that need reordering
     */
    List<Inventory> findInventoryNeedingReorder();

    /**
     * Finds all inventory items.
     * @return list of all inventory items
     */
    List<Inventory> findAll();

    /**
     * Checks if inventory exists for a product.
     * @param productId the product ID
     * @return true if exists, false otherwise
     */
    boolean existsByProductId(Long productId);

    /**
     * Deletes an inventory by its ID.
     * @param id the inventory ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of inventory items.
     * @return the total count
     */
    long count();

    /**
     * Counts inventory items by status.
     * @param status the inventory status
     * @return the count of inventory items with the specified status
     */
    long countByStatus(InventoryStatus status);
}
