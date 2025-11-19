package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for creating a product price tier.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class CreateProductPriceTierUseCase {

    private final ProductRepository productRepository;

    /**
     * Executes the create product price tier use case.
     *
     * @param command the command containing price tier data
     * @return the result containing the created price tier or error
     */
    @Transactional
    public CreateProductPriceTierResult execute(CreateProductPriceTierCommand command) {
        // 1. Validate product ID
        if (command.getProductId() == null) {
            return CreateProductPriceTierResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }

        // 2. Query Product by ID
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return CreateProductPriceTierResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        Product product = productOpt.get();

        // 3. Validate minQuantity is positive (already validated by @Positive in request DTO, but double-check)
        if (command.getMinQuantity() == null || command.getMinQuantity() <= 0) {
            return CreateProductPriceTierResult.failure("Minimum quantity must be positive", "INVALID_MIN_QUANTITY");
        }

        // 4. If maxQuantity provided, validate maxQuantity > minQuantity
        if (command.getMaxQuantity() != null && command.getMaxQuantity() <= command.getMinQuantity()) {
            return CreateProductPriceTierResult.failure(
                "Maximum quantity must be greater than minimum quantity",
                "INVALID_MAX_QUANTITY"
            );
        }

        // 5. If discountPercent provided, validate 0 <= discountPercent <= 100
        if (command.getDiscountPercent() != null) {
            if (command.getDiscountPercent() < 0 || command.getDiscountPercent() > 100) {
                return CreateProductPriceTierResult.failure(
                    "Discount percent must be between 0 and 100",
                    "INVALID_DISCOUNT_PERCENT"
                );
            }
        }

        // 6. Check for overlapping price tiers
        boolean hasOverlap = product.getPriceTiers().stream()
            .anyMatch(existingTier -> tierOverlaps(
                command.getMinQuantity(), 
                command.getMaxQuantity(), 
                existingTier.getMinQuantity(), 
                existingTier.getMaxQuantity()
            ));

        if (hasOverlap) {
            return CreateProductPriceTierResult.failure(
                "Price tier overlaps with existing tier",
                "PRICE_TIER_OVERLAP"
            );
        }

        // 7. Create new PriceTier
        PriceTier newTier = new PriceTier(
            null, // ID will be generated
            command.getMinQuantity(),
            command.getMaxQuantity(),
            command.getDiscountPercent(),
            LocalDateTime.now()
        );

        // 8. Add price tier to product
        product.addPriceTier(newTier);

        // 9. Save product (this persists the price tier through cascade)
        Product savedProduct = productRepository.save(product);

        // 10. Get the saved tier (last one added)
        PriceTier savedTier = savedProduct.getPriceTiers().get(savedProduct.getPriceTiers().size() - 1);

        // 11. Return success
        return CreateProductPriceTierResult.success(savedTier);
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
