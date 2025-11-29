package com.example.ecommerce.marketplace.application.ai;

import java.util.List;
import java.util.Map;

/**
 * Command object for ingesting a product into the AI vector database.
 */
public class IngestProductCommand {

    private final Long productId;
    private final String sku;
    private final String name;
    private final String description;
    private final String categoryName;
    private final Double basePrice;
    private final List<String> tags;
    private final Map<String, Object> additionalMetadata;

    private IngestProductCommand(Builder builder) {
        this.productId = builder.productId;
        this.sku = builder.sku;
        this.name = builder.name;
        this.description = builder.description;
        this.categoryName = builder.categoryName;
        this.basePrice = builder.basePrice;
        this.tags = builder.tags;
        this.additionalMetadata = builder.additionalMetadata;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Object> getAdditionalMetadata() {
        return additionalMetadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long productId;
        private String sku;
        private String name;
        private String description;
        private String categoryName;
        private Double basePrice;
        private List<String> tags;
        private Map<String, Object> additionalMetadata;

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder basePrice(Double basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder additionalMetadata(Map<String, Object> additionalMetadata) {
            this.additionalMetadata = additionalMetadata;
            return this;
        }

        public IngestProductCommand build() {
            return new IngestProductCommand(this);
        }
    }
}
