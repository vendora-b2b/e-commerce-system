package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Use case for deleting a product variant.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class DeleteProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Executes the delete product variant use case.
     *
     * @param command the command containing product and variant IDs
     * @return the result indicating success or failure
     */
    @Transactional
    public DeleteProductVariantResult execute(DeleteProductVariantCommand command) {
        // 1. Validate product ID
        if (command.getProductId() == null) {
            return DeleteProductVariantResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Validate variant ID
        if (command.getVariantId() == null) {
            return DeleteProductVariantResult.failure("Variant ID is required", "INVALID_VARIANT_ID");
        }

        // 3. Query Product by productId
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return DeleteProductVariantResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        // 4. Query ProductVariant by variantId
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(command.getVariantId());
        if (variantOpt.isEmpty()) {
            return DeleteProductVariantResult.failure("Variant not found", "VARIANT_NOT_FOUND");
        }

        ProductVariant variant = variantOpt.get();

        // 5. Verify variant belongs to product
        if (!variant.getProductId().equals(command.getProductId())) {
            return DeleteProductVariantResult.failure(
                "Variant does not belong to the specified product",
                "VARIANT_PRODUCT_MISMATCH"
            );
        }

        // 6. Check if product has only 1 variant remaining
        long variantCount = productVariantRepository.countByProductId(command.getProductId());
        if (variantCount <= 1) {
            return DeleteProductVariantResult.failure(
                "Cannot remove last variant",
                "LAST_VARIANT_CANNOT_BE_DELETED"
            );
        }

        // 7. Check if variant has pending orders
        List<OrderStatus> pendingStatuses = List.of(OrderStatus.PENDING, OrderStatus.PROCESSING);
        boolean hasPendingOrders = orderRepository.existsByVariantIdAndStatusIn(
            command.getVariantId(),
            pendingStatuses
        );

        if (hasPendingOrders) {
            return DeleteProductVariantResult.failure(
                "Variant has pending orders",
                "VARIANT_HAS_PENDING_ORDERS"
            );
        }

        // 8. BEGIN TRANSACTION (handled by @Transactional at service layer)
        // Delete associated Inventory record
        inventoryRepository.deleteByVariantId(command.getVariantId());

        // Delete ProductVariant record
        productVariantRepository.deleteById(command.getVariantId());
        // COMMIT TRANSACTION

        // 9. Return success
        return DeleteProductVariantResult.success();
    }
}
