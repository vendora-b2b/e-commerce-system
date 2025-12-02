package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for product-only search from the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    
    private List<ProductSearchResult> products;
    private int total;
    private String query;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchResult {
        private Long productId;
        private String sku;
        private String name;
        private String description;
        private Long supplierId;
        private String category;
        private Double score;
    }
}
