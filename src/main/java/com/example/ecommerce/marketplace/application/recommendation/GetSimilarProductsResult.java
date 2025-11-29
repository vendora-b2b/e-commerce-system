package com.example.ecommerce.marketplace.application.recommendation;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned after getting similar products.
 */
public class GetSimilarProductsResult {

    private final boolean success;
    private final Long sourceProductId;
    private final List<ProductRecommendation> similarProducts;
    private final int total;
    private final String message;
    private final String errorCode;

    private GetSimilarProductsResult(boolean success, Long sourceProductId,
                                      List<ProductRecommendation> similarProducts,
                                      int total, String message, String errorCode) {
        this.success = success;
        this.sourceProductId = sourceProductId;
        this.similarProducts = similarProducts;
        this.total = total;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static GetSimilarProductsResult success(Long sourceProductId, 
                                                    List<ProductRecommendation> similarProducts) {
        return new GetSimilarProductsResult(
            true, sourceProductId, similarProducts, similarProducts.size(), 
            "Similar products retrieved successfully", null
        );
    }

    public static GetSimilarProductsResult failure(String message, String errorCode) {
        return new GetSimilarProductsResult(false, null, Collections.emptyList(), 0, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public List<ProductRecommendation> getSimilarProducts() {
        return similarProducts;
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
     * Inner class representing a similar product.
     */
    public static class ProductRecommendation {
        private final Long productId;
        private final String sku;
        private final String name;
        private final Double similarityScore;

        public ProductRecommendation(Long productId, String sku, String name, Double similarityScore) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.similarityScore = similarityScore;
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

        public Double getSimilarityScore() {
            return similarityScore;
        }
    }
}
