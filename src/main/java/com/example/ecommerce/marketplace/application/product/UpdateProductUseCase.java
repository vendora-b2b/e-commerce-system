package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.Category;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;

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

        // 3. Create Category objects from categories if provided
        List<Category> categories = null;
        if (command.getCategories() != null && !command.getCategories().isEmpty()) {
            categories = command.getCategories().stream()
                .map(dto -> new Category(null, dto.getName(), dto.getSlug(), null, null))
                .toList();
        }

        // 4. Convert price tiers if provided
        List<PriceTier> priceTiers = null;
        if (command.getPriceTiers() != null && !command.getPriceTiers().isEmpty()) {
            priceTiers = command.getPriceTiers().stream()
                .map(dto -> new PriceTier(
                    null,
                    dto.getMinQuantity(),
                    dto.getMaxQuantity(),
                    dto.getDiscountPercent()
                ))
                .toList();
        }

        // 5. Update product information using domain logic
        try {
            // Update basic info (name, description, categories, unit)
            product.updateProductInfo(
                command.getName(),
                command.getDescription(),
                categories,
                command.getUnit()
            );

            // Update base price if provided
            if (command.getBasePrice() != null) {
                product.updateBasePrice(command.getBasePrice());
            }

            // Update minimum order quantity if provided
            if (command.getMinimumOrderQuantity() != null) {
                product.updateMinimumOrderQuantity(command.getMinimumOrderQuantity());
            }

            // Update images if provided
            if (command.getImages() != null) {
                product.setImages(command.getImages());
            }

            // Update colors if provided
            if (command.getColors() != null) {
                product.setColors(command.getColors());
            }

            // Update sizes if provided
            if (command.getSizes() != null) {
                product.setSizes(command.getSizes());
            }

            // Update price tiers if provided
            if (priceTiers != null) {
                product.clearPriceTiers();
                priceTiers.forEach(product::addPriceTier);
            }

        } catch (IllegalArgumentException e) {
            // Domain validation failed
            return UpdateProductResult.failure(e.getMessage(), "VALIDATION_ERROR");
        }

        // 6. Save updated product
        Product updatedProduct = productRepository.save(product);

        // 7. Return success result
        return UpdateProductResult.success(updatedProduct.getId());
    }
}
