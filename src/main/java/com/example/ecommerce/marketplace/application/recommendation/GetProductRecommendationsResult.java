package com.example.ecommerce.marketplace.application.recommendation;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after getting product recommendations.
 */
public class GetProductRecommendationsResult {

    private final boolean success;
    private final List<ProductRecommendation> recommendations;
    private final int total;
    private final String message;
    private final String errorCode;

    private GetProductRecommendationsResult(boolean success, List<ProductRecommendation> recommendations,
                                            int total, String message, String errorCode) {
        this.success = success;
        this.recommendations = recommendations;
        this.total = total;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetProductRecommendationsResult success(List<ProductRecommendation> recommendations) {
        return new GetProductRecommendationsResult(
            true, recommendations, recommendations.size(), "Recommendations retrieved successfully", null
        );
    }

    public static GetProductRecommendationsResult failure(String message, String errorCode) {
        return new GetProductRecommendationsResult(false, Collections.emptyList(), 0, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ProductRecommendation> getRecommendations() {
        return recommendations;
    }

    public int getTotal() {
        return total;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Inner class representing a single product recommendation.
     */
    public static class ProductRecommendation {
        private final Long productId;
        private final String sku;
        private final String name;
        private final Double score;

        public ProductRecommendation(Long productId, String sku, String name, Double score) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.score = score;
        }

        public Long getProductId() {
            return productId;
        }

        public String getSku() {
            return sku;
        }

        public String getName() {
            return name;
        }

        public Double getScore() {
            return score;
        }
    }
}
