package com.example.ecommerce.marketplace.infrastructure.product;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder for ProductEntity queries.
 * Provides dynamic query construction for product filtering.
 *
 * This class follows the Specification pattern to build type-safe,
 * composable database queries without hard-coding method combinations.
 */
public class ProductSpecifications {

    /**
     * Creates a combined Specification based on provided filters.
     * All non-null filters are combined with AND logic.
     *
     * @param sku optional SKU filter (exact match)
     * @param supplierName optional supplier name filter (substring match, case-insensitive)
     * @param categorySlug optional category slug filter (joins categories table)
     * @param minPrice optional minimum price filter (inclusive)
     * @param maxPrice optional maximum price filter (inclusive)
     * @return combined Specification (returns all products if all filters are null)
     */
    public static Specification<ProductEntity> withFilters(
            String sku,
            String supplierName,
            String categorySlug,
            Double minPrice,
            Double maxPrice
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // SKU filter (exact match)
            if (sku != null && !sku.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("sku"), sku));
            }

            // Supplier name filter (substring match, case-insensitive)
            if (supplierName != null && !supplierName.trim().isEmpty()) {
                // Join with suppliers table to access supplier name
                Join<Object, Object> supplierJoin = root.join("supplier", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(supplierJoin.get("name")),
                    "%" + supplierName.toLowerCase() + "%"
                ));
            }

            // Category slug filter (requires join with categories table)
            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                // Join with categories (ManyToMany relationship)
                Join<ProductEntity, CategoryEntity> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(categoryJoin.get("slug"), categorySlug));
            }

            // Price range filters
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }

            // Handle DISTINCT for category joins to avoid duplicate products
            if (categorySlug != null && !categorySlug.trim().isEmpty() && query != null) {
                query.distinct(true);
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Individual specification for SKU filtering.
     * Useful for composing custom queries.
     */
    public static Specification<ProductEntity> hasSku(String sku) {
        return (root, query, criteriaBuilder) -> {
            if (sku == null || sku.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter
            }
            return criteriaBuilder.equal(root.get("sku"), sku);
        };
    }

    /**
     * Individual specification for supplier filtering by name.
     */
    public static Specification<ProductEntity> hasSupplierName(String supplierName) {
        return (root, query, criteriaBuilder) -> {
            if (supplierName == null || supplierName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> supplierJoin = root.join("supplier", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(supplierJoin.get("name")),
                "%" + supplierName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Individual specification for category filtering.
     */
    public static Specification<ProductEntity> hasCategorySlug(String categorySlug) {
        return (root, query, criteriaBuilder) -> {
            if (categorySlug == null || categorySlug.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<ProductEntity, CategoryEntity> categoryJoin = root.join("categories", JoinType.INNER);
            if (query != null) {
                query.distinct(true);
            }
            return criteriaBuilder.equal(categoryJoin.get("slug"), categorySlug);
        };
    }

    /**
     * Individual specification for price range filtering.
     */
    public static Specification<ProductEntity> hasPriceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
