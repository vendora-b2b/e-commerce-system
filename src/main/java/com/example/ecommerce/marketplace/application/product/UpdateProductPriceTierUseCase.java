package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for updating a product price tier.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class UpdateProductPriceTierUseCase {

    private final ProductRepository productRepository;

    /**
     * Executes the update product price tier use case.
     *
     * @param command the command containing price tier data
     * @return the result containing the updated price tier or error
     */
    @Transactional
    public UpdateProductPriceTierResult execute(UpdateProductPriceTierCommand command) {
        // 1. Validate product ID
        if (command.getProductId() == null) {
            return UpdateProductPriceTierResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Validate tier ID
        if (command.getTierId() == null) {
            return UpdateProductPriceTierResult.failure("Price tier ID is required", "INVALID_PRICE_TIER_ID");
        }

        // 3. Query Product by productId
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return UpdateProductPriceTierResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        Product product = productOpt.get();

        // 4. Query PriceTier by tierId
        PriceTier tierToUpdate = product.getPriceTiers().stream()
            .filter(tier -> command.getTierId().equals(tier.getId()))
            .findFirst()
            .orElse(null);

        if (tierToUpdate == null) {
            return UpdateProductPriceTierResult.failure("Price tier not found", "PRICE_TIER_NOT_FOUND");
        }

        // 5. Verify price tier belongs to product (already verified in step 4)
        // Additional explicit check for clarity
        boolean belongsToProduct = product.getPriceTiers().stream()
            .anyMatch(tier -> command.getTierId().equals(tier.getId()));

        if (!belongsToProduct) {
            return UpdateProductPriceTierResult.failure(
                "Price tier does not belong to the specified product",
                "PRICE_TIER_PRODUCT_MISMATCH"
            );
        }

        // 6. Prepare new values (use current values if not provided)
        Integer newMinQuantity = command.getMinQuantity() != null ? command.getMinQuantity() : tierToUpdate.getMinQuantity();
        Integer newMaxQuantity = command.getMaxQuantity() != null ? command.getMaxQuantity() : tierToUpdate.getMaxQuantity();
        Double newDiscountPercent = command.getDiscountPercent() != null ? command.getDiscountPercent() : tierToUpdate.getDiscountPercent();

        // 7. Validate minQuantity is positive
        if (newMinQuantity == null || newMinQuantity <= 0) {
            return UpdateProductPriceTierResult.failure("Minimum quantity must be positive", "INVALID_MIN_QUANTITY");
        }

        // 8. If maxQuantity provided, validate maxQuantity > minQuantity
        if (newMaxQuantity != null && newMaxQuantity <= newMinQuantity) {
            return UpdateProductPriceTierResult.failure(
                "Maximum quantity must be greater than minimum quantity",
                "INVALID_MAX_QUANTITY"
            );
        }

        // 9. If discountPercent provided, validate 0 <= discountPercent <= 100
        if (newDiscountPercent != null) {
            if (newDiscountPercent < 0 || newDiscountPercent > 100) {
                return UpdateProductPriceTierResult.failure(
                    "Discount percent must be between 0 and 100",
                    "INVALID_DISCOUNT_PERCENT"
                );
            }
        }

        // 10. Check for overlapping price tiers (excluding current tier)
        boolean hasOverlap = product.getPriceTiers().stream()
            .filter(tier -> !command.getTierId().equals(tier.getId())) // Exclude current tier
            .anyMatch(existingTier -> tierOverlaps(
                newMinQuantity, 
                newMaxQuantity, 
                existingTier.getMinQuantity(), 
                existingTier.getMaxQuantity()
            ));

        if (hasOverlap) {
            return UpdateProductPriceTierResult.failure(
                "Price tier overlaps with existing tier",
                "PRICE_TIER_OVERLAP"
            );
        }

        // 11. Update PriceTier record with all provided fields
        tierToUpdate.setMinQuantity(newMinQuantity);
        tierToUpdate.setMaxQuantity(newMaxQuantity);
        tierToUpdate.setDiscountPercent(newDiscountPercent);
        tierToUpdate.setUpdatedAt(LocalDateTime.now());

        // 11. Save product (this persists the updated price tier through cascade)
        Product savedProduct = productRepository.save(product);

        // 12. Get the updated tier from saved product
        PriceTier updatedTier = savedProduct.getPriceTiers().stream()
            .filter(tier -> command.getTierId().equals(tier.getId()))
            .findFirst()
            .orElse(tierToUpdate); // Fallback to the tier we updated

        // 13. Return success
        return UpdateProductPriceTierResult.success(updatedTier);
    }

    /**
     * Checks if two price tier ranges overlap.
     * 
     * @param min1 minimum quantity of first tier
     * @param max1 maximum quantity of first tier (null means unlimited)
     * @param min2 minimum quantity of second tier
     * @param max2 maximum quantity of second tier (null means unlimited)
     * @return true if ranges overlap, false otherwise
     */
    private boolean tierOverlaps(Integer min1, Integer max1, Integer min2, Integer max2) {
        // If either tier is null for min, can't overlap properly
        if (min1 == null || min2 == null) {
            return false;
        }

        // Check if tier1 starts within tier2's range
        boolean tier1StartsInTier2 = min1 >= min2 && (max2 == null || min1 <= max2);

        // Check if tier2 starts within tier1's range
        boolean tier2StartsInTier1 = min2 >= min1 && (max1 == null || min2 <= max1);

        return tier1StartsInTier2 || tier2StartsInTier1;
    }
}
