package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

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

    /**
     * Converts JPA entity to domain model.
     */
    public Product.ProductVariant toDomain() {
        return new Product.ProductVariant(
            this.id,
            this.product != null ? this.product.getId() : null,
            this.sku,
            this.color,
            this.size,
            this.priceAdjustment,
            this.images != null ? new ArrayList<>(this.images) : null
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static ProductVariantEntity fromDomain(Product.ProductVariant variant, ProductEntity product) {
        return new ProductVariantEntity(
            variant.getId(),
            product,
            variant.getSku(),
            variant.getColor(),
            variant.getSize(),
            variant.getPriceAdjustment(),
            variant.getImages() != null ? new ArrayList<>(variant.getImages()) : null
        );
    }
}
