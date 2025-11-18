package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ProductRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ProductEntity::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
            .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findBySupplierId(Long supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId).stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByBasePriceBetween(Double minPrice, Double maxPrice) {
        return jpaRepository.findByBasePriceBetween(minPrice, maxPrice).stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String keyword) {
        return jpaRepository.findByNameContainingIgnoreCase(keyword).stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countBySupplierId(Long supplierId) {
        return jpaRepository.countBySupplierId(supplierId);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return jpaRepository.countByCategoryId(categoryId);
    }

    @Override
    public List<Product> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq) {
        return jpaRepository.findByMinimumOrderQuantityLessThanEqual(maxMoq).stream()
            .map(ProductEntity::toDomain)
            .collect(Collectors.toList());
    }
}
