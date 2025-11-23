package com.example.ecommerce.marketplace.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User aggregate root.
 * Defines persistence operations for users following the repository pattern.
 */
public interface UserRepository {

    /**
     * Saves a new user or updates an existing one.
     * @param user the user to save
     * @return the saved user with generated ID if new
     */
    User save(User user);

    /**
     * Finds a user by its unique identifier.
     * @param id the user ID
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by username.
     * @param username the username
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds all users with the specified role.
     * @param role the user role
     * @return list of users with the specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Finds a user by entity ID and role.
     * This is useful for finding the user account associated with a Supplier or Retailer.
     * @param entityId the entity ID (Supplier or Retailer ID)
     * @param role the user role
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEntityIdAndRole(Long entityId, UserRole role);

    /**
     * Finds all enabled users.
     * @param enabled true for enabled users, false for disabled
     * @return list of users with the specified enabled status
     */
    List<User> findByEnabled(Boolean enabled);

    /**
     * Finds all locked user accounts.
     * @param accountLocked true for locked accounts, false for unlocked
     * @return list of users with the specified lock status
     */
    List<User> findByAccountLocked(Boolean accountLocked);

    /**
     * Finds all users (for admin purposes).
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Checks if a user with the given username exists.
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists for the given entity ID and role.
     * @param entityId the entity ID
     * @param role the user role
     * @return true if user exists, false otherwise
     */
    boolean existsByEntityIdAndRole(Long entityId, UserRole role);

    /**
     * Deletes a user by ID.
     * @param id the user ID to delete
     */
    void deleteById(Long id);

    /**
     * Counts all users.
     * @return total number of users
     */
    long count();

    /**
     * Counts users by role.
     * @param role the user role
     * @return number of users with the specified role
     */
    long countByRole(UserRole role);

    /**
     * Counts enabled users.
     * @param enabled true for enabled users, false for disabled
     * @return number of users with the specified enabled status
     */
    long countByEnabled(Boolean enabled);

    /**
     * Counts locked user accounts.
     * @param accountLocked true for locked accounts, false for unlocked
     * @return number of users with the specified lock status
     */
    long countByAccountLocked(Boolean accountLocked);
}
