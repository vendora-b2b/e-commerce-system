package com.example.ecommerce.marketplace.infrastructure.retailer;

import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for RetailerEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaRetailerRepository extends JpaRepository<RetailerEntity, Long> {

    Optional<RetailerEntity> findByEmail(String email);

    Optional<RetailerEntity> findByBusinessLicense(String businessLicense);

    List<RetailerEntity> findByAccountStatus(String accountStatus);

    List<RetailerEntity> findByLoyaltyTier(RetailerLoyaltyTier loyaltyTier);

    boolean existsByEmail(String email);

    boolean existsByBusinessLicense(String businessLicense);

    long countByAccountStatus(String accountStatus);
}
