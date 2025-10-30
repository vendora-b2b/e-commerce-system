package com.example.ecommerce.marketplace.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ProductEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Finds a product by SKU.
     */
    Optional<ProductEntity> findBySku(String sku);

    /**
     * Finds all products by supplier ID.
     */
    List<ProductEntity> findBySupplierId(Long supplierId);

    /**
     * Finds all products by category.
     */
    List<ProductEntity> findByCategory(String category);

    /**
     * Finds all products by status.
     */
    List<ProductEntity> findByStatus(String status);

    /**
     * Finds products by supplier and status.
     */
    List<ProductEntity> findBySupplierIdAndStatus(Long supplierId, String status);

    /**
     * Finds products within a price range.
     */
    List<ProductEntity> findByBasePriceBetween(Double minPrice, Double maxPrice);

    /**
     * Finds products by category and status.
     */
    List<ProductEntity> findByCategoryAndStatus(String category, String status);

    /**
     * Searches products by name (case-insensitive).
     */
    List<ProductEntity> findByNameContainingIgnoreCase(String keyword);

    /**
     * Checks if a product exists with the given SKU.
     */
    boolean existsBySku(String sku);

    /**
     * Counts products by status.
     */
    long countByStatus(String status);

    /**
     * Counts products by supplier.
     */
    long countBySupplierId(Long supplierId);

    /**
     * Counts products by category.
     */
    long countByCategory(String category);

    /**
     * Finds products with MOQ less than or equal to specified value.
     */
    List<ProductEntity> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq);
}
