package com.example.ecommerce.marketplace.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for CategoryEntity.
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, Long> {

    /**
     * Find category by slug.
     */
    Optional<CategoryEntity> findBySlug(String slug);

    /**
     * Find category by name (case-insensitive).
     */
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
