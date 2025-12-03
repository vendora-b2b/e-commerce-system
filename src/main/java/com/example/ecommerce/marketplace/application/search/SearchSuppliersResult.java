package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after searching for suppliers.
 * Contains enriched Supplier domain objects with similarity scores.
 */
public class SearchSuppliersResult {

    private final boolean success;
    private final List<SupplierWithScore> suppliers;
    private final int total;
    private final String query;
    private final String message;
    private final String errorCode;

    private SearchSuppliersResult(boolean success, List<SupplierWithScore> suppliers,
                                  int total, String query, String message, String errorCode) {
        this.success = success;
        this.suppliers = suppliers;
        this.total = total;
        this.query = query;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static SearchSuppliersResult success(List<SupplierWithScore> suppliers, String query) {
        return new SearchSuppliersResult(
            true, suppliers, suppliers.size(), query, "Search completed successfully", null
        );
    }

    public static SearchSuppliersResult failure(String message, String errorCode) {
        return new SearchSuppliersResult(false, Collections.emptyList(), 0, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<SupplierWithScore> getSuppliers() {
        return suppliers;
    }

    public int getTotal() {
        return total;
    }

    public String getQuery() {
        return query;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Inner class representing a supplier with its similarity score.
     */
    public static class SupplierWithScore {
        private final Supplier supplier;
        private final Double score;

        public SupplierWithScore(Supplier supplier, Double score) {
            this.supplier = supplier;
            this.score = score;
        }

        public Supplier getSupplier() {
            return supplier;
        }

        public Double getScore() {
            return score;
        }
    }
}
