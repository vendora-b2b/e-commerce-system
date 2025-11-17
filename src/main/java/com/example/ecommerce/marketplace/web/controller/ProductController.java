package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.product.CreateProductRequest;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.product.UpdateProductRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductRepository productRepository;

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
                    variant.getVariantSku(),
                    variant.getColor(),
                    variant.getSize(),
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
            request.getCategoryId(),
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
     * GET /api/v1/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
        @PathVariable Long categoryId
    ) {
        List<Product> products = productRepository.findByCategoryId(categoryId);

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
            request.getCategoryId(),
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
}
