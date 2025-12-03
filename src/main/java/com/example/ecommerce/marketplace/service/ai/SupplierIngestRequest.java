package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for ingesting a supplier into the AI service.
 * Contains supplier data for vector embedding and semantic search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierIngestRequest {
    
    private Long supplierId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String businessLicense;
}
