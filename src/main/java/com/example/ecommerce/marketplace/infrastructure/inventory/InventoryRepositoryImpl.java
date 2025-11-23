package com.example.ecommerce.marketplace.infrastructure.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.inventory.InventoryStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final JpaInventoryRepository jpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = InventoryEntity.fromDomain(inventory);
        InventoryEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return jpaRepository.findById(id)
            .map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId)
            .map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findByVariantId(Long variantId) {
        return jpaRepository.findByVariantId(variantId)
            .map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductIdAndVariantId(Long productId, Long variantId) {
        return jpaRepository.findByProductIdAndVariantId(productId, variantId)
            .map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findBySupplierIdAndProductId(Long supplierId, Long productId) {
        return jpaRepository.findBySupplierIdAndProductId(supplierId, productId)
            .map(InventoryEntity::toDomain);
    }

    @Override
    public List<Inventory> findBySupplierId(Long supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findByStatus(InventoryStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findInventoryNeedingReorder() {
        return jpaRepository.findInventoryNeedingReorder().stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findInventoryNeedingReorderBySupplierId(Long supplierId) {
        return jpaRepository.findInventoryNeedingReorderBySupplierId(supplierId).stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jpaRepository.existsByProductId(productId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByVariantId(Long variantId) {
        jpaRepository.deleteByVariantId(variantId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(InventoryStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public Page<Inventory> findBySupplierIdWithFilters(
        Long supplierId,
        Long productId,
        Long variantId,
        Boolean needsReorder,
        Pageable pageable
    ) {
        Page<InventoryEntity> entityPage = jpaRepository.findBySupplierIdWithFilters(
            supplierId, productId, variantId, needsReorder, pageable
        );

        List<Inventory> inventories = entityPage.getContent().stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());

        return new PageImpl<>(inventories, pageable, entityPage.getTotalElements());
    }
}
