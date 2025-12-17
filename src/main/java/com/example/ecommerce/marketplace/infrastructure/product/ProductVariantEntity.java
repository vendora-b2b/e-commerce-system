package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
            this.priceAdjustment
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
        return entity;
    }
}
