package com.example.ecommerce.marketplace.domain.order;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an order item within an order.
 * Each order item contains product information, quantity, and price.
 */
@Getter
@Setter
public class OrderItem {

    private Long id;
    private Long productId;
    private Long variantId;  // NULL if no variant selected
    private Integer quantity;
    private Double price;
    private String productName;

    public OrderItem() {
    }

    public OrderItem(Long id, Long productId, Long variantId, Integer quantity, Double price, String productName) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.price = price;
        this.productName = productName;
    }

    /**
     * Validates the order item.
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        if (productId == null) {
            return false;
        }
        if (quantity == null || quantity <= 0) {
            return false;
        }
        if (price == null || price < 0) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the subtotal for this order item.
     * @return subtotal (quantity * price)
     */
    public Double calculateSubtotal() {
        if (quantity == null || price == null) {
            return 0.0;
        }
        double subtotal = quantity * price;
        // Round to 2 decimal places
        return Math.round(subtotal * 100.0) / 100.0;
    }

    /**
     * Updates the quantity.
     * @param newQuantity the new quantity
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = newQuantity;
    }

    /**
     * Updates the price.
     * @param newPrice the new price
     */
    public void updatePrice(Double newPrice) {
        if (newPrice == null || newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
    }
}
