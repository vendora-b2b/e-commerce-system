package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.OrderItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for OrderItem.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    private String productName;

    /**
     * Converts JPA entity to domain model.
     */
    public OrderItem toDomain() {
        return new OrderItem(
            this.id,
            this.productId,
            this.quantity,
            this.price,
            this.productName
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static OrderItemEntity fromDomain(OrderItem orderItem) {
        return new OrderItemEntity(
            orderItem.getId(),
            null, // Order will be set by the parent
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getPrice(),
            orderItem.getProductName()
        );
    }
}
