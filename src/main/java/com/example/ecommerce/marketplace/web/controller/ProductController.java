package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.common.PagedResponse;
import com.example.ecommerce.marketplace.web.model.product.CreateProductRequest;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.product.ProductVariantResponse;
import com.example.ecommerce.marketplace.web.model.product.UpdateProductRequest;
import com.example.ecommerce.marketplace.web.model.product.CreateProductVariantRequest;
import com.example.ecommerce.marketplace.web.model.product.ProductPriceTierResponse;
import com.example.ecommerce.marketplace.web.model.product.UpdateProductVariantRequest;
import com.example.ecommerce.marketplace.web.model.product.CreateProductPriceTierRequest;
import com.example.ecommerce.marketplace.web.model.product.UpdateProductPriceTierRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Product operations.
 * Handles HTTP requests and delegates business logic to use cases.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product API")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ListProductVariantsUseCase listProductVariantsUseCase;
    private final CreateProductVariantUseCase createProductVariantUseCase;
    private final UpdateProductVariantUseCase updateProductVariantUseCase;
    private final DeleteProductVariantUseCase deleteProductVariantUseCase;
    private final ListProductPriceTiersUseCase listProductPriceTiersUseCase;
    private final CreateProductPriceTierUseCase createProductPriceTierUseCase;
    private final UpdateProductPriceTierUseCase updateProductPriceTierUseCase;
    private final DeleteProductPriceTierUseCase deleteProductPriceTierUseCase;
    private final ProductRepository productRepository;

    /**
     * Create a new product with initial inventory.
     * POST /api/v1/products
     * 
     * @param request the product creation request
     * @return 201 CREATED with complete product details, 
     *         404 NOT FOUND if supplier doesn't exist,
     *         400 BAD REQUEST for validation errors,
     *         409 CONFLICT if SKU already exists
     */
    @Operation(summary = "Create a new product", description = "Create a new product with initial inventory. Products without variants cannot be ordered.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data - validation errors",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Supplier not found",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "SKU already exists",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request
    ) {
        // Convert price tiers from request to command DTOs
        List<CreateProductCommand.PriceTierDto> priceTierDtos = null;
        if (request.getPriceTiers() != null) {
            priceTierDtos = request.getPriceTiers().stream()
                .map(tier -> new CreateProductCommand.PriceTierDto(
                    tier.getMinQuantity(),
                    tier.getMaxQuantity(),
                    tier.getDiscountPercent()
                ))
                .collect(Collectors.toList());
        }

        // Convert categories from request to command DTOs
        List<CreateProductCommand.CategoryDto> categoryDtos = null;
        if (request.getCategories() != null) {
            categoryDtos = request.getCategories().stream()
                .map(cat -> new CreateProductCommand.CategoryDto(
                    cat.getName(),
                    cat.getSlug()
                ))
                .collect(Collectors.toList());
        }

        // Convert request to command
        CreateProductCommand command = new CreateProductCommand(
            request.getSku(),
            request.getName(),
            request.getDescription(),
            categoryDtos,
            request.getBasePrice(),
            request.getMinimumOrderQuantity(),
            request.getSupplierId(),
            request.getUnit(),
            request.getImages(),
            request.getColors(),
            request.getSizes(),
            priceTierDtos,
            null // No variants
        );

        // Execute use case
        CreateProductResult result = createProductUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Get the created product from repository
            Optional<Product> product = productRepository.findById(result.getProductId());
            if (product.isPresent()) {
                ProductResponse response = ProductResponse.fromDomain(product.get());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * List and filter products with pagination.
     * GET /api/v1/products
     * 
     * @param sku optional SKU filter (exact match)
     * @param supplierId optional supplier ID filter
     * @param category optional category slug filter
     * @param page page number (default: 0, zero-based)
     * @param size page size (default: 20, max: 100)
     * @param sort sort criteria: field,direction (e.g., name,asc or createdAt,desc)
     * @return 200 OK with paginated product list
     */
    @Operation(summary = "List products", description = "List and filter products with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> listProducts(
        @RequestParam(required = false) String sku,
        @RequestParam(required = false) Long supplierId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        // Validate and limit page size
        if (size > 100) {
            size = 100;
        }
        if (size < 1) {
            size = 20;
        }
        if (page < 0) {
            page = 0;
        }

        // Parse sort parameter
        Sort sortObj = parseSort(sort);

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Fetch products with filters
        Page<Product> productPage = productRepository.findWithFilters(sku, supplierId, category, pageable);

        // Convert to response DTOs
        Page<ProductResponse> responsePage = productPage.map(ProductResponse::fromDomain);

        // Return paginated response
        return ResponseEntity.ok(PagedResponse.of(responsePage));
    }

    /**
     * Parses sort parameter in format "field,direction".
     * Defaults to "createdAt,desc" if invalid.
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim();

        // Validate field name to prevent injection
        List<String> allowedFields = List.of("id", "sku", "name", "basePrice", "createdAt", "updatedAt");
        if (!allowedFields.contains(field)) {
            field = "createdAt";
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;

        return Sort.by(sortDirection, field);
    }

    /**
     * Retrieve product details by ID.
     * GET /api/v1/products/{id}
     * 
     * @param id the product ID
     * @return 200 OK with product details, 404 NOT FOUND if product doesn't exist
     */
    @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ProductResponse response = ProductResponse.fromDomain(product.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update product. Only provided fields are updated.
     * PATCH /api/v1/products/{id}
     * 
     * @param id the product ID
     * @param request the update request with optional fields
     * @return 200 OK with updated product, 404 NOT FOUND if product doesn't exist, 400 BAD REQUEST for validation errors
     */
    @Operation(summary = "Update product", description = "Partially update product. Only provided fields are updated.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data - validation errors",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        // Convert categories from request to command DTOs
        List<UpdateProductCommand.CategoryDto> categoryDtos = null;
        if (request.getCategories() != null) {
            categoryDtos = request.getCategories().stream()
                .map(cat -> new UpdateProductCommand.CategoryDto(
                    cat.getName(),
                    cat.getSlug()
                ))
                .collect(Collectors.toList());
        }

        // Convert price tiers from request to command DTOs
        List<UpdateProductCommand.PriceTierDto> priceTierDtos = null;
        if (request.getPriceTiers() != null) {
            priceTierDtos = request.getPriceTiers().stream()
                .map(tier -> new UpdateProductCommand.PriceTierDto(
                    tier.getMinQuantity(),
                    tier.getMaxQuantity(),
                    tier.getDiscountPercent()
                ))
                .collect(Collectors.toList());
        }

        // Convert request to command
        UpdateProductCommand command = new UpdateProductCommand(
            id,
            request.getName(),
            request.getDescription(),
            categoryDtos,
            request.getBasePrice(),
            request.getMinimumOrderQuantity(),
            request.getUnit(),
            request.getImages(),
            request.getColors(),
            request.getSizes(),
            priceTierDtos
        );

        // Execute use case
        UpdateProductResult result = updateProductUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Get the updated product from repository
            Optional<Product> product = productRepository.findById(result.getProductId());
            if (product.isPresent()) {
                ProductResponse response = ProductResponse.fromDomain(product.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Delete product.
     * DELETE /api/v1/products/{id}
     * 
     * @param id the product ID
     * @return 204 NO CONTENT if deleted successfully, 
     *         404 NOT FOUND if product doesn't exist,
     *         400 BAD REQUEST if product has pending orders
     */
    @Operation(summary = "Delete product", description = "Delete product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete product with pending orders",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Execute use case
        DeleteProductResult result = deleteProductUseCase.execute(id);

        // Handle result
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Delete a product variant.
     * DELETE /api/v1/products/{productId}/variants/{variantId}
     * 
     * @param productId the product ID
     * @param variantId the variant ID
     * @return 204 NO CONTENT on success,
     *         400 BAD REQUEST if validation fails or variant doesn't belong to product or is last variant or has pending orders,
     *         404 NOT FOUND if product or variant doesn't exist
     */
    @Operation(summary = "Delete product variant", description = "Remove a variant from a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Variant deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid request, variant doesn't belong to product, last variant, or has pending orders",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or variant not found",
            content = @Content)
    })
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<?> deleteProductVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        
        // Build command
        DeleteProductVariantCommand command = new DeleteProductVariantCommand(productId, variantId);

        // Execute use case
        DeleteProductVariantResult result = deleteProductVariantUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Update a product variant (partial update).
     * PATCH /api/v1/products/{productId}/variants/{variantId}
     * 
     * @param productId the product ID
     * @param variantId the variant ID
     * @param request the update request with optional fields
     * @return 200 OK with updated variant,
     *         400 BAD REQUEST if validation fails or variant doesn't belong to product,
     *         404 NOT FOUND if product or variant doesn't exist,
     *         409 CONFLICT if update would create duplicate
     */
    @Operation(summary = "Update product variant", description = "Partially update a product variant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Variant updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductVariantResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or variant doesn't belong to product",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or variant not found",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Duplicate SKU or color+size combination",
            content = @Content)
    })
    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<?> updateProductVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody @Valid UpdateProductVariantRequest request) {
        
        // Build command
        UpdateProductVariantCommand command = UpdateProductVariantCommand.builder()
            .productId(productId)
            .variantId(variantId)
            .sku(request.getSku())
            .color(request.getColor())
            .size(request.getSize())
            .priceAdjustment(request.getPriceAdjustment())
            .build();

        // Execute use case
        UpdateProductVariantResult result = updateProductVariantUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            ProductVariantResponse response = ProductVariantResponse.fromDomain(result.getVariant());
            return ResponseEntity.ok(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Add a new price tier to a product.
     * POST /api/v1/products/{id}/price-tiers
     * 
     * @param id the product ID
     * @param request the price tier creation request
     * @return 201 CREATED with new price tier,
     *         400 BAD REQUEST for validation errors,
     *         404 NOT FOUND if product doesn't exist,
     *         409 CONFLICT if price tier overlaps
     */
    @Operation(summary = "Create product price tier", description = "Add a new price tier to a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Price tier created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductPriceTierResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Price tier overlaps with existing tier",
            content = @Content)
    })
    @PostMapping("/{id}/price-tiers")
    public ResponseEntity<?> createProductPriceTier(
            @PathVariable Long id,
            @RequestBody @Valid CreateProductPriceTierRequest request) {
        
        // Build command
        CreateProductPriceTierCommand command = new CreateProductPriceTierCommand(
            id,
            request.getMinQuantity(),
            request.getMaxQuantity(),
            request.getDiscountPercent()
        );

        // Execute use case
        CreateProductPriceTierResult result = createProductPriceTierUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            ProductPriceTierResponse response = ProductPriceTierResponse.fromDomain(result.getPriceTier());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Update an existing price tier for a product.
     * PUT /api/v1/products/{productId}/price-tiers/{tierId}
     * 
     * @param productId the product ID
     * @param tierId the price tier ID
     * @param request the price tier update request
     * @return 200 OK with updated price tier,
     *         400 BAD REQUEST for validation errors,
     *         404 NOT FOUND if product or price tier doesn't exist,
     *         409 CONFLICT if price tier overlaps
     */
    @Operation(summary = "Update product price tier", description = "Update an existing price tier for a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Price tier updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductPriceTierResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or price tier not found",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Price tier overlaps with existing tier",
            content = @Content)
    })
    @PutMapping("/{productId}/price-tiers/{tierId}")
    public ResponseEntity<?> updateProductPriceTier(
            @PathVariable Long productId,
            @PathVariable Long tierId,
            @RequestBody @Valid UpdateProductPriceTierRequest request) {
        
        // Build command
        UpdateProductPriceTierCommand command = new UpdateProductPriceTierCommand(
            productId,
            tierId,
            request.getMinQuantity(),
            request.getMaxQuantity(),
            request.getDiscountPercent()
        );

        // Execute use case
        UpdateProductPriceTierResult result = updateProductPriceTierUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            ProductPriceTierResponse response = ProductPriceTierResponse.fromDomain(result.getPriceTier());
            return ResponseEntity.ok(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Delete a price tier from a product.
     * DELETE /api/v1/products/{productId}/price-tiers/{tierId}
     * 
     * @param productId the product ID
     * @param tierId the price tier ID
     * @return 204 NO CONTENT on success,
     *         400 BAD REQUEST if tier doesn't belong to product,
     *         404 NOT FOUND if product or price tier doesn't exist
     */
    @Operation(summary = "Delete product price tier", description = "Remove a price tier from a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Price tier deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Price tier does not belong to product",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or price tier not found",
            content = @Content)
    })
    @DeleteMapping("/{productId}/price-tiers/{tierId}")
    public ResponseEntity<?> deleteProductPriceTier(
            @PathVariable Long productId,
            @PathVariable Long tierId) {
        
        // Build command
        DeleteProductPriceTierCommand command = new DeleteProductPriceTierCommand(
            productId,
            tierId
        );

        // Execute use case
        DeleteProductPriceTierResult result = deleteProductPriceTierUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Get all price tiers for a product.
     * GET /api/v1/products/{productId}/price-tiers
     * 
     * @param productId the product ID
     * @return 200 OK with list of price tiers,
     *         404 NOT FOUND if product doesn't exist
     */
    @Operation(summary = "Get product price tiers", description = "Get all price tiers for a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Price tiers retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ListProductPriceTiersResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content)
    })
    @GetMapping("/{productId}/price-tiers")
    public ResponseEntity<?> listProductPriceTiers(@PathVariable Long productId) {
        
        // Execute use case
        ListProductPriceTiersResult result = listProductPriceTiersUseCase.execute(productId);

        // Handle result
        if (result.isSuccess()) {
            List<ProductPriceTierResponse> tierResponses = result.getPriceTiers().stream()
                .map(ProductPriceTierResponse::fromDomain)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ListProductPriceTiersResponse(tierResponses));
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * List variants for a product with filtering.
     * GET /api/v1/products/{productId}/variants
     * 
     * @param productId the product ID
     * @param color optional color filter
     * @param size optional size filter
     * @return 200 OK with list of variants,
     *         404 NOT FOUND if product doesn't exist
     */
    @Operation(summary = "List product variants", description = "List variants for a product with optional filtering by color and size")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Variants retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ListProductVariantsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content)
    })
    @GetMapping("/{productId}/variants")
    public ResponseEntity<?> listProductVariants(
            @PathVariable Long productId,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size) {
        
        // Execute use case
        ListProductVariantsResult result = listProductVariantsUseCase.execute(productId, color, size);

        // Handle result
        if (result.isSuccess()) {
            List<ProductVariantResponse> variantResponses = result.getVariants().stream()
                .map(ProductVariantResponse::fromDomain)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ListProductVariantsResponse(variantResponses));
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Response wrapper for list of product variants.
     */
    private static class ListProductVariantsResponse {
        public final List<ProductVariantResponse> content;

        public ListProductVariantsResponse(List<ProductVariantResponse> content) {
            this.content = content;
        }
    }

    /**
     * Response wrapper for list of product price tiers.
     */
    private static class ListProductPriceTiersResponse {
        public final List<ProductPriceTierResponse> content;

        public ListProductPriceTiersResponse(List<ProductPriceTierResponse> content) {
            this.content = content;
        }
    }

    /**
     * Create a new variant for an existing product.
     * POST /api/v1/products/{productId}/variants
     * 
     * @param productId the product ID
     * @param request the variant creation request
     * @return 201 CREATED with variant details,
     *         404 NOT FOUND if product doesn't exist,
     *         400 BAD REQUEST for validation errors,
     *         409 CONFLICT if duplicate variant or SKU exists
     */
    @Operation(summary = "Create product variant", description = "Add a new variant to an existing product with default inventory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Variant created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductVariantResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Duplicate variant or SKU",
            content = @Content)
    })
    @PostMapping("/{productId}/variants")
    public ResponseEntity<?> createProductVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductVariantRequest request) {
        
        // Build command
        CreateProductVariantCommand command = new CreateProductVariantCommand(
            productId,
            request.getSku(),
            request.getColor(),
            request.getSize(),
            request.getPriceAdjustment()
        );

        // Execute use case
        CreateProductVariantResult result = createProductVariantUseCase.execute(command);

        // Handle result
        if (result.isSuccess()) {
            ProductVariantResponse response = ProductVariantResponse.fromDomain(result.getVariant());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    
}
