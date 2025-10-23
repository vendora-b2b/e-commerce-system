package com.example.ecommerce.marketplace.domain.supplier;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Supplier aggregate root.
 * Defines persistence operations for suppliers following the repository pattern.
 */
public interface SupplierRepository {

    /**
     * Saves a new supplier or updates an existing one.
     * @param supplier the supplier to save
     * @return the saved supplier with generated ID if new
     */
    Supplier save(Supplier supplier);

    /**
     * Finds a supplier by its unique identifier.
     * @param id the supplier ID
     * @return an Optional containing the supplier if found, empty otherwise
     */
    Optional<Supplier> findById(Long id);

    /**
     * Finds a supplier by email address.
     * @param email the email address
     * @return an Optional containing the supplier if found, empty otherwise
     */
    Optional<Supplier> findByEmail(String email);

    /**
     * Finds a supplier by business license number.
     * @param businessLicense the business license
     * @return an Optional containing the supplier if found, empty otherwise
     */
    Optional<Supplier> findByBusinessLicense(String businessLicense);

    /**
     * Finds all verified suppliers.
     * @param verified true for verified suppliers, false for unverified
     * @return list of suppliers with the specified verification status
     */
    List<Supplier> findByVerified(Boolean verified);

    /**
     * Finds suppliers with rating greater than or equal to the specified value.
     * @param minRating the minimum rating threshold
     * @return list of suppliers meeting the rating criteria
     */
    List<Supplier> findByRatingGreaterThanEqual(Double minRating);

    /**
     * Finds all suppliers.
     * @return list of all suppliers
     */
    List<Supplier> findAll();

    /**
     * Checks if a supplier exists with the given email.
     * @param email the email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a supplier exists with the given business license.
     * @param businessLicense the business license
     * @return true if exists, false otherwise
     */
    boolean existsByBusinessLicense(String businessLicense);

    /**
     * Deletes a supplier by its ID.
     * @param id the supplier ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of suppliers.
     * @return the total count
     */
    long count();

    /**
     * Counts verified suppliers.
     * @param verified true for verified, false for unverified
     * @return the count of suppliers with the specified verification status
     */
    long countByVerified(Boolean verified);

    /**
     * Counts suppliers with rating greater than or equal to the specified value.
     * @param minRating the minimum rating threshold
     * @return the count of suppliers meeting the rating criteria
     */
    long countByRatingGreaterThanEqual(Double minRating);
}
