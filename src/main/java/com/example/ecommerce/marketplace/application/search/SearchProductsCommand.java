package com.example.ecommerce.marketplace.application.search;

/**
 * Command object for searching products using semantic search.
 */
public class SearchProductsCommand {

    private final String query;
    private final Integer limit;

    public SearchProductsCommand(String query, Integer limit) {
        this.query = query;
        this.limit = limit;
    }

    /**
     * Creates a command with default limit.
     *
     * @param query the search query
     * @return a new command with default limit of 20
     */
    public static SearchProductsCommand withDefaults(String query) {
        return new SearchProductsCommand(query, 20);
    }

    public String getQuery() {
        return query;
    }

    public Integer getLimit() {
        return limit != null ? limit : 20;
    }
}
