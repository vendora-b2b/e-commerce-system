package com.example.ecommerce.marketplace.application.recommendation;

/**
 * Command object for getting similar products.
 */
public class GetSimilarProductsCommand {

    private final Long productId;
    private final Integer limit;

    public GetSimilarProductsCommand(Long productId, Integer limit) {
        this.productId = productId;
        this.limit = limit;
    }

    /**
     * Creates a command with default limit.
     *
     * @param productId the product ID
     * @return a new command with default limit of 6
     */
    public static GetSimilarProductsCommand withDefaults(Long productId) {
        return new GetSimilarProductsCommand(productId, 6);
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getLimit() {
        return limit != null ? limit : 6;
    }
}
