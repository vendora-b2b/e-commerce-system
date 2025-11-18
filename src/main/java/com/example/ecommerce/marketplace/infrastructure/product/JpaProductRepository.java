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
     * Finds all products by category ID.
     */
    List<ProductEntity> findByCategoryId(Long categoryId);

    /**
     * Finds products within a price range.
     */
    List<ProductEntity> findByBasePriceBetween(Double minPrice, Double maxPrice);

    /**
     * Searches products by name (case-insensitive).
     */
    List<ProductEntity> findByNameContainingIgnoreCase(String keyword);

    /**
     * Checks if a product exists with the given SKU.
     */
    boolean existsBySku(String sku);

    /**
     * Counts products by supplier.
     */
    long countBySupplierId(Long supplierId);

    /**
     * Counts products by category ID.
     */
    long countByCategoryId(Long categoryId);

    /**
     * Finds products with MOQ less than or equal to specified value.
     */
    List<ProductEntity> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq);
}
