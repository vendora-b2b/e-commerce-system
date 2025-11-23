package com.example.ecommerce.marketplace.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Finds all products that belong to a specific category.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.categories c WHERE c.id = :categoryId")
    List<ProductEntity> findByCategory(@Param("categoryId") Long categoryId);

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
     * Counts products by category.
     */
    @Query("SELECT COUNT(DISTINCT p) FROM ProductEntity p JOIN p.categories c WHERE c.id = :categoryId")
    long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * Finds products with MOQ less than or equal to specified value.
     */
    List<ProductEntity> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq);

    /**
     * Finds products by SKU with pagination.
     */
    Page<ProductEntity> findBySku(String sku, Pageable pageable);

    /**
     * Finds products by supplier ID with pagination.
     */
    Page<ProductEntity> findBySupplierId(Long supplierId, Pageable pageable);

    /**
     * Finds products by category slug with pagination.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.categories c WHERE c.slug = :categorySlug")
    Page<ProductEntity> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Finds all products with pagination.
     */
    Page<ProductEntity> findAll(Pageable pageable);

    /**
     * Finds products by supplier ID and category slug with pagination.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.categories c WHERE p.supplierId = :supplierId AND c.slug = :categorySlug")
    Page<ProductEntity> findBySupplierIdAndCategorySlug(@Param("supplierId") Long supplierId, @Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Finds products by SKU and supplier ID with pagination.
     */
    Page<ProductEntity> findBySkuAndSupplierId(String sku, Long supplierId, Pageable pageable);

    /**
     * Finds products by SKU and category slug with pagination.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.categories c WHERE p.sku = :sku AND c.slug = :categorySlug")
    Page<ProductEntity> findBySkuAndCategorySlug(@Param("sku") String sku, @Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Finds products by all filters with pagination.
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.categories c WHERE p.sku = :sku AND p.supplierId = :supplierId AND c.slug = :categorySlug")
    Page<ProductEntity> findBySkuAndSupplierIdAndCategorySlug(@Param("sku") String sku, @Param("supplierId") Long supplierId, @Param("categorySlug") String categorySlug, Pageable pageable);
}
