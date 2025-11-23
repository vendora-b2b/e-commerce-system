package com.example.ecommerce.marketplace.domain.product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductVariant aggregate.
 * Defines operations for persisting and retrieving product variants.
 */
public interface ProductVariantRepository {

    /**
     * Saves a product variant.
     * @param variant the variant to save
     * @return the saved variant with generated ID
     */
    ProductVariant save(ProductVariant variant);

    /**
     * Finds a variant by ID.
     * @param id the variant ID
     * @return Optional containing the variant if found
     */
    Optional<ProductVariant> findById(Long id);

    /**
     * Finds all variants for a specific product.
     * @param productId the product ID
     * @return list of variants
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Finds variants for a product with optional filters.
     * @param productId the product ID
     * @param color optional color filter
     * @param size optional size filter
     * @return list of filtered variants
     */
    List<ProductVariant> findByProductIdWithFilters(Long productId, String color, String size);

    /**
     * Checks if a variant exists with the same product ID, color, and size.
     * @param productId the product ID
     * @param color the color
     * @param size the size
     * @return true if exists, false otherwise
     */
    boolean existsByProductIdAndColorAndSize(Long productId, String color, String size);

    /**
     * Checks if a variant exists with the same product ID, color, and size, excluding a specific variant.
     * @param productId the product ID
     * @param color the color
     * @param size the size
     * @param excludeVariantId the variant ID to exclude from the check
     * @return true if exists, false otherwise
     */
    boolean existsByProductIdAndColorAndSizeAndIdNot(Long productId, String color, String size, Long excludeVariantId);

    /**
     * Finds a variant by SKU.
     * @param sku the variant SKU
     * @return Optional containing the variant if found
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Checks if a variant exists by SKU.
     * @param sku the variant SKU
     * @return true if exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Counts the number of variants for a specific product.
     * @param productId the product ID
     * @return the count of variants
     */
    long countByProductId(Long productId);

    /**
     * Deletes a variant by ID.
     * @param id the variant ID
     */
    void deleteById(Long id);

    /**
     * Deletes all variants for a specific product.
     * @param productId the product ID
     */
    void deleteByProductId(Long productId);
}
