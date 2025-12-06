package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.search.*;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;
import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.search.CombinedSearchResultResponse;
import com.example.ecommerce.marketplace.web.model.search.ProductSearchResultResponse;
import com.example.ecommerce.marketplace.web.model.search.SupplierSearchResultResponse;
import com.example.ecommerce.marketplace.web.model.supplier.SupplierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for semantic search functionality.
 * Provides endpoints to search for products and suppliers using AI-powered vector search.
 * Returns enriched domain objects with similarity scores.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search", description = "AI-powered semantic search for products and suppliers")
public class SearchController {

    private final SearchProductsUseCase searchProductsUseCase;
    private final SearchSuppliersUseCase searchSuppliersUseCase;
    private final CombinedSearchUseCase combinedSearchUseCase;
    private final SupplierRepository supplierRepository;

    /**
     * Search for both products and suppliers in a single query.
     * This is the main endpoint for the search bar functionality.
     * Returns enriched Product and Supplier objects with similarity scores.
     *
     * @param query         the search query text
     * @param productLimit  maximum number of products to return (default: 10)
     * @param supplierLimit maximum number of suppliers to return (default: 5)
     * @return combined search results with full product and supplier details
     */
    @GetMapping("/combined")
    @Operation(
        summary = "Combined search for products and suppliers",
        description = "Searches both products and suppliers using semantic similarity. " +
                      "Returns full entity details with similarity scores. " +
                      "Ideal for search bar functionality."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = CombinedSearchResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Search service unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> searchCombined(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of products to return")
            @RequestParam(defaultValue = "10") int productLimit,
            @Parameter(description = "Maximum number of suppliers to return")
            @RequestParam(defaultValue = "5") int supplierLimit) {
        
        log.info("Combined search request: query='{}', productLimit={}, supplierLimit={}", 
                query, productLimit, supplierLimit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_QUERY", "Search query is required"));
        }
        
        // Validate limits
        productLimit = Math.min(Math.max(productLimit, 1), 50);
        supplierLimit = Math.min(Math.max(supplierLimit, 1), 20);
        
        // Build command
        CombinedSearchCommand command = new CombinedSearchCommand(query.trim(), productLimit, supplierLimit);
        
        // Execute use case
        CombinedSearchResult result = combinedSearchUseCase.execute(command);
        
        // Handle result
        if (result.isSuccess()) {
            // Convert products to response DTOs
            List<CombinedSearchResultResponse.ProductWithScoreItem> productItems = result.getProducts().stream()
                .map(p -> CombinedSearchResultResponse.ProductWithScoreItem.of(
                    ProductResponse.fromDomain(p.getProduct(), supplierRepository),
                    p.getScore()
                ))
                .collect(Collectors.toList());
            
            // Convert suppliers to response DTOs
            List<CombinedSearchResultResponse.SupplierWithScoreItem> supplierItems = result.getSuppliers().stream()
                .map(s -> CombinedSearchResultResponse.SupplierWithScoreItem.of(
                    SupplierResponse.fromDomain(s.getSupplier()),
                    s.getScore()
                ))
                .collect(Collectors.toList());
            
            CombinedSearchResultResponse response = CombinedSearchResultResponse.of(
                productItems, supplierItems, result.getQuery()
            );
            
            log.info("Combined search completed: {} products, {} suppliers", 
                    result.getTotalProducts(), result.getTotalSuppliers());
            
            return ResponseEntity.ok(response);
        }
        
        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Search for products only.
     * Returns enriched Product objects with similarity scores.
     *
     * @param query the search query text
     * @param limit maximum number of results (default: 20)
     * @return product search results with full product details
     */
    @GetMapping("/products")
    @Operation(
        summary = "Search for products",
        description = "Searches products using semantic similarity. Returns full product details with scores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Search service unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> searchProducts(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Product search request: query='{}', limit={}", query, limit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_QUERY", "Search query is required"));
        }
        
        // Validate limit
        limit = Math.min(Math.max(limit, 1), 100);
        
        // Build command
        SearchProductsCommand command = new SearchProductsCommand(query.trim(), limit);
        
        // Execute use case
        SearchProductsResult result = searchProductsUseCase.execute(command);
        
        // Handle result
        if (result.isSuccess()) {
            List<ProductSearchResultResponse.ProductWithScoreItem> items = result.getProducts().stream()
                .map(p -> ProductSearchResultResponse.ProductWithScoreItem.of(
                    ProductResponse.fromDomain(p.getProduct(), supplierRepository),
                    p.getScore()
                ))
                .collect(Collectors.toList());
            
            ProductSearchResultResponse response = ProductSearchResultResponse.of(items, result.getQuery());
            
            log.info("Product search completed: {} results", result.getTotal());
            
            return ResponseEntity.ok(response);
        }
        
        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Search for suppliers only.
     * Returns enriched Supplier objects with similarity scores.
     *
     * @param query the search query text
     * @param limit maximum number of results (default: 10)
     * @return supplier search results with full supplier details
     */
    @GetMapping("/suppliers")
    @Operation(
        summary = "Search for suppliers",
        description = "Searches suppliers using semantic similarity. Returns full supplier details with scores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = SupplierSearchResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Search service unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> searchSuppliers(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Supplier search request: query='{}', limit={}", query, limit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_QUERY", "Search query is required"));
        }
        
        // Validate limit
        limit = Math.min(Math.max(limit, 1), 50);
        
        // Build command
        SearchSuppliersCommand command = new SearchSuppliersCommand(query.trim(), limit);
        
        // Execute use case
        SearchSuppliersResult result = searchSuppliersUseCase.execute(command);
        
        // Handle result
        if (result.isSuccess()) {
            List<SupplierSearchResultResponse.SupplierWithScoreItem> items = result.getSuppliers().stream()
                .map(s -> SupplierSearchResultResponse.SupplierWithScoreItem.of(
                    SupplierResponse.fromDomain(s.getSupplier()),
                    s.getScore()
                ))
                .collect(Collectors.toList());
            
            SupplierSearchResultResponse response = SupplierSearchResultResponse.of(items, result.getQuery());
            
            log.info("Supplier search completed: {} results", result.getTotal());
            
            return ResponseEntity.ok(response);
        }
        
        // Handle failure
        ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
        HttpStatus status = mapErrorToStatus(result.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Maps error codes to appropriate HTTP status codes.
     */
    private HttpStatus mapErrorToStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (errorCode) {
            case "SEARCH_SERVICE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "INVALID_QUERY" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
