package com.example.ecommerce.marketplace.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Finds all products that belong to a specific category.
     * @param categoryId the category ID
     * @return list of products in the category
     */
    List<Product> findByCategory(Long categoryId);

    /**
     * Finds products within a price range.
     * @param minPrice the minimum base price (inclusive)
     * @param maxPrice the maximum base price (inclusive)
     * @return list of products within the price range
     */
    List<Product> findByBasePriceBetween(Double minPrice, Double maxPrice);

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
     * @param id the product ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of products.
     * @return the total count
     */
    long count();

    /**
     * Counts products by supplier.
     * @param supplierId the supplier ID
     * @return the count of products from the supplier
     */
    long countBySupplierId(Long supplierId);

    /**
     * Counts products by category.
     * @param categoryId the category ID
     * @return the count of products in the category
     */
    long countByCategory(Long categoryId);

    /**
     * Finds products with minimum order quantity less than or equal to a value.
     * Useful for finding products suitable for small orders.
     * @param maxMoq the maximum minimum order quantity
     * @return list of products with MOQ <= maxMoq
     */
    List<Product> findByMinimumOrderQuantityLessThanEqual(Integer maxMoq);

    /**
     * Finds products with filters and pagination.
     * @param sku optional SKU filter
     * @param supplierId optional supplier ID filter
     * @param categorySlug optional category slug filter
     * @param pageable pagination parameters
     * @return page of products matching the filters
     */
    Page<Product> findWithFilters(String sku, Long supplierId, String categorySlug, Pageable pageable);
}
