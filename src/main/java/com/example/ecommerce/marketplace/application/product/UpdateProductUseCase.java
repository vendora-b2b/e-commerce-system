package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.application.ai.IngestProductCommand;
import com.example.ecommerce.marketplace.application.ai.IngestProductUseCase;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.Category;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.infrastructure.product.CategoryEntity;
import com.example.ecommerce.marketplace.infrastructure.product.JpaCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for updating an existing product's information.
 * Framework-agnostic, following Clean Architecture principles.
 * 
 * Integration: After successful product update, the product is
 * re-ingested into the AI service ONLY if semantic fields (name,
 * description, categories) have changed. Price, stock, and other
 * frequently-changing fields do NOT trigger AI re-indexing.
 */
@RequiredArgsConstructor
@Slf4j
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final JpaCategoryRepository categoryRepository;
    private final IngestProductUseCase ingestProductUseCase;

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

        // 3. Capture current semantic field values BEFORE update (for AI re-index
        // check)
        String oldName = product.getName();
        String oldDescription = product.getDescription();
        String oldCategoryNames = extractCategoryNames(product.getCategories());
        String oldSKU = product.getSku();

        // 4. Fetch Category objects from database using IDs if provided
        List<Category> categories = null;
        if (command.getCategoryIds() != null && !command.getCategoryIds().isEmpty()) {
            List<CategoryEntity> categoryEntities = categoryRepository.findAllById(command.getCategoryIds());

            // Validate all categories exist
            if (categoryEntities.size() != command.getCategoryIds().size()) {
                return UpdateProductResult.failure("One or more category IDs not found", "INVALID_CATEGORY_IDS");
            }

            categories = categoryEntities.stream()
                    .map(CategoryEntity::toDomain)
                    .toList();
        }

        // 5. Convert price tiers if provided
        List<PriceTier> priceTiers = null;
        if (command.getPriceTiers() != null && !command.getPriceTiers().isEmpty()) {
            priceTiers = command.getPriceTiers().stream()
                    .map(dto -> new PriceTier(
                            null,
                            dto.getMinQuantity(),
                            dto.getMaxQuantity(),
                            dto.getDiscountPercent()))
                    .toList();
        }

        // 6. Update product information using domain logic
        try {
            // Update SKU if provided (triggers AI re-index if changed)
            if (command.getSku() != null) {
                product.setSku(command.getSku());
            }

            // Update basic info (name, description, categories, unit)
            product.updateProductInfo(
                    command.getName(),
                    command.getDescription(),
                    categories,
                    command.getUnit());

            // Update base price if provided (does NOT trigger AI re-index)
            if (command.getBasePrice() != null) {
                product.updateBasePrice(command.getBasePrice());
            }

            // Update minimum order quantity if provided (does NOT trigger AI re-index)
            if (command.getMinimumOrderQuantity() != null) {
                product.updateMinimumOrderQuantity(command.getMinimumOrderQuantity());
            }

            // Update images if provided
            if (command.getImages() != null) {
                product.setImages(command.getImages());
            }

            // Update price tiers if provided (does NOT trigger AI re-index)
            if (priceTiers != null) {
                product.clearPriceTiers();
                priceTiers.forEach(product::addPriceTier);
            }

        } catch (IllegalArgumentException e) {
            // Domain validation failed
            return UpdateProductResult.failure(e.getMessage(), "VALIDATION_ERROR");
        }

        // 7. Save updated product
        Product updatedProduct = productRepository.save(product);

        // 8. Check if semantic fields changed - only then re-ingest to AI
        String newSKU = updatedProduct.getSku();
        String newName = updatedProduct.getName();
        String newDescription = updatedProduct.getDescription();
        String newCategoryNames = extractCategoryNames(updatedProduct.getCategories());

        boolean semanticFieldsChanged = !Objects.equals(oldName, newName) ||
                !Objects.equals(oldDescription, newDescription) ||
                !Objects.equals(oldCategoryNames, newCategoryNames) ||
                !Objects.equals(oldSKU, newSKU);

        if (semanticFieldsChanged) {
            log.debug("Semantic fields changed for product {} - triggering AI re-index", updatedProduct.getSku());
            reingestProductToAiServiceAsync(updatedProduct);
        } else {
            log.debug("Only non-semantic fields changed for product {} - skipping AI re-index",
                    updatedProduct.getSku());
        }

        // 9. Return success result
        return UpdateProductResult.success(updatedProduct.getId());
    }

    /**
     * Extracts category names as a comma-separated string for comparison.
     */
    private String extractCategoryNames(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.stream()
                .map(Category::getName)
                .sorted() // Sort for consistent comparison
                .collect(Collectors.joining(", "));
    }

    /**
     * Asynchronously re-ingests the product into the AI service.
     * This updates the vector embeddings with the latest product information.
     * Failures are logged but don't affect the main operation.
     * 
     * Note: Only stores semantic data (name, description, category, supplier).
     * Price, stock, and other frequently-changing fields are NOT stored.
     */
    private void reingestProductToAiServiceAsync(Product updatedProduct) {
        try {
            // Extract category name for AI indexing
            String categoryName = extractCategoryNames(updatedProduct.getCategories());

            IngestProductCommand ingestCommand = IngestProductCommand.builder()
                    .productId(updatedProduct.getId())
                    .sku(updatedProduct.getSku())
                    .name(updatedProduct.getName())
                    .description(updatedProduct.getDescription())
                    .category(categoryName)
                    .supplierId(updatedProduct.getSupplierId())
                    .build();

            // Execute asynchronously - failures won't affect product update
            ingestProductUseCase.executeAsync(ingestCommand);

            log.debug("Triggered AI re-ingestion for updated product: {} ({})",
                    updatedProduct.getSku(), updatedProduct.getId());

        } catch (Exception e) {
            // Log but don't fail - AI re-ingestion is non-critical
            log.warn("Failed to trigger AI re-ingestion for product {}: {}",
                    updatedProduct.getSku(), e.getMessage());
        }
    }
}
