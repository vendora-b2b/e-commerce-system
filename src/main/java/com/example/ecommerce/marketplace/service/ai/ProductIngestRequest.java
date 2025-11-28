package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for ingesting a product into the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngestRequest {
    
    private String sku;
    private Long productId;
    private String name;
    private String description;
    private Long supplierId;
    private String category;
}
