package com.example.ecommerce.marketplace.domain.user;

import java.time.LocalDateTime;

/**
 * Represents a user entity in the e-commerce marketplace.
 * Users can be either suppliers or retailers and are used for authentication and authorization.
 * Implements Role-Based Access Control (RBAC) between Supplier and Retailer.
 */
public class User {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;

    private Long id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private Long entityId; // References either Supplier or Retailer ID
    private Boolean enabled;
    private Boolean accountLocked;
    private Integer failedLoginAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(Long id, String username, String passwordHash, UserRole role,
                Long entityId, Boolean enabled, Boolean accountLocked, Integer failedLoginAttempts,
                LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.entityId = entityId;
        this.enabled = enabled;
        this.accountLocked = accountLocked;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Validates the username format and length.
     * @return true if username is valid, false otherwise
     */
    public boolean validateUsername() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String trimmedUsername = username.trim();
        return trimmedUsername.length() >= MIN_USERNAME_LENGTH &&
               trimmedUsername.length() <= MAX_USERNAME_LENGTH &&
               trimmedUsername.matches("^[A-Za-z0-9_.-]+$");
    }

    /**
     * Validates the password meets minimum security requirements.
     * @param password the plain text password to validate
     * @return true if password is valid, false otherwise
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        // Password must contain at least one uppercase, one lowercase, one digit, and one special character
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Validates the user role is assigned.
     * @return true if role is valid, false otherwise
     */
    public boolean validateRole() {
        return role != null;
    }

    /**
     * Validates the entity ID reference is present.
     * @return true if entity ID is valid, false otherwise
     */
    public boolean validateEntityId() {
        return entityId != null && entityId > 0;
    }

    /**
     * Validates all user fields.
     * @return true if all validations pass, false otherwise
     */
    public boolean validate() {
        return validateUsername() &&
               validateRole() &&
               validateEntityId();
    }

    /**
     * Checks if the user is enabled and can authenticate.
     * @return true if user is enabled and not locked, false otherwise
     */
    public boolean canAuthenticate() {
        return (enabled != null && enabled) && 
               (accountLocked == null || !accountLocked);
    }

    /**
     * Checks if the user has the specified role.
     * @param requiredRole the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.role != null && this.role == requiredRole;
    }

    /**
     * Checks if the user is a supplier.
     * @return true if user has SUPPLIER role, false otherwise
     */
    public boolean isSupplier() {
        return hasRole(UserRole.SUPPLIER);
    }

    /**
     * Checks if the user is a retailer.
     * @return true if user has RETAILER role, false otherwise
     */
    public boolean isRetailer() {
        return hasRole(UserRole.RETAILER);
    }

    /**
     * Records a successful login attempt.
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Records a failed login attempt and locks account if threshold exceeded.
     * @param maxAttempts maximum allowed failed attempts before locking
     */
    public void recordFailedLogin(int maxAttempts) {
        if (this.failedLoginAttempts == null) {
            this.failedLoginAttempts = 0;
        }
        this.failedLoginAttempts++;
        
        if (this.failedLoginAttempts >= maxAttempts) {
            this.accountLocked = true;
        }
    }

    /**
     * Unlocks the user account and resets failed login attempts.
     */
    public void unlockAccount() {
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
    }

    /**
     * Locks the user account.
     */
    public void lockAccount() {
        this.accountLocked = true;
    }

    /**
     * Enables the user account.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disables the user account.
     */
    public void disable() {
        this.enabled = false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
