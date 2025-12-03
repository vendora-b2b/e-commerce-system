package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.CombinedSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case for combined search of products and suppliers using semantic search.
 * Returns enriched domain objects with similarity scores.
 * 
 * Flow:
 * 1. Call AI service to get product/supplier IDs and scores
 * 2. Fetch full entities from database
 * 3. Combine and return with scores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CombinedSearchUseCase {

    private final AiServiceClient aiServiceClient;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    /**
     * Executes the combined search use case.
     *
     * @param command the command containing search query and limits
     * @return the result containing enriched products and suppliers with scores, or failure result if error occurs
     */
    public CombinedSearchResult execute(CombinedSearchCommand command) {
        // Step 1: Validate query
        if (command.getQuery() == null || command.getQuery().trim().isEmpty()) {
            return CombinedSearchResult.failure("Search query is required", "INVALID_QUERY");
        }

        String query = command.getQuery().trim();
        int productLimit = command.getProductLimit();
        int supplierLimit = command.getSupplierLimit();

        try {
            // Step 2: Call AI service to get matching IDs and scores
            CombinedSearchResponse aiResponse = aiServiceClient.searchCombined(query, productLimit, supplierLimit);

            if (aiResponse == null) {
                log.debug("No results found for query: '{}'", query);
                return CombinedSearchResult.success(List.of(), List.of(), query);
            }

            // Step 3: Process products
            List<CombinedSearchResult.ProductWithScore> productsWithScores = new ArrayList<>();
            if (aiResponse.getProducts() != null && !aiResponse.getProducts().isEmpty()) {
                productsWithScores = enrichProducts(aiResponse.getProducts());
            }

            // Step 4: Process suppliers
            List<CombinedSearchResult.SupplierWithScore> suppliersWithScores = new ArrayList<>();
            if (aiResponse.getSuppliers() != null && !aiResponse.getSuppliers().isEmpty()) {
                suppliersWithScores = enrichSuppliers(aiResponse.getSuppliers());
            }

            log.info("Combined search completed for '{}': {} products, {} suppliers", 
                    query, productsWithScores.size(), suppliersWithScores.size());
            
            return CombinedSearchResult.success(productsWithScores, suppliersWithScores, query);

        } catch (AiServiceException e) {
            log.error("AI service error during combined search for '{}': {}", query, e.getMessage());
            return CombinedSearchResult.failure(
                "Search service is temporarily unavailable: " + e.getMessage(),
                "SEARCH_SERVICE_ERROR"
            );
        } catch (Exception e) {
            log.error("Unexpected error during combined search for '{}': {}", query, e.getMessage());
            return CombinedSearchResult.failure(
                "Failed to search: " + e.getMessage(),
                "SEARCH_ERROR"
            );
        }
    }

    /**
     * Enriches product search results with full Product entities from database.
     */
    private List<CombinedSearchResult.ProductWithScore> enrichProducts(
            List<CombinedSearchResponse.ProductSearchResult> aiProducts) {
        
        // Extract IDs and scores (filter out nulls)
        List<Long> productIds = aiProducts.stream()
            .filter(p -> p.getProductId() != null)
            .map(CombinedSearchResponse.ProductSearchResult::getProductId)
            .collect(Collectors.toList());

        Map<Long, Double> scoreMap = aiProducts.stream()
            .filter(p -> p.getProductId() != null)
            .collect(Collectors.toMap(
                CombinedSearchResponse.ProductSearchResult::getProductId,
                p -> p.getScore() != null ? p.getScore() : 0.0,
                (existing, replacement) -> existing
            ));

        // Fetch from database
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Combine, preserving AI order
        List<CombinedSearchResult.ProductWithScore> result = new ArrayList<>();
        for (Long productId : productIds) {
            Product product = productMap.get(productId);
            if (product != null) {
                result.add(new CombinedSearchResult.ProductWithScore(product, scoreMap.get(productId)));
            } else {
                log.warn("Product {} returned by AI service not found in database", productId);
            }
        }
        return result;
    }

    /**
     * Enriches supplier search results with full Supplier entities from database.
     */
    private List<CombinedSearchResult.SupplierWithScore> enrichSuppliers(
            List<CombinedSearchResponse.SupplierSearchResult> aiSuppliers) {
        
        // Extract IDs and scores (filter out nulls)
        List<Long> supplierIds = aiSuppliers.stream()
            .filter(s -> s.getSupplierId() != null)
            .map(CombinedSearchResponse.SupplierSearchResult::getSupplierId)
            .collect(Collectors.toList());

        Map<Long, Double> scoreMap = aiSuppliers.stream()
            .filter(s -> s.getSupplierId() != null)
            .collect(Collectors.toMap(
                CombinedSearchResponse.SupplierSearchResult::getSupplierId,
                s -> s.getScore() != null ? s.getScore() : 0.0,
                (existing, replacement) -> existing
            ));

        // Fetch from database
        List<Supplier> suppliers = supplierRepository.findAllById(supplierIds);
        Map<Long, Supplier> supplierMap = suppliers.stream()
            .collect(Collectors.toMap(Supplier::getId, Function.identity()));

        // Combine, preserving AI order
        List<CombinedSearchResult.SupplierWithScore> result = new ArrayList<>();
        for (Long supplierId : supplierIds) {
            Supplier supplier = supplierMap.get(supplierId);
            if (supplier != null) {
                result.add(new CombinedSearchResult.SupplierWithScore(supplier, scoreMap.get(supplierId)));
            } else {
                log.warn("Supplier {} returned by AI service not found in database", supplierId);
            }
        }
        return result;
    }
}
