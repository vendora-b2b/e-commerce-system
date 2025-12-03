package com.example.ecommerce.marketplace.web.model.search;

import com.example.ecommerce.marketplace.web.model.product.ProductResponse;
import com.example.ecommerce.marketplace.web.model.supplier.SupplierResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP response DTO for combined product and supplier search results.
 * Contains enriched information with similarity scores for both entity types.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CombinedSearchResultResponse {

    private List<ProductWithScoreItem> products;
    private List<SupplierWithScoreItem> suppliers;
    private int totalProducts;
    private int totalSuppliers;
    private String query;

    /**
     * Creates a response from search results.
     *
     * @param products  the list of products with scores
     * @param suppliers the list of suppliers with scores
     * @param query     the original search query
     * @return the response DTO
     */
    public static CombinedSearchResultResponse of(List<ProductWithScoreItem> products,
                                                   List<SupplierWithScoreItem> suppliers,
                                                   String query) {
        return new CombinedSearchResultResponse(
            products, 
            suppliers, 
            products.size(), 
            suppliers.size(), 
            query
        );
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

    /**
     * Inner class representing a supplier with its similarity score.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierWithScoreItem {
        private SupplierResponse supplier;
        private Double score;

        public static SupplierWithScoreItem of(SupplierResponse supplier, Double score) {
            return new SupplierWithScoreItem(supplier, score);
        }
    }
}
