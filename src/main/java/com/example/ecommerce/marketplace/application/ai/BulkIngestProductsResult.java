package com.example.ecommerce.marketplace.application.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result object returned after bulk product ingestion.
 */
public class BulkIngestProductsResult {

    private final boolean success;
    private final int totalCount;
    private final int successCount;
    private final int failedCount;
    private final String message;
    private final String errorCode;
    private final List<FailedProduct> failures;

    private BulkIngestProductsResult(boolean success, int totalCount, int successCount, 
                                      int failedCount, String message, String errorCode,
                                      List<FailedProduct> failures) {
        this.success = success;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.message = message;
        this.errorCode = errorCode;
        this.failures = failures != null 
            ? Collections.unmodifiableList(new ArrayList<>(failures))
            : Collections.emptyList();
    }

    public static BulkIngestProductsResult success(int totalCount, int successCount) {
        return new BulkIngestProductsResult(
            true, 
            totalCount, 
            successCount, 
            totalCount - successCount,
            String.format("Bulk ingestion complete: %d/%d products ingested", successCount, totalCount),
            null,
            Collections.emptyList()
        );
    }

    public static BulkIngestProductsResult partialSuccess(int totalCount, int successCount, 
                                                           List<FailedProduct> failures) {
        return new BulkIngestProductsResult(
            true, // Still considered success if any products ingested
            totalCount,
            successCount,
            failures.size(),
            String.format("Bulk ingestion partial: %d/%d products ingested, %d failed", 
                successCount, totalCount, failures.size()),
            null,
            failures
        );
    }

    public static BulkIngestProductsResult failure(String message, String errorCode) {
        return new BulkIngestProductsResult(false, 0, 0, 0, message, errorCode, Collections.emptyList());
    }

    public static BulkIngestProductsResult empty() {
        return new BulkIngestProductsResult(true, 0, 0, 0, "No products to ingest", null, Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<FailedProduct> getFailures() {
        return failures;
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    /**
     * Represents a product that failed to ingest.
     */
    public static class FailedProduct {
        private final Long productId;
        private final String sku;
        private final String reason;

        public FailedProduct(Long productId, String sku, String reason) {
            this.productId = productId;
            this.sku = sku;
            this.reason = reason;
        }

        public Long getProductId() {
            return productId;
        }

        public String getSku() {
            return sku;
        }

        public String getReason() {
            return reason;
        }
    }
}
