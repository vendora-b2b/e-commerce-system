package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Use case for listing price tiers for a product.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class ListProductPriceTiersUseCase {

    private final ProductRepository productRepository;

    /**
     * Executes the list product price tiers use case.
     *
     * @param productId the ID of the product
     * @return the result containing the price tiers or error
     */
    public ListProductPriceTiersResult execute(Long productId) {
        // 1. Validate product ID
        if (productId == null) {
            return ListProductPriceTiersResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Find product
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ListProductPriceTiersResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        Product product = productOpt.get();

        // 3. Get price tiers from product
        List<PriceTier> priceTiers = product.getPriceTiers();

        // 4. Return success with price tiers (may be empty list)
        return ListProductPriceTiersResult.success(priceTiers);
    }
}
