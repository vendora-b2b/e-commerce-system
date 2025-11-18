package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProductVariantRepository.
 * Handles persistence operations for product variants.
 */
@Repository
@RequiredArgsConstructor
public class ProductVariantRepositoryImpl implements ProductVariantRepository {

    private final JpaProductVariantRepository jpaRepository;
    private final JpaProductRepository jpaProductRepository;

    @Override
    public ProductVariant save(ProductVariant variant) {
        // Find the product entity
        ProductEntity productEntity = null;
        if (variant.getProductId() != null) {
            productEntity = jpaProductRepository.findById(variant.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + variant.getProductId()));
        }

        ProductVariantEntity entity = ProductVariantEntity.fromDomain(variant, productEntity);
        ProductVariantEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ProductVariantEntity::toDomain);
    }

    @Override
    public List<ProductVariant> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).stream()
            .map(ProductVariantEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductVariant> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
            .map(ProductVariantEntity::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByProductId(Long productId) {
        jpaRepository.deleteByProductId(productId);
    }
}
