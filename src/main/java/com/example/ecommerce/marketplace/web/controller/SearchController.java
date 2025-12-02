package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.CombinedSearchResponse;
import com.example.ecommerce.marketplace.service.ai.ProductSearchResponse;
import com.example.ecommerce.marketplace.service.ai.SupplierSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for semantic search functionality.
 * Provides endpoints to search for products and suppliers using AI-powered vector search.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search", description = "AI-powered semantic search for products and suppliers")
public class SearchController {

    private final AiServiceClient aiServiceClient;

    /**
     * Search for both products and suppliers in a single query.
     * This is the main endpoint for the search bar functionality.
     *
     * @param query         the search query text
     * @param productLimit  maximum number of products to return (default: 10)
     * @param supplierLimit maximum number of suppliers to return (default: 5)
     * @return combined search results with both products and suppliers
     */
    @GetMapping("/combined")
    @Operation(
        summary = "Combined search for products and suppliers",
        description = "Searches both products and suppliers using semantic similarity. " +
                      "Ideal for search bar functionality where users want to find both."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
        @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    public ResponseEntity<CombinedSearchResponse> searchCombined(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of products to return")
            @RequestParam(defaultValue = "10") int productLimit,
            @Parameter(description = "Maximum number of suppliers to return")
            @RequestParam(defaultValue = "5") int supplierLimit) {
        
        log.info("Combined search request: query='{}', productLimit={}, supplierLimit={}", 
                query, productLimit, supplierLimit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate limits
        productLimit = Math.min(Math.max(productLimit, 1), 50);
        supplierLimit = Math.min(Math.max(supplierLimit, 1), 20);
        
        try {
            CombinedSearchResponse response = aiServiceClient.searchCombined(
                    query.trim(), productLimit, supplierLimit);
            
            log.info("Combined search completed: {} products, {} suppliers", 
                    response.getTotalProducts(), response.getTotalSuppliers());
            
            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            log.error("AI service error during combined search: {}", e.getMessage());
            return ResponseEntity.status(503).build();
        }
    }

    /**
     * Search for products only.
     *
     * @param query the search query text
     * @param limit maximum number of results (default: 20)
     * @return product search results
     */
    @GetMapping("/products")
    @Operation(
        summary = "Search for products",
        description = "Searches products using semantic similarity based on name and description."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
        @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Product search request: query='{}', limit={}", query, limit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate limit
        limit = Math.min(Math.max(limit, 1), 100);
        
        try {
            ProductSearchResponse response = aiServiceClient.searchProducts(query.trim(), limit);
            
            log.info("Product search completed: {} results", response.getTotal());
            
            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            log.error("AI service error during product search: {}", e.getMessage());
            return ResponseEntity.status(503).build();
        }
    }

    /**
     * Search for suppliers only.
     *
     * @param query the search query text
     * @param limit maximum number of results (default: 10)
     * @return supplier search results
     */
    @GetMapping("/suppliers")
    @Operation(
        summary = "Search for suppliers",
        description = "Searches suppliers using semantic similarity based on name and location."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
        @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    public ResponseEntity<SupplierSearchResponse> searchSuppliers(
            @Parameter(description = "Search query text", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Supplier search request: query='{}', limit={}", query, limit);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate limit
        limit = Math.min(Math.max(limit, 1), 50);
        
        try {
            SupplierSearchResponse response = aiServiceClient.searchSuppliers(query.trim(), limit);
            
            log.info("Supplier search completed: {} results", response.getTotal());
            
            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            log.error("AI service error during supplier search: {}", e.getMessage());
            return ResponseEntity.status(503).build();
        }
    }
}
