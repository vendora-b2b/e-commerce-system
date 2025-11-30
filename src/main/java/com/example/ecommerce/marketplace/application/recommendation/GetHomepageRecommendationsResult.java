package com.example.ecommerce.marketplace.application.recommendation;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after getting homepage recommendations.
 */
public class GetHomepageRecommendationsResult {

    private final boolean success;
    private final List<ProductRecommendation> recommendations;
    private final boolean personalized;  // True if recommendations are personalized for user
    private final int total;
    private final String message;
    private final String errorCode;

    private GetHomepageRecommendationsResult(boolean success, List<ProductRecommendation> recommendations,
                                              boolean personalized, int total, String message, String errorCode) {
        this.success = success;
        this.recommendations = recommendations;
        this.personalized = personalized;
        this.total = total;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetHomepageRecommendationsResult success(List<ProductRecommendation> recommendations, 
                                                           boolean personalized) {
        return new GetHomepageRecommendationsResult(
            true, recommendations, personalized, recommendations.size(), 
            "Recommendations retrieved successfully", null
        );
    }

    public static GetHomepageRecommendationsResult failure(String message, String errorCode) {
        return new GetHomepageRecommendationsResult(false, Collections.emptyList(), false, 0, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ProductRecommendation> getRecommendations() {
        return recommendations;
    }

    public boolean isPersonalized() {
        return personalized;
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
