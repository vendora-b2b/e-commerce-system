package com.example.ecommerce.marketplace.web.model.search;

import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP response DTO for product search results.
 * Contains enriched product information with similarity scores.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResultResponse {

    private List<ProductWithScoreItem> products;
    private int total;
    private String query;

    /**
     * Creates a response from search results.
     *
     * @param products the list of products with scores
     * @param query    the original search query
     * @return the response DTO
     */
    public static ProductSearchResultResponse of(List<ProductWithScoreItem> products, String query) {
        return new ProductSearchResultResponse(products, products.size(), query);
    }

    /**
     * Inner class representing a product with its similarity score.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductWithScoreItem {
        private ProductResponse product;
        private Double score;

        public static ProductWithScoreItem of(ProductResponse product, Double score) {
            return new ProductWithScoreItem(product, score);
        }
    }
}
