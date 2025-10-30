package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Price Tier.
 * Represents bulk pricing tiers for products.
 */
@Entity
@Table(name = "price_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceTierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private Integer minQuantity;

    @Column
    private Integer maxQuantity;

    @Column(nullable = false)
    private Double pricePerUnit;

    @Column
    private Double discountPercent;

    /**
     * Converts JPA entity to domain model.
     */
    public Product.PriceTier toDomain() {
        return new Product.PriceTier(
            this.id,
            this.minQuantity,
            this.maxQuantity,
            this.pricePerUnit,
            this.discountPercent
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static PriceTierEntity fromDomain(Product.PriceTier tier, ProductEntity product) {
        return new PriceTierEntity(
            tier.getId(),
            product,
            tier.getMinQuantity(),
            tier.getMaxQuantity(),
            tier.getPricePerUnit(),
            tier.getDiscountPercent()
        );
    }
}
