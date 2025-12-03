package com.example.ecommerce.marketplace.application.search;

/**
 * Command object for combined search of products and suppliers using semantic search.
 */
public class CombinedSearchCommand {

    private final String query;
    private final Integer productLimit;
    private final Integer supplierLimit;

    public CombinedSearchCommand(String query, Integer productLimit, Integer supplierLimit) {
        this.query = query;
        this.productLimit = productLimit;
        this.supplierLimit = supplierLimit;
    }

    /**
     * Creates a command with default limits.
     *
     * @param query the search query
     * @return a new command with default limits (10 products, 5 suppliers)
     */
    public static CombinedSearchCommand withDefaults(String query) {
        return new CombinedSearchCommand(query, 10, 5);
    }

    public String getQuery() {
        return query;
    }

    public Integer getProductLimit() {
        return productLimit != null ? productLimit : 10;
    }

    public Integer getSupplierLimit() {
        return supplierLimit != null ? supplierLimit : 5;
    }
}
