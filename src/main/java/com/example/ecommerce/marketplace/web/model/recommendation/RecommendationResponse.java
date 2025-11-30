package com.example.ecommerce.marketplace.web.model.recommendation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP response DTO for product recommendations.
 * Contains a list of recommended products with their relevance scores.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private List<ProductRecommendationItem> recommendations;
    private int total;
    private String recommendationType;

    /**
     * Creates a response for user-based recommendations.
     *
     * @param recommendations the list of recommendations
     * @return the response DTO
     */
    public static RecommendationResponse forUser(List<ProductRecommendationItem> recommendations) {
        return new RecommendationResponse(recommendations, recommendations.size(), "personalized");
    }

    /**
     * Creates a response for similar product recommendations.
     *
     * @param recommendations the list of recommendations
     * @return the response DTO
     */
    public static RecommendationResponse forSimilar(List<ProductRecommendationItem> recommendations) {
        return new RecommendationResponse(recommendations, recommendations.size(), "similar");
    }

    /**
     * Creates a response for homepage recommendations.
     *
     * @param recommendations the list of recommendations
     * @return the response DTO
     */
    public static RecommendationResponse forHomepage(List<ProductRecommendationItem> recommendations) {
        return new RecommendationResponse(recommendations, recommendations.size(), "homepage");
    }

    /**
     * Inner class representing a single product recommendation.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRecommendationItem {
        private Long productId;
        private String sku;
        private String name;
        private Double score;

        /**
         * Creates a recommendation item from individual fields.
         *
         * @param productId the product ID
         * @param sku       the product SKU
         * @param name      the product name
         * @param score     the relevance score
         * @return the recommendation item
         */
        public static ProductRecommendationItem of(Long productId, String sku, String name, Double score) {
            return new ProductRecommendationItem(productId, sku, name, score);
        }
    }
}
