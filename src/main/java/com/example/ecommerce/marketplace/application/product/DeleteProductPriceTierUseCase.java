package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for deleting a product price tier.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class DeleteProductPriceTierUseCase {

    private final ProductRepository productRepository;

    /**
     * Executes the delete product price tier use case.
     *
     * @param command the command containing productId and tierId
     * @return the result indicating success or failure
     */
    @Transactional
    public DeleteProductPriceTierResult execute(DeleteProductPriceTierCommand command) {
        // 1. Validate product ID
        if (command.getProductId() == null) {
            return DeleteProductPriceTierResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Validate tier ID
        if (command.getTierId() == null) {
            return DeleteProductPriceTierResult.failure("Price tier ID is required", "INVALID_PRICE_TIER_ID");
        }

        // 3. Query Product by productId
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return DeleteProductPriceTierResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        Product product = productOpt.get();

        // 4. Query PriceTier by tierId
        PriceTier tierToDelete = product.getPriceTiers().stream()
            .filter(tier -> command.getTierId().equals(tier.getId()))
            .findFirst()
            .orElse(null);

        if (tierToDelete == null) {
            return DeleteProductPriceTierResult.failure("Price tier not found", "PRICE_TIER_NOT_FOUND");
        }

        // 5. Verify price tier belongs to product (already verified in step 4)
        // Additional explicit check for clarity
        boolean belongsToProduct = product.getPriceTiers().stream()
            .anyMatch(tier -> command.getTierId().equals(tier.getId()));

        if (!belongsToProduct) {
            return DeleteProductPriceTierResult.failure(
                "Price tier does not belong to the specified product",
                "PRICE_TIER_PRODUCT_MISMATCH"
            );
        }

        // 6. Delete PriceTier record (remove from product's collection)
        product.getPriceTiers().remove(tierToDelete);

        // 7. Save product (this persists the deletion through cascade)
        productRepository.save(product);

        // 8. Return success
        return DeleteProductPriceTierResult.success();
    }
}
