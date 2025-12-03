package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for supplier-only search from the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSearchResponse {
    
    private List<SupplierSearchResult> suppliers;
    private int total;
    private String query;
    
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
