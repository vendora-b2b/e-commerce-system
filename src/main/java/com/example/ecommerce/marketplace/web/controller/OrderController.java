package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.order.*;
import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.order.CancelOrderRequest;
import com.example.ecommerce.marketplace.web.model.order.OrderResponse;
import com.example.ecommerce.marketplace.web.model.order.PlaceOrderRequest;
import com.example.ecommerce.marketplace.web.model.order.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Order operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final OrderRepository orderRepository;

    /**
     * Place a new order.
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody PlaceOrderRequest request
    ) {
        // Convert request to command
        PlaceOrderCommand command = new PlaceOrderCommand(
            request.getOrderNumber(),
            request.getRetailerId(),
            request.getSupplierId(),
            request.getOrderItems().stream()
                .map(item -> new PlaceOrderCommand.OrderItemCommand(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getProductName()
                ))
                .collect(Collectors.toList()),
            request.getShippingAddress(),
            request.getOrderDate()
        );

        // Execute use case
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Get the order from the result
            Order order = result.getOrder();
            OrderResponse response = OrderResponse.fromDomain(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Get order by ID.
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            OrderResponse response = OrderResponse.fromDomain(order.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Update order status.
     * PUT /api/v1/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        // Convert request to command
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
            id,
            request.getNewStatus()
        );

        // Execute use case
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated order to return full details
            Optional<Order> order = orderRepository.findById(result.getOrderId());
            if (order.isPresent()) {
                OrderResponse response = OrderResponse.fromDomain(order.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Cancel an order.
     * DELETE /api/v1/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(
        @PathVariable Long id,
        @RequestBody(required = false) CancelOrderRequest request
    ) {
        // Convert request to command
        CancelOrderCommand command = new CancelOrderCommand(id);

        // Execute use case
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the cancelled order to return full details
            Optional<Order> order = orderRepository.findById(result.getOrderId());
            if (order.isPresent()) {
                OrderResponse response = OrderResponse.fromDomain(order.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
