package com.example.ecommerce.marketplace.application.report;

/**
 * Result of report generation operation.
 */
public class GenerateReportResult {
    private final boolean success;
    private final String errorCode;
    private final String message;
    private final byte[] fileData;

    private GenerateReportResult(boolean success, String errorCode, String message, byte[] fileData) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
        this.fileData = fileData;
    }

    public static GenerateReportResult success(byte[] fileData) {
        return new GenerateReportResult(true, null, "Report generated successfully", fileData);
    }

    public static GenerateReportResult failure(String errorCode, String message) {
        return new GenerateReportResult(false, errorCode, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
