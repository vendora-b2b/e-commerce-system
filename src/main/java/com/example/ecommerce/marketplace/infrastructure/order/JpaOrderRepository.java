package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for OrderEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    List<OrderEntity> findByRetailerId(Long retailerId);

    List<OrderEntity> findBySupplierId(Long supplierId);

    List<OrderEntity> findByStatus(OrderStatus status);

    List<OrderEntity> findByRetailerIdAndStatus(Long retailerId, OrderStatus status);

    List<OrderEntity> findBySupplierIdAndStatus(Long supplierId, OrderStatus status);

    List<OrderEntity> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderEntity> findByTotalAmountGreaterThanEqual(Double minAmount);

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(OrderStatus status);

    long countByRetailerId(Long retailerId);

    long countBySupplierId(Long supplierId);
}
