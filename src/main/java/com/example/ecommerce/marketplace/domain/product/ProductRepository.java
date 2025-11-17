package com.example.ecommerce.marketplace.domain.product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product aggregate root.
 * Defines persistence operations for products following the repository pattern.
 * This interface is part of the domain layer and is framework-agnostic.
 */
public interface ProductRepository {

    /**
     * Saves a new product or updates an existing one.
     * @param product the product to save
     * @return the saved product with generated ID if new
     */
    Product save(Product product);

    /**
     * Finds a product by its unique identifier.
     * @param id the product ID
     * @return an Optional containing the product if found, empty otherwise
     */
    Optional<Product> findById(Long id);

    /**
     * Finds a product by SKU (Stock Keeping Unit).
     * SKU is unique across all products.
     * @param sku the product SKU
     * @return an Optional containing the product if found, empty otherwise
     */
    Optional<Product> findBySku(String sku);

    /**
     * Finds all products belonging to a specific supplier.
     * @param supplierId the supplier ID
     * @return list of products from the supplier
     */
    List<Product> findBySupplierId(Long supplierId);

    /**
     * Finds all products in a specific category.
     * @param categoryId the product category ID
     * @return list of products in the category
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Finds all products with a specific status.
     * @param status the product status (ACTIVE, INACTIVE, DISCONTINUED)
     * @return list of products with the specified status
     */
    List<Product> findByStatus(String status);

    /**
     * Finds products by supplier and status.
     * Useful for getting active products from a supplier.
     * @param supplierId the supplier ID
     * @param status the product status
     * @return list of products matching both criteria
     */
    List<Product> findBySupplierIdAndStatus(Long supplierId, String status);

    /**
     * Finds products within a price range.
     * @param minPrice the minimum base price (inclusive)
     * @param maxPrice the maximum base price (inclusive)
     * @return list of products within the price range
     */
    List<Product> findByBasePriceBetween(Double minPrice, Double maxPrice);

    /**
     * Finds products by category and status.
     * Useful for browsing active products in a category.
     * @param categoryId the product category ID
     * @param status the product status
     * @return list of products matching both criteria
     */
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status);

    /**
     * Searches products by name containing a keyword (case-insensitive).
     * @param keyword the search keyword
     * @return list of products with matching names
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Finds all products.
     * Use with caution in production - consider pagination.
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Checks if a product exists with the given SKU.
     * @param sku the product SKU
     * @return true if exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Checks if a product exists with the given ID.
     * @param id the product ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Deletes a product by its ID.
     * Note: Consider using soft delete (status = DISCONTINUED) instead.
     * @param id the product ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of products.
     * @return the total count
     */
    long count();

    /**
     * Counts products by status.
     * @param status the product status
     * @return the count of products with the specified status
     */
    long countByStatus(String status);

    /**
     * Counts products by supplier.
     * @param supplierId the supplier ID
     * @return the count of products from the supplier
     */
    long countBySupplierId(Long supplierId);

    /**
     * Counts products by category.
     * @param categoryId the product category ID
     * @return the count of products in the category
     */
    long countByCategoryId(Long categoryId);

    /**
     * Finds products with minimum order quantity less than or equal to a value.
     * Useful for finding products suitable for small orders.
     * @param maxMoq the maximum minimum order quantity
     * @return list of products with MOQ <= maxMoq
     */
    List<Product> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq);

    /**
     * Finds active products by supplier.
     * Convenience method combining supplier filter and active status.
     * @param supplierId the supplier ID
     * @return list of active products from the supplier
     */
    default List<Product> findActiveProductsBySupplierId(Long supplierId) {
        return findBySupplierIdAndStatus(supplierId, "ACTIVE");
    }

    /**
     * Finds active products by category.
     * Convenience method combining category filter and active status.
     * @param categoryId the product category ID
     * @return list of active products in the category
     */
    default List<Product> findActiveProductsByCategoryId(Long categoryId) {
        return findByCategoryIdAndStatus(categoryId, "ACTIVE");
    }
}
