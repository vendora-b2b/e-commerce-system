package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.order.*;
import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;
import com.example.ecommerce.marketplace.web.model.order.OrderResponse;
import com.example.ecommerce.marketplace.web.model.order.PlaceOrderRequest;
import com.example.ecommerce.marketplace.web.model.order.UpdateOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Tag(name = "Order", description = "Order API")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final OrderRepository orderRepository;

    /**
     * List orders with filtering and pagination.
     * GET /api/v1/orders
     * @param startDate Filter orders from this date (ISO format: 2024-01-01T00:00:00)
     * @param endDate Filter orders until this date (ISO format: 2024-12-31T23:59:59)
     */
    @GetMapping
    public ResponseEntity<?> listOrders(
        @RequestParam(required = false) Long retailerId,
        @RequestParam(required = false) Long supplierId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "40") int size,
        @RequestParam(defaultValue = "orderDate,desc") String sort
    ) {
        // Parse date parameters
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
        }
        
        // Query with filters
        java.util.List<Order> allOrders;
        
        if (retailerId != null && status != null) {
            allOrders = orderRepository.findByRetailerIdAndStatus(
                retailerId, com.example.ecommerce.marketplace.domain.order.OrderStatus.valueOf(status));
        } else if (supplierId != null && status != null) {
            allOrders = orderRepository.findBySupplierIdAndStatus(
                supplierId, com.example.ecommerce.marketplace.domain.order.OrderStatus.valueOf(status));
        } else if (retailerId != null) {
            allOrders = orderRepository.findByRetailerId(retailerId);
        } else if (supplierId != null) {
            allOrders = orderRepository.findBySupplierId(supplierId);
        } else if (status != null) {
            allOrders = orderRepository.findByStatus(
                com.example.ecommerce.marketplace.domain.order.OrderStatus.valueOf(status));
        } else {
            allOrders = orderRepository.findAll();
        }
        
        // Apply date filtering
        final LocalDateTime finalStart = start;
        final LocalDateTime finalEnd = end;
        allOrders = allOrders.stream()
            .filter(order -> {
                LocalDateTime orderDate = order.getOrderDate();
                if (orderDate == null) return false;
                
                // If startDate is null => <= endDate
                // If endDate is null => >= startDate
                // If both null, return all
                if (finalStart != null && finalEnd != null) {
                    return !orderDate.isBefore(finalStart) && !orderDate.isAfter(finalEnd);
                } else if (finalStart != null) {
                    return !orderDate.isBefore(finalStart);
                } else if (finalEnd != null) {
                    return !orderDate.isAfter(finalEnd);
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";
            
            java.util.Comparator<Order> comparator = null;
            
            switch (sortField) {
                case "orderDate":
                    comparator = java.util.Comparator.comparing(Order::getOrderDate, 
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                    break;
                case "totalAmount":
                    comparator = java.util.Comparator.comparing(Order::getTotalAmount,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                    break;
                case "status":
                    comparator = java.util.Comparator.comparing(order -> order.getStatus().name());
                    break;
                default:
                    comparator = java.util.Comparator.comparing(Order::getOrderDate,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            }
            
            if ("desc".equalsIgnoreCase(sortDirection)) {
                comparator = comparator.reversed();
            }
            
            allOrders = allOrders.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        }
        
        // Manual pagination with 40 items per page
        int totalElements = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        java.util.List<Order> pagedOrders = startIndex < totalElements 
            ? allOrders.subList(startIndex, endIndex) 
            : java.util.Collections.emptyList();
        
        // Convert to response
        java.util.List<OrderResponse> content = pagedOrders.stream()
            .map(OrderResponse::fromDomain)
            .collect(Collectors.toList());
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", content);
        
        java.util.Map<String, Object> pageInfo = new java.util.HashMap<>();
        pageInfo.put("size", size);
        pageInfo.put("number", page);
        pageInfo.put("totalElements", totalElements);
        pageInfo.put("totalPages", totalPages);
        response.put("page", pageInfo);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Place a new order.
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(
        @Valid @RequestBody PlaceOrderRequest request
    ) {
        // Convert request to command
        PlaceOrderCommand command = new PlaceOrderCommand(
            request.getOrderNumber(),
            request.getRetailerId(),
            request.getSupplierId(),
            request.getOrderItems().stream()
                .map(item -> new PlaceOrderCommand.OrderItemCommand(
                    item.getVariantId(),
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

        // Handle failure - return error message in response body
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Get order by ID.
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            OrderResponse response = OrderResponse.fromDomain(order.get());
            return ResponseEntity.ok(response);
        }

        ErrorResponse errorResponse = ErrorResponse.of("ORDER_NOT_FOUND", "Order not found with ID: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Update order (status, deliveryDate, item prices).
     * PATCH /api/v1/orders/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateOrder(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOrderRequest request
    ) {
        // Convert request to command
        UpdateOrderCommand command = new UpdateOrderCommand(
            id,
            request.getStatus(),
            request.getDeliveryDate(),
            request.getItems() != null ? request.getItems().stream()
                .map(item -> new UpdateOrderCommand.OrderItemPriceUpdate(
                    item.getId(),
                    item.getFinalTotalPrice()
                ))
                .collect(java.util.stream.Collectors.toList())
                : null
        );

        // Execute use case
        UpdateOrderStatusResult result = updateOrderStatusUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            Order order = result.getOrder();
            OrderResponse response = OrderResponse.fromDomain(order);
            return ResponseEntity.ok(response);
        }

        // Handle failure - return error message in response body
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Cancel/delete an order.
     * DELETE /api/v1/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        CancelOrderCommand command = new CancelOrderCommand(id);
        CancelOrderResult result = cancelOrderUseCase.execute(command);

        if (!result.isSuccess()) {
            HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
            ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }

        // Return 204 No Content on successful deletion
        return ResponseEntity.noContent().build();
    }

}
