package com.example.ecommerce.marketplace.domain.user;

/**
 * Enum representing user roles in the e-commerce marketplace.
 * Supports Role-Based Access Control (RBAC) between Suppliers and Retailers.
 */
public enum UserRole {
    /**
     * Supplier role - for businesses that provide products.
     */
    SUPPLIER,
    
    /**
     * Retailer role - for businesses that purchase products.
     */
    RETAILER,
    
    /**
     * Admin role - for system administrators (optional for future use).
     */
    ADMIN
}
