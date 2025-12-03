package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case for searching products using semantic search.
 * Returns enriched Product domain objects with similarity scores.
 * 
 * Flow:
 * 1. Call AI service to get product IDs and scores
 * 2. Fetch full Product entities from database
 * 3. Combine and return products with their scores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductsUseCase {

    private final AiServiceClient aiServiceClient;
    private final ProductRepository productRepository;

    /**
     * Executes the search products use case.
     *
     * @param command the command containing search query and limit
     * @return the result containing enriched products with scores, or failure result if error occurs
     */
    public SearchProductsResult execute(SearchProductsCommand command) {
        // Step 1: Validate query
        if (command.getQuery() == null || command.getQuery().trim().isEmpty()) {
            return SearchProductsResult.failure("Search query is required", "INVALID_QUERY");
        }

        String query = command.getQuery().trim();
        int limit = command.getLimit();

        try {
            // Step 2: Call AI service to get matching product IDs and scores
            ProductSearchResponse aiResponse = aiServiceClient.searchProducts(query, limit);

            if (aiResponse == null || aiResponse.getProducts() == null || aiResponse.getProducts().isEmpty()) {
                log.debug("No products found for query: '{}'", query);
                return SearchProductsResult.success(List.of(), query);
            }

            // Step 3: Extract product IDs and create score map (filter out nulls)
            List<Long> productIds = aiResponse.getProducts().stream()
                .filter(p -> p.getProductId() != null)
                .map(ProductSearchResponse.ProductSearchResult::getProductId)
                .collect(Collectors.toList());

            Map<Long, Double> scoreMap = aiResponse.getProducts().stream()
                .filter(p -> p.getProductId() != null)
                .collect(Collectors.toMap(
                    ProductSearchResponse.ProductSearchResult::getProductId,
                    p -> p.getScore() != null ? p.getScore() : 0.0,
                    (existing, replacement) -> existing // Keep first in case of duplicates
                ));

            // Step 4: Fetch full Product entities from database
            List<Product> products = productRepository.findAllById(productIds);

            // Step 5: Create a map for quick lookup to preserve order
            Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

            // Step 6: Combine products with scores, preserving AI service order
            List<SearchProductsResult.ProductWithScore> productsWithScores = new ArrayList<>();
            for (Long productId : productIds) {
                Product product = productMap.get(productId);
                if (product != null) {
                    Double score = scoreMap.get(productId);
                    productsWithScores.add(new SearchProductsResult.ProductWithScore(product, score));
                } else {
                    log.warn("Product {} returned by AI service not found in database", productId);
                }
            }

            log.info("Product search completed for '{}': {} results", query, productsWithScores.size());
            return SearchProductsResult.success(productsWithScores, query);

        } catch (AiServiceException e) {
            log.error("AI service error during product search for '{}': {}", query, e.getMessage());
            return SearchProductsResult.failure(
                "Search service is temporarily unavailable: " + e.getMessage(),
                "SEARCH_SERVICE_ERROR"
            );
        } catch (Exception e) {
            log.error("Unexpected error during product search for '{}': {}", query, e.getMessage());
            return SearchProductsResult.failure(
                "Failed to search products: " + e.getMessage(),
                "SEARCH_ERROR"
            );
        }
    }
}
