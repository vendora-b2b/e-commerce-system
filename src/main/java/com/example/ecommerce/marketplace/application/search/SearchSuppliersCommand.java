package com.example.ecommerce.marketplace.application.search;

/**
 * Command object for searching suppliers using semantic search.
 */
public class SearchSuppliersCommand {

    private final String query;
    private final Integer limit;

    public SearchSuppliersCommand(String query, Integer limit) {
        this.query = query;
        this.limit = limit;
    }

    /**
     * Creates a command with default limit.
     *
     * @param query the search query
     * @return a new command with default limit of 10
     */
    public static SearchSuppliersCommand withDefaults(String query) {
        return new SearchSuppliersCommand(query, 10);
    }

    public String getQuery() {
        return query;
    }

    public Integer getLimit() {
        return limit != null ? limit : 10;
    }
}
