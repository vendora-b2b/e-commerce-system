package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
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
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_status", columnList = "status"),
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

    @Column(name = "category_id")
    private Long categoryId;

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
    private List<ProductVariantEntity> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceTierEntity> priceTiers;

    @Column(nullable = false, length = 20)
    private String status;

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
        List<ProductVariant> domainVariants = null;
        if (variants != null) {
            domainVariants = variants.stream()
                .map(ProductVariantEntity::toDomain)
                .collect(Collectors.toList());
        }

        List<PriceTier> domainPriceTiers = null;
        if (priceTiers != null) {
            domainPriceTiers = priceTiers.stream()
                .map(PriceTierEntity::toDomain)
                .collect(Collectors.toList());
        }

        return new Product(
            this.id,
            this.sku,
            this.name,
            this.description,
            this.categoryId,
            this.supplierId,
            this.basePrice,
            this.minimumOrderQuantity,
            this.unit,
            this.images != null ? new ArrayList<>(this.images) : null,
            domainVariants,
            domainPriceTiers,
            this.status,
            this.createdAt,
            this.updatedAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = new ProductEntity(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getCategoryId(),
            product.getSupplierId(),
            product.getBasePrice(),
            product.getMinimumOrderQuantity(),
            product.getUnit(),
            product.getImages() != null ? new ArrayList<>(product.getImages()) : null,
            null, // variants set below
            null, // priceTiers set below
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );

        // Map variants
        if (product.getVariants() != null) {
            List<ProductVariantEntity> variantEntities = product.getVariants().stream()
                .map(v -> ProductVariantEntity.fromDomain(v, entity))
                .collect(Collectors.toList());
            entity.setVariants(variantEntities);
        }

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
