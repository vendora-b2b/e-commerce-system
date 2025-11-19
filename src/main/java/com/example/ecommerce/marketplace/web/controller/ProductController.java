package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.common.PagedResponse;
import com.example.ecommerce.marketplace.web.model.product.CreateProductRequest;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;

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

    
}
