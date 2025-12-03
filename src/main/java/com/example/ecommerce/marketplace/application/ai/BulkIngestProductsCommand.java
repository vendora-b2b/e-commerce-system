package com.example.ecommerce.marketplace.application.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command object for bulk ingesting products into the AI vector database.
 * Used for initial sync or batch updates.
 */
public class BulkIngestProductsCommand {

    private final List<IngestProductCommand> products;

    private BulkIngestProductsCommand(Builder builder) {
        this.products = builder.products != null 
            ? Collections.unmodifiableList(new ArrayList<>(builder.products))
            : Collections.emptyList();
    }

    public List<IngestProductCommand> getProducts() {
        return products;
    }

    public int getProductCount() {
        return products.size();
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<IngestProductCommand> products = new ArrayList<>();

        public Builder products(List<IngestProductCommand> products) {
            this.products = products;
            return this;
        }

        public Builder addProduct(IngestProductCommand product) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            this.products.add(product);
            return this;
        }

        public BulkIngestProductsCommand build() {
            return new BulkIngestProductsCommand(this);
        }
    }
}
