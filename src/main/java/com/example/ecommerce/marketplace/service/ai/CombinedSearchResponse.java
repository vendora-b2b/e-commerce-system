package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for combined product and supplier search from the AI service.
 * Used for search bar functionality where both products and suppliers are returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinedSearchResponse {
    
    private List<ProductSearchResult> products;
    private List<SupplierSearchResult> suppliers;
    private int totalProducts;
    private int totalSuppliers;
    private String query;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchResult {
        private Long productId;
        private String sku;
        private String name;
        private String description;
        private Long supplierId;
        private String category;
        private Double score;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierSearchResult {
        private Long supplierId;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String businessLicense;
        private Double score;
    }
}
