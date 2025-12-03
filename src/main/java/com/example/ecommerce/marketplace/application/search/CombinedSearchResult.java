package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after combined search for products and suppliers.
 * Contains enriched domain objects with similarity scores.
 */
public class CombinedSearchResult {

    private final boolean success;
    private final List<ProductWithScore> products;
    private final List<SupplierWithScore> suppliers;
    private final int totalProducts;
    private final int totalSuppliers;
    private final String query;
    private final String message;
    private final String errorCode;

    private CombinedSearchResult(boolean success, 
                                 List<ProductWithScore> products,
                                 List<SupplierWithScore> suppliers,
                                 String query, 
                                 String message, 
                                 String errorCode) {
        this.success = success;
        this.products = products;
        this.suppliers = suppliers;
        this.totalProducts = products != null ? products.size() : 0;
        this.totalSuppliers = suppliers != null ? suppliers.size() : 0;
        this.query = query;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CombinedSearchResult success(List<ProductWithScore> products, 
                                                List<SupplierWithScore> suppliers,
                                                String query) {
        return new CombinedSearchResult(
            true, products, suppliers, query, "Search completed successfully", null
        );
    }

    public static CombinedSearchResult failure(String message, String errorCode) {
        return new CombinedSearchResult(
            false, Collections.emptyList(), Collections.emptyList(), null, message, errorCode
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ProductWithScore> getProducts() {
        return products;
    }

    public List<SupplierWithScore> getSuppliers() {
        return suppliers;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getTotalSuppliers() {
        return totalSuppliers;
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
     * Inner class representing a product with its similarity score.
     */
    public static class ProductWithScore {
        private final Product product;
        private final Double score;

        public ProductWithScore(Product product, Double score) {
            this.product = product;
            this.score = score;
        }

        public Product getProduct() {
            return product;
        }

        public Double getScore() {
            return score;
        }
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
