package com.example.ecommerce.marketplace.application.recommendation;

/**
 * Command object for getting product recommendations for a user.
 */
public class GetProductRecommendationsCommand {

    private final Long userId;
    private final Integer limit;

    public GetProductRecommendationsCommand(Long userId, Integer limit) {
        this.userId = userId;
        this.limit = limit;
    }

    /**
     * Creates a command with default limit.
     *
     * @param userId the user ID
     * @return a new command with default limit of 10
     */
    public static GetProductRecommendationsCommand withDefaults(Long userId) {
        return new GetProductRecommendationsCommand(userId, 10);
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getLimit() {
        return limit != null ? limit : 10;
    }
}
