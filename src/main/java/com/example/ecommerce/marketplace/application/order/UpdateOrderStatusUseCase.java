package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case for updating an existing order's status.
 * Framework-agnostic, following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    /**
     * Executes the order status update use case.
     *
     * @param command the update command containing order ID and new status
     * @return the result indicating success or failure with details
     */
    public UpdateOrderStatusResult execute(UpdateOrderStatusCommand command) {
        // 1. Validate order ID
        if (command.getOrderId() == null) {
            return UpdateOrderStatusResult.failure("Order ID is required", "INVALID_ORDER_ID");
        }

        // 2. Validate new status
        if (command.getNewStatus() == null) {
            return UpdateOrderStatusResult.failure("New status is required", "INVALID_STATUS");
        }

        // 3. Find order
        Optional<Order> orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty()) {
            return UpdateOrderStatusResult.failure("Order not found", "ORDER_NOT_FOUND");
        }

        Order order = orderOpt.get();

        // 4. Update status using domain logic with proper state transitions
        try {
            OrderStatus newStatus = command.getNewStatus();
            
            // Use domain methods for state transitions
            switch (newStatus) {
                case PROCESSING:
                    order.markAsProcessing();
                    break;
                case SHIPPED:
                    order.markAsShipped();
                    break;
                case DELIVERED:
                    order.markAsDelivered();
                    break;
                case CANCELLED:
                    order.markAsCancelled();
                    break;
                case PENDING:
                    // Cannot go back to PENDING
                    return UpdateOrderStatusResult.failure(
                        "Cannot change order status to PENDING",
                        "INVALID_STATUS_TRANSITION"
                    );
                default:
                    return UpdateOrderStatusResult.failure(
                        "Invalid status transition",
                        "INVALID_STATUS_TRANSITION"
                    );
            }
        } catch (IllegalStateException e) {
            return UpdateOrderStatusResult.failure(e.getMessage(), "INVALID_STATUS_TRANSITION");
        }

        // 5. Save updated order
        Order updatedOrder = orderRepository.save(order);

        // 6. Return success result
        return UpdateOrderStatusResult.success(updatedOrder.getId());
    }
}
