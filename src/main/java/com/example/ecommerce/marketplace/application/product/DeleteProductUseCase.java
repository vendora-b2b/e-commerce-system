package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Use case for deleting a product.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Executes the product deletion use case.
     *
     * @param productId the ID of the product to delete
     * @return the result indicating success or failure with details
     */
    public DeleteProductResult execute(Long productId) {
        // 1. Validate product ID
        if (productId == null) {
            return DeleteProductResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Find product
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return DeleteProductResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        // 3. Check if product has pending orders
        // Note: This checks for orders with status PENDING or PROCESSING
        boolean hasPendingOrders = orderRepository.existsByProductIdAndStatusIn(
            productId,
            java.util.List.of(OrderStatus.PENDING, OrderStatus.PROCESSING)
        );

        if (hasPendingOrders) {
            return DeleteProductResult.failure(
                "Cannot delete product with pending orders",
                "PRODUCT_HAS_PENDING_ORDERS"
            );
        }

        // 4. Delete product
        productRepository.deleteById(productId);

        // 5. Return success result
        return DeleteProductResult.success();
    }
}
