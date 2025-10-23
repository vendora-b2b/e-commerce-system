package com.example.ecommerce.marketplace.domain.retailer;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Retailer aggregate root.
 * Defines persistence operations for retailers following the repository pattern.
 */
public interface RetailerRepository {

    /**
     * Saves a new retailer or updates an existing one.
     * @param retailer the retailer to save
     * @return the saved retailer with generated ID if new
     */
    Retailer save(Retailer retailer);

    /**
     * Finds a retailer by its unique identifier.
     * @param id the retailer ID
     * @return an Optional containing the retailer if found, empty otherwise
     */
    Optional<Retailer> findById(Long id);

    /**
     * Finds a retailer by email address.
     * @param email the email address
     * @return an Optional containing the retailer if found, empty otherwise
     */
    Optional<Retailer> findByEmail(String email);

    /**
     * Finds a retailer by business license number.
     * @param businessLicense the business license
     * @return an Optional containing the retailer if found, empty otherwise
     */
    Optional<Retailer> findByBusinessLicense(String businessLicense);

    /**
     * Finds all retailers with a specific account status.
     * @param accountStatus the account status (ACTIVE, SUSPENDED, INACTIVE)
     * @return list of retailers with the specified status
     */
    List<Retailer> findByAccountStatus(String accountStatus);

    /**
     * Finds all retailers by loyalty tier.
     * @param loyaltyTier the loyalty tier (e.g., BRONZE, SILVER, GOLD)
     * @return list of retailers in the specified tier
     */
    List<Retailer> findByLoyaltyTier(String loyaltyTier);

    /**
     * Finds all retailers.
     * @return list of all retailers
     */
    List<Retailer> findAll();

    /**
     * Checks if a retailer exists with the given email.
     * @param email the email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a retailer exists with the given business license.
     * @param businessLicense the business license
     * @return true if exists, false otherwise
     */
    boolean existsByBusinessLicense(String businessLicense);

    /**
     * Deletes a retailer by its ID.
     * @param id the retailer ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of retailers.
     * @return the total count
     */
    long count();

    /**
     * Counts retailers by account status.
     * @param accountStatus the account status
     * @return the count of retailers with the specified status
     */
    long countByAccountStatus(String accountStatus);
}
