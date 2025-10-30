package com.example.ecommerce.marketplace.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order aggregate root.
 * Defines persistence operations for orders following the repository pattern.
 */
public interface OrderRepository {

    /**
     * Saves a new order or updates an existing one.
     * @param order the order to save
     * @return the saved order with generated ID if new
     */
    Order save(Order order);

    /**
     * Finds an order by its unique identifier.
     * @param id the order ID
     * @return an Optional containing the order if found, empty otherwise
     */
    Optional<Order> findById(Long id);

    /**
     * Finds an order by order number.
     * @param orderNumber the order number
     * @return an Optional containing the order if found, empty otherwise
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Finds all orders by retailer ID.
     * @param retailerId the retailer ID
     * @return list of orders for the retailer
     */
    List<Order> findByRetailerId(Long retailerId);

    /**
     * Finds all orders by supplier ID.
     * @param supplierId the supplier ID
     * @return list of orders for the supplier
     */
    List<Order> findBySupplierId(Long supplierId);

    /**
     * Finds all orders with a specific status.
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders by retailer and status.
     * @param retailerId the retailer ID
     * @param status the order status
     * @return list of matching orders
     */
    List<Order> findByRetailerIdAndStatus(Long retailerId, OrderStatus status);

    /**
     * Finds orders by supplier and status.
     * @param supplierId the supplier ID
     * @param status the order status
     * @return list of matching orders
     */
    List<Order> findBySupplierIdAndStatus(Long supplierId, OrderStatus status);

    /**
     * Finds orders within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of orders within the date range
     */
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds orders with total amount greater than or equal to the specified value.
     * @param minAmount the minimum total amount
     * @return list of orders meeting the amount criteria
     */
    List<Order> findByTotalAmountGreaterThanEqual(Double minAmount);

    /**
     * Finds all orders.
     * @return list of all orders
     */
    List<Order> findAll();

    /**
     * Checks if an order exists with the given order number.
     * @param orderNumber the order number
     * @return true if exists, false otherwise
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Deletes an order by its ID.
     * @param id the order ID
     */
    void deleteById(Long id);

    /**
     * Counts the total number of orders.
     * @return the total count
     */
    long count();

    /**
     * Counts orders by status.
     * @param status the order status
     * @return the count of orders with the specified status
     */
    long countByStatus(OrderStatus status);

    /**
     * Counts orders by retailer ID.
     * @param retailerId the retailer ID
     * @return the count of orders for the retailer
     */
    long countByRetailerId(Long retailerId);

    /**
     * Counts orders by supplier ID.
     * @param supplierId the supplier ID
     * @return the count of orders for the supplier
     */
    long countBySupplierId(Long supplierId);
}
