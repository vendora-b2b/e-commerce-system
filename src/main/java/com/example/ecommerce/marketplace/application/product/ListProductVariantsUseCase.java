package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Use case for listing product variants with optional filters.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class ListProductVariantsUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Executes the list product variants use case.
     *
     * @param productId the ID of the product
     * @param color optional color filter
     * @param size optional size filter
     * @return the result indicating success with variants list or failure
     */
    public ListProductVariantsResult execute(Long productId, String color, String size) {
        // 1. Validate product ID
        if (productId == null) {
            return ListProductVariantsResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Check if product exists
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ListProductVariantsResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        // 3. Find variants with filters
        List<ProductVariant> variants = productVariantRepository.findByProductIdWithFilters(
            productId, color, size
        );

        // 4. Return success result
        return ListProductVariantsResult.success(variants);
    }
}
