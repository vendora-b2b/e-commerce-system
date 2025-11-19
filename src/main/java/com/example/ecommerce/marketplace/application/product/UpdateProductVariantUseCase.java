package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for updating a product variant (partial update).
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class UpdateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Executes the update product variant use case.
     *
     * @param command the command containing update data
     * @return the result indicating success with updated variant or failure
     */
    public UpdateProductVariantResult execute(UpdateProductVariantCommand command) {
        // 1. Validate command
        if (command.getProductId() == null) {
            return UpdateProductVariantResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getVariantId() == null) {
            return UpdateProductVariantResult.failure("Variant ID is required", "INVALID_VARIANT_ID");
        }

        // 2. Check if product exists
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return UpdateProductVariantResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }

        // 3. Check if variant exists
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(command.getVariantId());
        if (variantOpt.isEmpty()) {
            return UpdateProductVariantResult.failure("Variant not found", "VARIANT_NOT_FOUND");
        }

        ProductVariant variant = variantOpt.get();

        // 4. Verify variant belongs to product
        if (!variant.getProductId().equals(command.getProductId())) {
            return UpdateProductVariantResult.failure(
                "Variant does not belong to this product",
                "VARIANT_PRODUCT_MISMATCH"
            );
        }

        // 5. Update only provided fields
        boolean updated = false;

        if (command.getSku() != null) {
            // Check for duplicate SKU
            Optional<ProductVariant> existingSku = productVariantRepository.findBySku(command.getSku());
            if (existingSku.isPresent() && !existingSku.get().getId().equals(variant.getId())) {
                return UpdateProductVariantResult.failure("SKU already exists", "DUPLICATE_SKU");
            }
            variant.setSku(command.getSku());
            updated = true;
        }

        if (command.getColor() != null) {
            variant.setColor(command.getColor());
            updated = true;
        }

        if (command.getSize() != null) {
            variant.setSize(command.getSize());
            updated = true;
        }

        if (command.getPriceAdjustment() != null) {
            variant.setPriceAdjustment(command.getPriceAdjustment());
            updated = true;
        }

        // 6. Check for duplicate variant if color or size was updated
        if (command.getColor() != null || command.getSize() != null) {
            boolean isDuplicate = productVariantRepository.existsByProductIdAndColorAndSizeAndIdNot(
                variant.getProductId(),
                variant.getColor(),
                variant.getSize(),
                variant.getId()
            );
            
            if (isDuplicate) {
                return UpdateProductVariantResult.failure(
                    "Variant with same color and size already exists for this product",
                    "DUPLICATE_VARIANT"
                );
            }
        }

        // 7. Set updatedAt timestamp
        if (updated) {
            variant.setUpdatedAt(LocalDateTime.now());
        }

        // 8. Save variant
        ProductVariant savedVariant = productVariantRepository.save(variant);

        // 9. Return success result
        return UpdateProductVariantResult.success(savedVariant);
    }
}
