package com.example.ecommerce.marketplace.infrastructure.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(InventoryStatus status) {
        return jpaRepository.countByStatus(status);
    }
}
