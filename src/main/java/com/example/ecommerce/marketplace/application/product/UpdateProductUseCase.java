package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Use case for updating an existing product's information.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    /**
     * Executes the product update use case.
     *
     * @param command the update command containing product ID and new product data
     * @return the result indicating success or failure with details
     */
    public UpdateProductResult execute(UpdateProductCommand command) {
        // 1. Validate product ID
        if (command.getProductId() == null) {
            return UpdateProductResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Find product
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return UpdateProductResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        Product product = productOpt.get();

        // 3. Update product information using domain logic
        try {
            // Update basic info (name, description, category)
            product.updateProductInfo(
                command.getName(),
                command.getDescription(),
                command.getCategory(),
                null // Unit is not in the command, keeps existing value
            );

            // Update base price if provided
            if (command.getBasePrice() != null) {
                product.updateBasePrice(command.getBasePrice());
            }

            // Update minimum order quantity if provided
            if (command.getMinimumOrderQuantity() != null) {
                product.updateMinimumOrderQuantity(command.getMinimumOrderQuantity());
            }

        } catch (IllegalArgumentException e) {
            // Domain validation failed
            return UpdateProductResult.failure(e.getMessage(), "VALIDATION_ERROR");
        }

        // 4. Save updated product
        Product updatedProduct = productRepository.save(product);

        // 5. Return success result
        return UpdateProductResult.success(updatedProduct.getId());
    }
}
