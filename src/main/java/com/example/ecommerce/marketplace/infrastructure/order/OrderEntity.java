package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
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
 * JPA entity for Order.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private Long retailerId;

    @Column(nullable = false)
    private Long supplierId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 500)
    private String shippingAddress;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime deliveryDate;

    /**
     * Converts JPA entity to domain model.
     */
    public Order toDomain() {
        List<OrderItem> domainItems = null;
        if (this.orderItems != null) {
            domainItems = this.orderItems.stream()
                .map(OrderItemEntity::toDomain)
                .collect(Collectors.toList());
        }

        return new Order(
            this.id,
            this.orderNumber,
            this.retailerId,
            this.supplierId,
            domainItems,
            this.totalAmount,
            this.status,
            this.shippingAddress,
            this.orderDate,
            this.deliveryDate
        );
    }                                 

    /**
     * Creates JPA entity from domain model.
     */
    public static OrderEntity fromDomain(Order order) {
        OrderEntity entity = new OrderEntity(
            order.getId(),
            order.getOrderNumber(),
            order.getRetailerId(),
            order.getSupplierId(),
            new ArrayList<>(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getShippingAddress(),
            order.getOrderDate(),
            order.getDeliveryDate()
        );

        // Convert order items
        if (order.getOrderItems() != null) {
            for (OrderItem domainItem : order.getOrderItems()) {
                OrderItemEntity itemEntity = OrderItemEntity.fromDomain(domainItem);
                itemEntity.setOrder(entity);
                entity.getOrderItems().add(itemEntity);
            }
        }

        return entity;
    }
}
