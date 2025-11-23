package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
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
 * Use case for updating an existing order (status, deliveryDate, item prices).
 * Follows API specification PATCH /api/v1/orders/{id} behavior.
 */
@Service
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Executes the order update use case following API spec PATCH behavior.
     *
     * @param command the update command containing order ID and fields to update
     * @return the result indicating success or failure with details
     */
    @Transactional
    public UpdateOrderStatusResult execute(UpdateOrderCommand command) {
        // Step 1: Query Order by ID
        Optional<Order> orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty()) {
            return UpdateOrderStatusResult.failure("Order not found", "ORDER_NOT_FOUND");
        }

        Order order = orderOpt.get();

        // Step 2: If status provided, update status with inventory management
        if (command.getNewStatus() != null) {
            OrderStatus newStatus = command.getNewStatus();
            OrderStatus currentStatus = order.getStatus();

            // Validate status transition
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                return UpdateOrderStatusResult.failure(
                    "Invalid status transition from " + currentStatus + " to " + newStatus,
                    "INVALID_STATUS_TRANSITION"
                );
            }

            try {
                // Handle SHIPPED status - deduct from inventory
                if (newStatus == OrderStatus.SHIPPED) {
                    // Query all OrderItems for this order
                    List<OrderItem> orderItems = order.getOrderItems();
                    
                    // For each OrderItem: deduct from inventory
                    for (OrderItem item : orderItems) {
                        Inventory inventory = inventoryRepository.findByVariantId(item.getVariantId())
                            .orElse(null);
                        
                        if (inventory == null) {
                            // Try fallback to productId
                            inventory = inventoryRepository.findByProductId(item.getProductId())
                                .orElse(null);
                        }
                        
                        if (inventory != null) {
                            // Deduct from both available and reserved
                            inventory.deductStock(item.getQuantity());
                            inventory.releaseReservedStock(item.getQuantity());
                            inventoryRepository.save(inventory);
                        }
                    }
                    
                    order.markAsShipped();
                }
                // Handle CANCELLED status - release reserved inventory
                else if (newStatus == OrderStatus.CANCELLED) {
                    // Check current status is not SHIPPED or DELIVERED
                    if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
                        return UpdateOrderStatusResult.failure(
                            "Cannot cancel shipped or delivered orders",
                            "INVALID_STATUS_TRANSITION"
                        );
                    }
                    
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
                            inventory.releaseReservedStock(item.getQuantity());
                            inventoryRepository.save(inventory);
                        }
                    }
                    
                    order.markAsCancelled();
                }
                // Handle other status updates
                else if (newStatus == OrderStatus.CONFIRMED) {
                    order.markAsConfirmed();
                }
                else if (newStatus == OrderStatus.DELIVERED) {
                    order.markAsDelivered();
                }
                else if (newStatus == OrderStatus.PENDING) {
                    return UpdateOrderStatusResult.failure(
                        "Cannot change order status to PENDING",
                        "INVALID_STATUS_TRANSITION"
                    );
                }
                
            } catch (IllegalStateException e) {
                return UpdateOrderStatusResult.failure(e.getMessage(), "INVALID_STATUS_TRANSITION");
            }
        }

        // Step 3: If deliveryDate provided, update it
        if (command.getDeliveryDate() != null) {
            order.setDeliveryDate(command.getDeliveryDate());
        }

        // Step 4: If items provided, update item prices and recalculate total
        if (command.getItemPriceUpdates() != null && !command.getItemPriceUpdates().isEmpty()) {
            for (UpdateOrderCommand.OrderItemPriceUpdate priceUpdate : command.getItemPriceUpdates()) {
                // Find the order item
                OrderItem orderItem = order.getOrderItems().stream()
                    .filter(item -> item.getId().equals(priceUpdate.getOrderItemId()))
                    .findFirst()
                    .orElse(null);
                
                if (orderItem == null) {
                    return UpdateOrderStatusResult.failure(
                        "Order item not found with ID: " + priceUpdate.getOrderItemId(),
                        "ORDER_ITEM_NOT_FOUND"
                    );
                }
                
                // Validate finalTotalPrice is positive
                if (priceUpdate.getFinalTotalPrice() == null || priceUpdate.getFinalTotalPrice() <= 0) {
                    return UpdateOrderStatusResult.failure(
                        "Final total price must be positive",
                        "INVALID_PRICE"
                    );
                }
                
                // Update OrderItem price
                orderItem.setPrice(priceUpdate.getFinalTotalPrice());
            }
            
            // Recalculate totalAmount
            Double totalAmount = order.calculateTotalAmount();
            order.setTotalAmount(totalAmount);
        }

        // Step 5: Save updated order (COMMIT TRANSACTION via @Transactional)
        // Note: updatedAt timestamp will be set automatically by JPA
        Order updatedOrder = orderRepository.save(order);

        // Step 7: Return 200 OK with updated order
        return UpdateOrderStatusResult.success(updatedOrder);
    }

    /**
     * Validates if the status transition is allowed per API spec.
     */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus newStatus) {
        if (current == null || newStatus == null) {
            return false;
        }
        
        // PENDING → CONFIRMED: allowed
        if (current == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            return true;
        }
        
        // CONFIRMED → SHIPPED: allowed
        if (current == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED) {
            return true;
        }
        
        // SHIPPED → DELIVERED: allowed
        if (current == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            return true;
        }
        
        // Any status → CANCELLED: allowed (except DELIVERED)
        if (newStatus == OrderStatus.CANCELLED && current != OrderStatus.DELIVERED) {
            return true;
        }
        
        return false;
    }
}
