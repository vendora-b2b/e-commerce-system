package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.web.model.inventory.InventoryResponse;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.supplier.SupplierResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Internal API controller for AI service integration.
 * 
 * These endpoints are designed for internal use by the AI service (Python)
 * to fetch real-time data from the Spring Boot backend for RAG context enrichment.
 * 
 * Endpoints are prefixed with /internal/ai to clearly indicate their purpose.
 */
@RestController
@RequestMapping("/internal/ai")
@RequiredArgsConstructor
@Hidden // Hide from public API documentation
@Tag(name = "Internal AI API", description = "Internal APIs for AI service integration")
public class InternalAiController {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;

    // ==================== Product Endpoints ====================

    /**
     * Get product details by ID.
     * GET /internal/ai/products/{productId}
     */
    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product by ID", description = "Fetch product details for AI context")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(p -> ResponseEntity.ok(ProductResponse.fromDomain(p, supplierRepository)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search products with filters.
     * GET /internal/ai/products/search
     */
    @GetMapping("/products/search")
    @Operation(summary = "Search products", description = "Search products with filters for AI context")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        
        // Convert supplierId to supplierName if provided
        String supplierName = null;
        if (supplierId != null) {
            Optional<Supplier> supplier = supplierRepository.findById(supplierId);
            supplierName = supplier.map(Supplier::getName).orElse(null);
        }
        
        // Use findWithFilters which handles all filter combinations
        // For query-based search, we use null for sku and supplierId, and apply name filtering post-query
        Page<Product> productsPage = productRepository.findWithFilters(null, null, supplierName, category, null, null, pageable);

        List<Product> products = productsPage.getContent();

        // Apply query filter on name (case-insensitive search)
        if (query != null && !query.isBlank()) {
            String lowerQuery = query.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerQuery) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());
        }

        // Apply price filter if provided
        if (minPrice != null || maxPrice != null) {
            products = products.stream()
                    .filter(p -> (minPrice == null || p.getBasePrice() >= minPrice))
                    .filter(p -> (maxPrice == null || p.getBasePrice() <= maxPrice))
                    .collect(Collectors.toList());
        }

        List<ProductResponse> productResponses = products.stream()
                .map(p -> ProductResponse.fromDomain(p, supplierRepository))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ProductSearchResponse(productResponses, (long) productResponses.size()));
    }

    /**
     * Get multiple products by IDs (batch fetch).
     * POST /internal/ai/products/batch
     */
    @PostMapping("/products/batch")
    @Operation(summary = "Batch get products", description = "Fetch multiple products by IDs for AI context")
    public ResponseEntity<ProductBatchResponse> getProductsByIds(@RequestBody ProductBatchRequest request) {
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ResponseEntity.ok(new ProductBatchResponse(List.of()));
        }

        List<Product> products = productRepository.findAllById(request.getProductIds());
        List<ProductResponse> productResponses = products.stream()
                .map(p -> ProductResponse.fromDomain(p, supplierRepository))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ProductBatchResponse(productResponses));
    }

    // ==================== Supplier Endpoints ====================

    /**
     * Get supplier information by ID.
     * GET /internal/ai/suppliers/{supplierId}
     */
    @GetMapping("/suppliers/{supplierId}")
    @Operation(summary = "Get supplier by ID", description = "Fetch supplier details for AI context")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long supplierId) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        return supplier.map(s -> ResponseEntity.ok(SupplierResponse.fromDomain(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search suppliers by name.
     * GET /internal/ai/suppliers/search
     */
    @GetMapping("/suppliers/search")
    @Operation(summary = "Search suppliers by name", description = "Search suppliers by name for AI context")
    public ResponseEntity<SupplierSearchResponse> searchSuppliers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<Supplier> suppliers;
        
        if (query != null && !query.isBlank()) {
            // Search by name (case-insensitive) - load all and filter
            // Note: This is acceptable for AI service internal use with small supplier datasets
            // For production scale, consider adding findByNameContainingIgnoreCase to repository
            String lowerQuery = query.toLowerCase();
            suppliers = supplierRepository.findAll().stream()
                    .filter(s -> s.getName().toLowerCase().contains(lowerQuery))
                    .limit(Math.min(limit, 50))
                    .collect(Collectors.toList());
        } else {
            // Return all suppliers with pagination
            suppliers = supplierRepository.findAll();
            if (suppliers.size() > pageable.getPageSize()) {
                suppliers = suppliers.subList(0, pageable.getPageSize());
            }
        }
        
        List<SupplierResponse> supplierResponses = suppliers.stream()
                .map(SupplierResponse::fromDomain)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new SupplierSearchResponse(supplierResponses, (long) supplierResponses.size()));
    }

    // ==================== Inventory Endpoints ====================

    /**
     * Get inventory status for a product.
     * GET /internal/ai/inventory/{productId}
     */
    @GetMapping("/inventory/{productId}")
    @Operation(summary = "Get inventory status", description = "Fetch inventory status for AI context")
    public ResponseEntity<InventoryStatusResponse> getInventoryStatus(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId
    ) {
        Optional<Inventory> inventory;
        if (variantId != null) {
            inventory = inventoryRepository.findByProductIdAndVariantId(productId, variantId);
        } else {
            inventory = inventoryRepository.findByProductId(productId);
        }

        return inventory.map(inv -> {
            InventoryStatusResponse response = new InventoryStatusResponse();
            response.setProductId(inv.getProductId());
            response.setVariantId(inv.getVariantId());
            response.setAvailableQuantity(inv.getAvailableQuantity());
            response.setReservedQuantity(inv.getReservedQuantity());
            response.setReorderPoint(inv.getReorderLevel());
            response.setInStock(inv.getAvailableQuantity() > 0);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== Request/Response DTOs ====================

    /**
     * Response for product search.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchResponse {
        private List<ProductResponse> products;
        private Long totalCount;
    }

    /**
     * Response for supplier search.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierSearchResponse {
        private List<SupplierResponse> suppliers;
        private Long totalCount;
    }

    /**
     * Request for batch product fetch.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBatchRequest {
        private List<Long> productIds;
    }

    /**
     * Response for batch product fetch.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBatchResponse {
        private List<ProductResponse> products;
    }

    /**
     * Response for inventory status (simplified for AI use).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryStatusResponse {
        private Long productId;
        private Long variantId;
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private Integer reorderPoint;
        private Boolean inStock;
    }
}
