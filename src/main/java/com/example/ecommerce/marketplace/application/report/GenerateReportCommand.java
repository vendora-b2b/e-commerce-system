package com.example.ecommerce.marketplace.application.report;

import java.time.LocalDateTime;

/**
 * Command to generate a financial report.
 */
public record GenerateReportCommand(
    String type,                    // PDF or CSV
    LocalDateTime startDate,        // null = no start limit
    LocalDateTime endDate,          // null = no end limit
    boolean includeCompleted,
    boolean includeCancelled,
    Long entityId,                  // retailerId or supplierId
    String entityType               // RETAILER or SUPPLIER
) {
}
