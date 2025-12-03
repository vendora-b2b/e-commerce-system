package com.example.ecommerce.marketplace.web.model.search;

import com.example.ecommerce.marketplace.web.model.supplier.SupplierResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * HTTP response DTO for supplier search results.
 * Contains enriched supplier information with similarity scores.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSearchResultResponse {

    private List<SupplierWithScoreItem> suppliers;
    private int total;
    private String query;

    /**
     * Creates a response from search results.
     *
     * @param suppliers the list of suppliers with scores
     * @param query     the original search query
     * @return the response DTO
     */
    public static SupplierSearchResultResponse of(List<SupplierWithScoreItem> suppliers, String query) {
        return new SupplierSearchResultResponse(suppliers, suppliers.size(), query);
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
