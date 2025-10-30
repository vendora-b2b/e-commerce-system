package com.example.ecommerce.marketplace.infrastructure.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of RetailerRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class RetailerRepositoryImpl implements RetailerRepository {

    private final JpaRetailerRepository jpaRepository;

    @Override
    public Retailer save(Retailer retailer) {
        RetailerEntity entity = RetailerEntity.fromDomain(retailer);
        RetailerEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Retailer> findById(Long id) {
        return jpaRepository.findById(id)
            .map(RetailerEntity::toDomain);
    }

    @Override
    public Optional<Retailer> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(RetailerEntity::toDomain);
    }

    @Override
    public Optional<Retailer> findByBusinessLicense(String businessLicense) {
        return jpaRepository.findByBusinessLicense(businessLicense)
            .map(RetailerEntity::toDomain);
    }

    @Override
    public List<Retailer> findByAccountStatus(String accountStatus) {
        return jpaRepository.findByAccountStatus(accountStatus).stream()
            .map(RetailerEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Retailer> findByLoyaltyTier(String loyaltyTier) {
        RetailerLoyaltyTier tier = RetailerLoyaltyTier.valueOf(loyaltyTier);
        return jpaRepository.findByLoyaltyTier(tier).stream()
            .map(RetailerEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Retailer> findAll() {
        return jpaRepository.findAll().stream()
            .map(RetailerEntity::toDomain)
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
    public long countByAccountStatus(String accountStatus) {
        return jpaRepository.countByAccountStatus(accountStatus);
    }
}
