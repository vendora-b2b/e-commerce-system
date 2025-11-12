package com.example.ecommerce.marketplace.infrastructure.user;

import com.example.ecommerce.marketplace.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository interface for UserEntity.
 * Provides basic CRUD operations and custom query methods.
 */
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by username.
     * @param username the username
     * @return an Optional containing the user entity if found
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Finds a user by email.
     * @param email the email address
     * @return an Optional containing the user entity if found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds all users with the specified role.
     * @param role the user role
     * @return list of user entities with the specified role
     */
    List<UserEntity> findByRole(UserRole role);

    /**
     * Finds a user by entity ID and role.
     * @param entityId the entity ID
     * @param role the user role
     * @return an Optional containing the user entity if found
     */
    Optional<UserEntity> findByEntityIdAndRole(Long entityId, UserRole role);

    /**
     * Finds all users with the specified enabled status.
     * @param enabled true for enabled users, false for disabled
     * @return list of user entities
     */
    List<UserEntity> findByEnabled(Boolean enabled);

    /**
     * Finds all users with the specified account locked status.
     * @param accountLocked true for locked accounts, false for unlocked
     * @return list of user entities
     */
    List<UserEntity> findByAccountLocked(Boolean accountLocked);

    /**
     * Checks if a user with the given username exists.
     * @param username the username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email exists.
     * @param email the email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists for the given entity ID and role.
     * @param entityId the entity ID
     * @param role the user role
     * @return true if exists, false otherwise
     */
    boolean existsByEntityIdAndRole(Long entityId, UserRole role);

    /**
     * Counts users by role.
     * @param role the user role
     * @return number of users with the specified role
     */
    long countByRole(UserRole role);

    /**
     * Counts users by enabled status.
     * @param enabled true for enabled users, false for disabled
     * @return number of users with the specified enabled status
     */
    long countByEnabled(Boolean enabled);

    /**
     * Counts users by account locked status.
     * @param accountLocked true for locked accounts, false for unlocked
     * @return number of users with the specified lock status
     */
    long countByAccountLocked(Boolean accountLocked);
}
