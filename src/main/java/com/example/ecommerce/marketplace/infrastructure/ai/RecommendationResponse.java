package com.example.ecommerce.marketplace.infrastructure.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for product recommendations from the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private List<ProductRecommendation> recommendations;
    private int total;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRecommendation {
        private Long productId;
        private String sku;
        private String name;
        private Double score;
    }
}
