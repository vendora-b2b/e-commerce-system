package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.service.ai.AiServiceClient;
import com.example.ecommerce.marketplace.service.ai.AiServiceException;
import com.example.ecommerce.marketplace.service.ai.SupplierSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case for searching suppliers using semantic search.
 * Returns enriched Supplier domain objects with similarity scores.
 * 
 * Flow:
 * 1. Call AI service to get supplier IDs and scores
 * 2. Fetch full Supplier entities from database
 * 3. Combine and return suppliers with their scores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSuppliersUseCase {

    private final AiServiceClient aiServiceClient;
    private final SupplierRepository supplierRepository;

    /**
     * Executes the search suppliers use case.
     *
     * @param command the command containing search query and limit
     * @return the result containing enriched suppliers with scores, or failure result if error occurs
     */
    public SearchSuppliersResult execute(SearchSuppliersCommand command) {
        // Step 1: Validate query
        if (command.getQuery() == null || command.getQuery().trim().isEmpty()) {
            return SearchSuppliersResult.failure("Search query is required", "INVALID_QUERY");
        }

        String query = command.getQuery().trim();
        int limit = command.getLimit();

        try {
            // Step 2: Call AI service to get matching supplier IDs and scores
            SupplierSearchResponse aiResponse = aiServiceClient.searchSuppliers(query, limit);

            if (aiResponse == null || aiResponse.getSuppliers() == null || aiResponse.getSuppliers().isEmpty()) {
                log.debug("No suppliers found for query: '{}'", query);
                return SearchSuppliersResult.success(List.of(), query);
            }

            // Step 3: Extract supplier IDs and create score map (filter out nulls)
            List<Long> supplierIds = aiResponse.getSuppliers().stream()
                .filter(s -> s.getSupplierId() != null)
                .map(SupplierSearchResponse.SupplierSearchResult::getSupplierId)
                .collect(Collectors.toList());

            Map<Long, Double> scoreMap = aiResponse.getSuppliers().stream()
                .filter(s -> s.getSupplierId() != null)
                .collect(Collectors.toMap(
                    SupplierSearchResponse.SupplierSearchResult::getSupplierId,
                    s -> s.getScore() != null ? s.getScore() : 0.0,
                    (existing, replacement) -> existing // Keep first in case of duplicates
                ));

            // Step 4: Fetch full Supplier entities from database
            List<Supplier> suppliers = supplierRepository.findAllById(supplierIds);

            // Step 5: Create a map for quick lookup to preserve order
            Map<Long, Supplier> supplierMap = suppliers.stream()
                .collect(Collectors.toMap(Supplier::getId, Function.identity()));

            // Step 6: Combine suppliers with scores, preserving AI service order
            List<SearchSuppliersResult.SupplierWithScore> suppliersWithScores = new ArrayList<>();
            for (Long supplierId : supplierIds) {
                Supplier supplier = supplierMap.get(supplierId);
                if (supplier != null) {
                    Double score = scoreMap.get(supplierId);
                    suppliersWithScores.add(new SearchSuppliersResult.SupplierWithScore(supplier, score));
                } else {
                    log.warn("Supplier {} returned by AI service not found in database", supplierId);
                }
            }

            log.info("Supplier search completed for '{}': {} results", query, suppliersWithScores.size());
            return SearchSuppliersResult.success(suppliersWithScores, query);

        } catch (AiServiceException e) {
            log.error("AI service error during supplier search for '{}': {}", query, e.getMessage());
            return SearchSuppliersResult.failure(
                "Search service is temporarily unavailable: " + e.getMessage(),
                "SEARCH_SERVICE_ERROR"
            );
        } catch (Exception e) {
            log.error("Unexpected error during supplier search for '{}': {}", query, e.getMessage());
            return SearchSuppliersResult.failure(
                "Failed to search suppliers: " + e.getMessage(),
                "SEARCH_ERROR"
            );
        }
    }
}
