package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String categoryName;  // Alias for category, for consistency
    private Double basePrice;
    private List<String> tags;
}
