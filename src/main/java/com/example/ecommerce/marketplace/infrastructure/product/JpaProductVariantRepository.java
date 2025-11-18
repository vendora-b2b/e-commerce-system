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
     * Delete all variants for a specific product.
     * @param productId the product ID
     */
    void deleteByProductId(Long productId);
}
