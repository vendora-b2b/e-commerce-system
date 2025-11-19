package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Product Variant.
 * Represents different variations of a product (e.g., colors, sizes).
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String size;

    @Column
    private Double priceAdjustment;

    @ElementCollection
    @CollectionTable(name = "product_variant_images", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Converts JPA entity to domain model.
     */
    public ProductVariant toDomain() {
        return new ProductVariant(
            this.id,
            this.productId,
            this.sku,
            this.color,
            this.size,
            this.priceAdjustment,
            this.images != null ? new ArrayList<>(this.images) : null,
            this.createdAt,
            this.updatedAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static ProductVariantEntity fromDomain(ProductVariant variant, ProductEntity product) {
        ProductVariantEntity entity = new ProductVariantEntity();
        entity.setId(variant.getId());
        entity.setProductId(variant.getProductId());
        entity.setSku(variant.getSku());
        entity.setColor(variant.getColor());
        entity.setSize(variant.getSize());
        entity.setPriceAdjustment(variant.getPriceAdjustment());
        entity.setImages(variant.getImages() != null ? new ArrayList<>(variant.getImages()) : null);
        entity.setCreatedAt(variant.getCreatedAt());
        entity.setUpdatedAt(variant.getUpdatedAt());
        return entity;
    }
}
