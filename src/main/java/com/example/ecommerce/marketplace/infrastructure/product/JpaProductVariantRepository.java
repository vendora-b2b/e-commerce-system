package com.example.ecommerce.marketplace.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ProductVariant entities.
 */
@Repository
public interface JpaProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {

    /**
     * Find all variants for a specific product.
     * @param productId the product ID
     * @return list of variant entities
     */
    List<ProductVariantEntity> findByProductId(Long productId);

    /**
     * Find variants by product ID and color.
     * @param productId the product ID
     * @param color the color filter
     * @return list of variant entities
     */
    List<ProductVariantEntity> findByProductIdAndColor(Long productId, String color);

    /**
     * Find variants by product ID and size.
     * @param productId the product ID
     * @param size the size filter
     * @return list of variant entities
     */
    List<ProductVariantEntity> findByProductIdAndSize(Long productId, String size);

    /**
     * Find variants by product ID, color, and size.
     * @param productId the product ID
     * @param color the color filter
     * @param size the size filter
     * @return list of variant entities
     */
    List<ProductVariantEntity> findByProductIdAndColorAndSize(Long productId, String color, String size);

    /**
     * Find a variant by SKU.
     * @param sku the variant SKU
     * @return Optional containing the variant entity if found
     */
    Optional<ProductVariantEntity> findBySku(String sku);

    /**
     * Check if a variant exists by SKU.
     * @param sku the variant SKU
     * @return true if exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Check if a variant exists with the same product ID, color, and size.
     * @param productId the product ID
     * @param color the color
     * @param size the size
     * @return true if exists, false otherwise
     */
    boolean existsByProductIdAndColorAndSize(Long productId, String color, String size);

    /**
     * Check if a variant exists with the same product ID, color, and size, excluding a specific variant.
     * @param productId the product ID
     * @param color the color
     * @param size the size
     * @param id the variant ID to exclude
     * @return true if exists, false otherwise
     */
    boolean existsByProductIdAndColorAndSizeAndIdNot(Long productId, String color, String size, Long id);

    /**
     * Delete all variants for a specific product.
     * @param productId the product ID
     */
    void deleteByProductId(Long productId);
}
