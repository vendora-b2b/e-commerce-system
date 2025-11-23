package com.example.ecommerce.marketplace.domain.order;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order entity in the e-commerce marketplace.
 * Orders are created by retailers to purchase products from suppliers.
 */
@Getter
@Setter
public class Order {

    private static final Integer MINIMUM_ORDER_QUANTITY = 1;

    private Long id;
    private String orderNumber;
    private Long retailerId;
    private Long supplierId;
    private List<OrderItem> orderItems;
    private Double totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    public Order() {
        this.orderItems = new ArrayList<>();
    }

    public Order(Long id, String orderNumber, Long retailerId, Long supplierId,
                 List<OrderItem> orderItems, Double totalAmount, OrderStatus status,
                 String shippingAddress, LocalDateTime orderDate, LocalDateTime deliveryDate) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.retailerId = retailerId;
        this.supplierId = supplierId;
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
    }

    /**
     * Calculates the total amount of the order based on order items.
     * @return total amount
     */
    public Double calculateTotalAmount() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (OrderItem item : orderItems) {
            if (item.getQuantity() != null && item.getPrice() != null) {
                total += item.getQuantity() * item.getPrice();
            }
        }

        // Round to 2 decimal places
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Applies a discount to the order.
     * @param discountAmount the discount amount to apply
     */
    public void applyDiscount(Double discountAmount) {
        if (discountAmount == null) {
            throw new IllegalArgumentException("Discount amount cannot be null");
        }
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        if (this.totalAmount == null) {
            this.totalAmount = calculateTotalAmount();
        }
        if (discountAmount > this.totalAmount) {
            throw new IllegalArgumentException("Discount amount cannot exceed total amount");
        }

        this.totalAmount = this.totalAmount - discountAmount;
        // Round to 2 decimal places
        this.totalAmount = Math.round(this.totalAmount * 100.0) / 100.0;
    }

    /**
     * Validates if the order meets minimum order quantity requirements.
     * @return true if minimum order quantity is met, false otherwise
     */
    public boolean validateMinimumOrderQuantity() {
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        int totalQuantity = 0;
        for (OrderItem item : orderItems) {
            if (item.getQuantity() != null) {
                totalQuantity += item.getQuantity();
            }
        }

        return totalQuantity >= MINIMUM_ORDER_QUANTITY;
    }

    /**
     * Validates the order number format.
     * @return true if order number is valid, false otherwise
     */
    public boolean validateOrderNumber() {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return false;
        }
        // Order number should be at least 5 characters and alphanumeric with hyphens allowed
        return orderNumber.trim().length() >= 5 &&
               orderNumber.trim().matches("^[A-Za-z0-9-]+$");
    }

    /**
     * Checks if the order can be cancelled based on current status.
     * @return true if order can be cancelled, false otherwise
     */
    public boolean canBeCancelled() {
        if (status == null) {
            return false;
        }
        // Can only cancel if order is PENDING or CONFIRMED (not SHIPPED or DELIVERED)
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Checks if the order has been delivered.
     * @return true if delivered, false otherwise
     */
    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    /**
     * Checks if the order is pending.
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Marks the order as confirmed.
     */
    public void markAsConfirmed() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be marked as confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Marks the order as shipped.
     */
    public void markAsShipped() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be marked as shipped");
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * Marks the order as delivered.
     */
    public void markAsDelivered() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be marked as delivered");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveryDate = LocalDateTime.now();
    }

    /**
     * Marks the order as cancelled.
     */
    public void markAsCancelled() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Updates the order details.
     * @param shippingAddress new shipping address
     * @param deliveryDate new delivery date
     */
    public void updateOrderDetails(String shippingAddress, LocalDateTime deliveryDate) {
        if (shippingAddress != null && !shippingAddress.trim().isEmpty()) {
            this.shippingAddress = shippingAddress.trim();
        }
        if (deliveryDate != null) {
            this.deliveryDate = deliveryDate;
        }
    }

    /**
     * Adds an order item to the order.
     * @param item the order item to add
     */
    public void addOrderItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(item);
        // Recalculate total amount
        this.totalAmount = calculateTotalAmount();
    }
}
