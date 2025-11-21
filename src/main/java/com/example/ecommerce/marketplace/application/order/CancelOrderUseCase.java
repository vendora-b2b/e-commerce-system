package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Use case for cancelling an order in the marketplace.
 * Handles validation, inventory release, and order cancellation logic.
 * Follows API specification behavior for CANCELLED status.
 */
@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Executes the order cancellation use case following API spec behavior.
     *
     * @param command the cancellation command containing order ID
     * @return the result indicating success or failure with details
     */
    @Transactional
    public CancelOrderResult execute(CancelOrderCommand command) {
        // Step 1: Validate order ID
        if (command.getOrderId() == null) {
            return CancelOrderResult.failure("Order ID is required", "INVALID_ORDER_ID");
        }

        // Step 2: Find order
        Optional<Order> orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty()) {
            return CancelOrderResult.failure("Order not found", "ORDER_NOT_FOUND");
        }

        Order order = orderOpt.get();

        // Step 3: Check if order can be cancelled (not SHIPPED or DELIVERED)
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            return CancelOrderResult.failure(
                "Cannot cancel shipped or delivered orders",
                "INVALID_STATUS_TRANSITION"
            );
        }

        // Step 4: BEGIN TRANSACTION (via @Transactional)
        // Query all OrderItems for this order
        List<OrderItem> orderItems = order.getOrderItems();
        
        // For each OrderItem: release reserved stock
        for (OrderItem item : orderItems) {
            Inventory inventory = inventoryRepository.findByVariantId(item.getVariantId())
                .orElse(null);
            
            if (inventory == null) {
                // Try fallback to productId
                inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElse(null);
            }
            
            if (inventory != null) {
                try {
                    inventory.releaseReservedStock(item.getQuantity());
                    inventoryRepository.save(inventory);
                } catch (IllegalStateException e) {
                    // Log but continue - inventory might have been already released
                }
            }
        }

        // Step 5: Update Order status to CANCELLED
        try {
            order.markAsCancelled();
        } catch (IllegalStateException e) {
            return CancelOrderResult.failure(e.getMessage(), "CANCELLATION_FAILED");
        }

        // Step 6: Save cancelled order (COMMIT TRANSACTION via @Transactional)
        Order cancelledOrder = orderRepository.save(order);

        // Step 7: Return success result
        return CancelOrderResult.success(cancelledOrder.getId());
    }
}
