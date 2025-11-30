package com.example.ecommerce.marketplace.application.recommendation;

/**
 * Command object for getting homepage recommendations.
 * Can be used for both authenticated and anonymous users.
 */
public class GetHomepageRecommendationsCommand {

    private final Long userId;  // Nullable for anonymous users
    private final Integer limit;

    public GetHomepageRecommendationsCommand(Long userId, Integer limit) {
        this.userId = userId;
        this.limit = limit;
    }

    /**
     * Creates a command for an anonymous user with default limit.
     *
     * @return a new command for anonymous user
     */
    public static GetHomepageRecommendationsCommand forAnonymous() {
        return new GetHomepageRecommendationsCommand(null, 12);
    }

    /**
     * Creates a command for an authenticated user with default limit.
     *
     * @param userId the user ID
     * @return a new command
     */
    public static GetHomepageRecommendationsCommand forUser(Long userId) {
        return new GetHomepageRecommendationsCommand(userId, 12);
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getLimit() {
        return limit != null ? limit : 12;
    }

    public boolean isAnonymous() {
        return userId == null;
    }
}
