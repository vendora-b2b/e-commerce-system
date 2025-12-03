package com.example.ecommerce.marketplace.application.search;

import com.example.ecommerce.marketplace.domain.product.Product;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after searching for products.
 * Contains enriched Product domain objects with similarity scores.
 */
public class SearchProductsResult {

    private final boolean success;
    private final List<ProductWithScore> products;
    private final int total;
    private final String query;
    private final String message;
    private final String errorCode;

    private SearchProductsResult(boolean success, List<ProductWithScore> products,
                                 int total, String query, String message, String errorCode) {
        this.success = success;
        this.products = products;
        this.total = total;
        this.query = query;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static SearchProductsResult success(List<ProductWithScore> products, String query) {
        return new SearchProductsResult(
            true, products, products.size(), query, "Search completed successfully", null
        );
    }

    public static SearchProductsResult failure(String message, String errorCode) {
        return new SearchProductsResult(false, Collections.emptyList(), 0, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ProductWithScore> getProducts() {
        return products;
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
}
