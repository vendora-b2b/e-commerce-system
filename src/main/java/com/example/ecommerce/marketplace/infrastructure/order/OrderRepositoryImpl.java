package com.example.ecommerce.marketplace.infrastructure.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of OrderRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
            .map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return jpaRepository.findByOrderNumber(orderNumber)
            .map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByRetailerId(Long retailerId) {
        return jpaRepository.findByRetailerId(retailerId).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findBySupplierId(Long supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRetailerIdAndStatus(Long retailerId, OrderStatus status) {
        return jpaRepository.findByRetailerIdAndStatus(retailerId, status).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findBySupplierIdAndStatus(Long supplierId, OrderStatus status) {
        return jpaRepository.findBySupplierIdAndStatus(supplierId, status).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByOrderDateBetween(startDate, endDate).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByTotalAmountGreaterThanEqual(Double minAmount) {
        return jpaRepository.findByTotalAmountGreaterThanEqual(minAmount).stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
            .map(OrderEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return jpaRepository.existsByOrderNumber(orderNumber);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long countByRetailerId(Long retailerId) {
        return jpaRepository.countByRetailerId(retailerId);
    }

    @Override
    public long countBySupplierId(Long supplierId) {
        return jpaRepository.countBySupplierId(supplierId);
    }

    @Override
    public boolean existsByProductIdAndStatusIn(Long productId, List<OrderStatus> statuses) {
        return jpaRepository.existsByProductIdAndStatusIn(productId, statuses);
    }

    @Override
    public boolean existsByVariantIdAndStatusIn(Long variantId, List<OrderStatus> statuses) {
        return jpaRepository.existsByVariantIdAndStatusIn(variantId, statuses);
    }
}
