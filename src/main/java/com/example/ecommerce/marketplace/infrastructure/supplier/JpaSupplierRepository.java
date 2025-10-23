package com.example.ecommerce.marketplace.infrastructure.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for SupplierEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByEmail(String email);

    Optional<SupplierEntity> findByBusinessLicense(String businessLicense);

    List<SupplierEntity> findByVerified(Boolean verified);

    List<SupplierEntity> findByRatingGreaterThanEqual(Double minRating);

    boolean existsByEmail(String email);

    boolean existsByBusinessLicense(String businessLicense);

    long countByVerified(Boolean verified);

    long countByRatingGreaterThanEqual(Double minRating);
}
