package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case for creating a product variant.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class CreateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Executes the create product variant use case.
     *
     * @param command the command containing variant data
     * @return the result indicating success with created variant or failure
     */
    public CreateProductVariantResult execute(CreateProductVariantCommand command) {
        // 1. Validate command
        if (command.getProductId() == null) {
            return CreateProductVariantResult.failure("Product ID is required", "INVALID_PRODUCT_ID");
        }
        if (command.getSku() == null || command.getSku().trim().isEmpty()) {
            return CreateProductVariantResult.failure("SKU is required", "INVALID_SKU");
        }
        if (command.getColor() == null || command.getColor().trim().isEmpty()) {
            return CreateProductVariantResult.failure("Color is required", "INVALID_COLOR");
        }
        if (command.getSize() == null || command.getSize().trim().isEmpty()) {
            return CreateProductVariantResult.failure("Size is required", "INVALID_SIZE");
        }

        // 2. Check if product exists
        Optional<Product> productOpt = productRepository.findById(command.getProductId());
        if (productOpt.isEmpty()) {
            return CreateProductVariantResult.failure("Product not found", "PRODUCT_NOT_FOUND");
        }
        
        Product product = productOpt.get();

        // 3. Check for duplicate SKU
        if (productVariantRepository.existsBySku(command.getSku())) {
            return CreateProductVariantResult.failure("SKU already exists", "DUPLICATE_SKU");
        }

        // 4. Check for duplicate variant (same productId + color + size)
        if (productVariantRepository.existsByProductIdAndColorAndSize(
                command.getProductId(), command.getColor(), command.getSize())) {
            return CreateProductVariantResult.failure(
                "Variant with same color and size already exists for this product",
                "DUPLICATE_VARIANT"
            );
        }

        // 5. Create variant
        ProductVariant variant = new ProductVariant();
        variant.setProductId(command.getProductId());
        variant.setSku(command.getSku());
        variant.setColor(command.getColor());
        variant.setSize(command.getSize());
        variant.setPriceAdjustment(command.getPriceAdjustment() != null ? command.getPriceAdjustment() : 0.0);
        variant.setCreatedAt(LocalDateTime.now());

        ProductVariant savedVariant = productVariantRepository.save(variant);

        // 6. Create default inventory record with zero quantities
        Inventory inventory = new Inventory();
        inventory.setSupplierId(product.getSupplierId());
        inventory.setProductId(savedVariant.getProductId());
        inventory.setVariantId(savedVariant.getId());
        inventory.setAvailableQuantity(0);
        inventory.setReservedQuantity(0);
        inventory.setLastRestocked(LocalDateTime.now());

        inventoryRepository.save(inventory);

        // 7. Return success result
        return CreateProductVariantResult.success(savedVariant);
    }
}
