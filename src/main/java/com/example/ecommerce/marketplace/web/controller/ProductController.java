package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.application.inventory.*;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.product.CreateProductRequest;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.product.UpdateProductRequest;
import com.example.ecommerce.marketplace.web.model.inventory.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final ProductRepository productRepository;
    private final RestockInventoryUseCase restockInventoryUseCase;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final ReleaseInventoryUseCase releaseInventoryUseCase;
    private final DeductInventoryUseCase deductInventoryUseCase;
    private final CheckInventoryAvailabilityUseCase checkInventoryAvailabilityUseCase;
    private final InventoryRepository inventoryRepository;

    /**
     * Create a new product.
     * POST /api/v1/products
     */
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
                    tier.getPricePerUnit(),
                    tier.getDiscountPercent()
                ))
                .collect(Collectors.toList());
        }

        // Convert variants from request to command DTOs
        List<CreateProductCommand.ProductVariantDto> variantDtos = null;
        if (request.getVariants() != null) {
            variantDtos = request.getVariants().stream()
                .map(variant -> new CreateProductCommand.ProductVariantDto(
                    variant.getVariantName(),
                    variant.getVariantValue(),
                    variant.getPriceAdjustment(),
                    variant.getImages()
                ))
                .collect(Collectors.toList());
        }

        // Convert request to command
        CreateProductCommand command = new CreateProductCommand(
            request.getSku(),
            request.getName(),
            request.getDescription(),
            request.getCategory(),
            request.getBasePrice(),
            request.getMinimumOrderQuantity(),
            request.getSupplierId(),
            request.getImages(),
            priceTierDtos,
            variantDtos
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
     * Get product by ID.
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            ProductResponse response = ProductResponse.fromDomain(product.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get product by SKU.
     * GET /api/v1/products/sku/{sku}
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        Optional<Product> product = productRepository.findBySku(sku);

        if (product.isPresent()) {
            ProductResponse response = ProductResponse.fromDomain(product.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get all products by supplier ID.
     * GET /api/v1/products/supplier/{supplierId}
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ProductResponse>> getProductsBySupplierId(
        @PathVariable Long supplierId
    ) {
        List<Product> products = productRepository.findBySupplierId(supplierId);
        
        List<ProductResponse> responses = products.stream()
            .map(ProductResponse::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all products by category.
     * GET /api/v1/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
        @PathVariable String category
    ) {
        List<Product> products = productRepository.findByCategory(category);
        
        List<ProductResponse> responses = products.stream()
            .map(ProductResponse::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update product information.
     * PUT /api/v1/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        // Convert request to command
        UpdateProductCommand command = new UpdateProductCommand(
            id,
            request.getName(),
            request.getDescription(),
            request.getCategory(),
            request.getBasePrice(),
            request.getMinimumOrderQuantity()
        );

        // Execute use case
        UpdateProductResult result = updateProductUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated product to return full details
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
     * Delete a product (soft delete by deactivating).
     * DELETE /api/v1/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        
        try {
            // Deactivate the product (soft delete)
            product.deactivate();
            productRepository.save(product);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // Cannot deactivate discontinued product
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Activate a product.
     * POST /api/v1/products/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        
        try {
            product.activate();
            Product updatedProduct = productRepository.save(product);
            ProductResponse response = ProductResponse.fromDomain(updatedProduct);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Cannot activate discontinued product
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Deactivate a product.
     * POST /api/v1/products/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivateProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        
        try {
            product.deactivate();
            Product updatedProduct = productRepository.save(product);
            ProductResponse response = ProductResponse.fromDomain(updatedProduct);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Cannot deactivate discontinued product
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Discontinue a product.
     * POST /api/v1/products/{id}/discontinue
     */
    @PostMapping("/{id}/discontinue")
    public ResponseEntity<ProductResponse> discontinueProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        product.discontinue();
        Product updatedProduct = productRepository.save(product);
        ProductResponse response = ProductResponse.fromDomain(updatedProduct);
        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory for a specific product.
     * GET /api/v1/products/{productId}/inventory
     */
    @GetMapping("/{productId}/inventory")
    public ResponseEntity<InventoryResponse> getInventoryByProduct(@PathVariable Long productId) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);

        if (inventory.isPresent()) {
            InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Check inventory availability for a product.
     * GET /api/v1/products/{productId}/inventory/availability
     */
    @GetMapping("/{productId}/inventory/availability")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        CheckInventoryAvailabilityCommand command = new CheckInventoryAvailabilityCommand(
            productId,
            quantity
        );

        // Execute use case
        CheckInventoryAvailabilityResult result = checkInventoryAvailabilityUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            CheckAvailabilityResponse response = CheckAvailabilityResponse.fromResult(result);
            return ResponseEntity.ok(response);
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Restock inventory for a product.
     * POST /api/v1/products/{productId}/inventory/restock
     */
    @PostMapping("/{productId}/inventory/restock")
    public ResponseEntity<InventoryResponse> restockInventory(
        @PathVariable Long productId,
        @Valid @RequestBody RestockInventoryRequest request
    ) {
        // Create command
        RestockInventoryCommand command = new RestockInventoryCommand(
            productId,
            request.getQuantity()
        );

        // Execute use case
        RestockInventoryResult result = restockInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Reserve inventory for a product.
     * POST /api/v1/products/{productId}/inventory/reserve
     */
    @PostMapping("/{productId}/inventory/reserve")
    public ResponseEntity<InventoryResponse> reserveInventory(
        @PathVariable Long productId,
        @Valid @RequestBody ReserveInventoryRequest request
    ) {
        // Create command
        ReserveInventoryCommand command = new ReserveInventoryCommand(
            productId,
            request.getQuantity()
        );

        // Execute use case
        ReserveInventoryResult result = reserveInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Release reserved inventory for a product.
     * POST /api/v1/products/{productId}/inventory/release
     */
    @PostMapping("/{productId}/inventory/release")
    public ResponseEntity<InventoryResponse> releaseInventory(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        ReleaseInventoryCommand command = new ReleaseInventoryCommand(
            productId,
            quantity
        );

        // Execute use case
        ReleaseInventoryResult result = releaseInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }

    /**
     * Deduct reserved inventory for a product (after order confirmation).
     * POST /api/v1/products/{productId}/inventory/deduct
     */
    @PostMapping("/{productId}/inventory/deduct")
    public ResponseEntity<InventoryResponse> deductInventory(
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        // Create command
        DeductInventoryCommand command = new DeductInventoryCommand(
            productId,
            quantity
        );

        // Execute use case
        DeductInventoryResult result = deductInventoryUseCase.execute(command);

        // Convert result to response
        if (result.isSuccess()) {
            // Fetch the updated inventory to return full details
            Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
            if (inventory.isPresent()) {
                InventoryResponse response = InventoryResponse.fromDomain(inventory.get());
                return ResponseEntity.ok(response);
            }
        }

        // Handle failure
        HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
        return ResponseEntity.status(status).build();
    }
}
