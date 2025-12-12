package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.report.GenerateReportCommand;
import com.example.ecommerce.marketplace.application.report.GenerateReportUseCase;
import com.example.ecommerce.marketplace.application.report.GenerateReportResult;
import com.example.ecommerce.marketplace.web.common.ErrorMapper;
import com.example.ecommerce.marketplace.web.model.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for Report operations.
 * Handles generation and export of financial reports in PDF and CSV formats.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report", description = "Report API")
public class ReportController {

    private final GenerateReportUseCase generateReportUseCase;

    /**
     * Generate and export financial report.
     * GET /api/v1/reports/export
     * 
     * @param type Report type: PDF or CSV
     * @param startDate Filter orders from this date (ISO format: 2024-01-01T00:00:00)
     * @param endDate Filter orders until this date (ISO format: 2024-12-31T23:59:59)
     * @param includeCompleted Include completed orders in report
     * @param includeCancelled Include cancelled orders in report
     * @param entityId Entity ID (retailerId or supplierId)
     * @param entityType Entity type: RETAILER or SUPPLIER
     * @return byte[] with appropriate headers for file download
     */
    @Operation(summary = "Export financial report", description = "Generate and download financial report in PDF or CSV format")
    @GetMapping("/export")
    public ResponseEntity<?> exportReport(
        @RequestParam(defaultValue = "PDF") String type,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "true") boolean includeCompleted,
        @RequestParam(defaultValue = "false") boolean includeCancelled,
        @RequestParam Long entityId,
        @RequestParam String entityType
    ) {
        try {
            // Parse date parameters
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
            }

            // Create command
            GenerateReportCommand command = new GenerateReportCommand(
                type.toUpperCase(),
                start,
                end,
                includeCompleted,
                includeCancelled,
                entityId,
                entityType.toUpperCase()
            );

            // Execute use case
            GenerateReportResult result = generateReportUseCase.execute(command);

            if (!result.isSuccess()) {
                HttpStatus status = ErrorMapper.toHttpStatus(result.getErrorCode());
                ErrorResponse errorResponse = ErrorResponse.of(result.getErrorCode(), result.getMessage());
                return ResponseEntity.status(status).body(errorResponse);
            }

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            String filename = String.format("report_%s_%s.%s",
                entityType.toLowerCase(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                type.equalsIgnoreCase("PDF") ? "pdf" : "csv"
            );
            headers.setContentDispositionFormData("attachment", filename);
            
            if (type.equalsIgnoreCase("PDF")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.parseMediaType("text/csv"));
            }

            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getFileData());

        } catch (Exception e) {
            log.error("Error generating report", e);
            ErrorResponse errorResponse = ErrorResponse.of("REPORT_GENERATION_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
