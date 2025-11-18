package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA entity for Product.
 * This is the persistence model, separate from the domain model.
 * Handles mapping between database tables and domain objects.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_supplier_id", columnList = "supplier_id"),
    @Index(name = "idx_sku", columnList = "sku")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Integer minimumOrderQuantity;

    @Column(nullable = false, length = 50)
    private String unit;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceTierEntity> priceTiers;

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
    public Product toDomain() {
        List<PriceTier> domainPriceTiers = null;
        if (priceTiers != null) {
            domainPriceTiers = priceTiers.stream()
                .map(PriceTierEntity::toDomain)
                .collect(Collectors.toList());
        }

        List<Category> domainCategories = null;
        if (categories != null) {
            domainCategories = categories.stream()
                .map(CategoryEntity::toDomain)
                .collect(Collectors.toList());
        }

        return new Product(
            this.id,
            this.sku,
            this.name,
            this.description,
            domainCategories,
            this.supplierId,
            this.basePrice,
            this.minimumOrderQuantity,
            this.unit,
            this.images != null ? new ArrayList<>(this.images) : null,
            domainPriceTiers,
            this.createdAt,
            this.updatedAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static ProductEntity fromDomain(Product product) {
        List<CategoryEntity> categoryEntities = null;
        if (product.getCategories() != null) {
            categoryEntities = product.getCategories().stream()
                .map(CategoryEntity::fromDomain)
                .collect(Collectors.toList());
        }

        ProductEntity entity = new ProductEntity(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            categoryEntities,
            product.getSupplierId(),
            product.getBasePrice(),
            product.getMinimumOrderQuantity(),
            product.getUnit(),
            product.getImages() != null ? new ArrayList<>(product.getImages()) : null,
            null, // priceTiers set below
            product.getCreatedAt(),
            product.getUpdatedAt()
        );

        // Map price tiers
        if (product.getPriceTiers() != null) {
            List<PriceTierEntity> tierEntities = product.getPriceTiers().stream()
                .map(t -> PriceTierEntity.fromDomain(t, entity))
                .collect(Collectors.toList());
            entity.setPriceTiers(tierEntities);
        }

        return entity;
    }
}
