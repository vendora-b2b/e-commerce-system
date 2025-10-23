package com.example.ecommerce.marketplace.infrastructure.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of SupplierRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class SupplierRepositoryImpl implements SupplierRepository {

    private final JpaSupplierRepository jpaRepository;

    @Override
    public Supplier save(Supplier supplier) {
        SupplierEntity entity = SupplierEntity.fromDomain(supplier);
        SupplierEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return jpaRepository.findById(id)
            .map(SupplierEntity::toDomain);
    }

    @Override
    public Optional<Supplier> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(SupplierEntity::toDomain);
    }

    @Override
    public Optional<Supplier> findByBusinessLicense(String businessLicense) {
        return jpaRepository.findByBusinessLicense(businessLicense)
            .map(SupplierEntity::toDomain);
    }

    @Override
    public List<Supplier> findByVerified(Boolean verified) {
        return jpaRepository.findByVerified(verified).stream()
            .map(SupplierEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Supplier> findByRatingGreaterThanEqual(Double minRating) {
        return jpaRepository.findByRatingGreaterThanEqual(minRating).stream()
            .map(SupplierEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Supplier> findAll() {
        return jpaRepository.findAll().stream()
            .map(SupplierEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByBusinessLicense(String businessLicense) {
        return jpaRepository.existsByBusinessLicense(businessLicense);
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
    public long countByVerified(Boolean verified) {
        return jpaRepository.countByVerified(verified);
    }

    @Override
    public long countByRatingGreaterThanEqual(Double minRating) {
        return jpaRepository.countByRatingGreaterThanEqual(minRating);
    }
}
