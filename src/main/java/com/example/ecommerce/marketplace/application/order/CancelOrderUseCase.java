package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case for cancelling an order in the marketplace.
 * Handles validation and order cancellation logic.
 * Framework-agnostic, following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;

    /**
     * Executes the order cancellation use case.
     *
     * @param command the cancellation command containing order ID
     * @return the result indicating success or failure with details
     */
    public CancelOrderResult execute(CancelOrderCommand command) {
        // 1. Validate order ID
        if (command.getOrderId() == null) {
            return CancelOrderResult.failure("Order ID is required", "INVALID_ORDER_ID");
        }

        // 2. Find order
        Optional<Order> orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty()) {
            return CancelOrderResult.failure("Order not found", "ORDER_NOT_FOUND");
        }

        Order order = orderOpt.get();

        // 3. Check if order can be cancelled using domain logic
        if (!order.canBeCancelled()) {
            return CancelOrderResult.failure(
                "Order cannot be cancelled in current status: " + order.getStatus(),
                "CANNOT_CANCEL_ORDER"
            );
        }

        // 4. Cancel order using domain logic
        try {
            order.markAsCancelled();
        } catch (IllegalStateException e) {
            return CancelOrderResult.failure(e.getMessage(), "CANCELLATION_FAILED");
        }

        // 5. Save cancelled order
        Order cancelledOrder = orderRepository.save(order);

        // 6. Return success result
        return CancelOrderResult.success(cancelledOrder.getId());
    }
}
