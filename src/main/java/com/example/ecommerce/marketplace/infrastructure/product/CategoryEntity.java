package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for Category.
 * Represents product categories in the database.
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_parent_category_id", columnList = "parent_category_id"),
    @Index(name = "idx_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column
    private Integer level;

    @Column(length = 200)
    private String slug;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Converts JPA entity to domain model.
     */
    public Category toDomain() {
        return new Category(
            this.id,
            this.name,
            this.parentCategoryId,
            this.level,
            this.slug,
            this.createdAt,
            this.updatedAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static CategoryEntity fromDomain(Category category) {
        return new CategoryEntity(
            category.getId(),
            category.getName(),
            category.getParentCategoryId(),
            category.getLevel(),
            category.getSlug(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
