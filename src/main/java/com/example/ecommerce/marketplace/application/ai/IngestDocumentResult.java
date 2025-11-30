package com.example.ecommerce.marketplace.application.ai;

/**
 * Result object returned after document ingestion.
 */
public class IngestDocumentResult {

    private final boolean success;
    private final String documentId;
    private final String documentType;
    private final String message;
    private final String errorCode;

    private IngestDocumentResult(boolean success, String documentId, String documentType,
                                  String message, String errorCode) {
        this.success = success;
        this.documentId = documentId;
        this.documentType = documentType;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static IngestDocumentResult success(String documentId, String documentType) {
        return new IngestDocumentResult(
            true, documentId, documentType, "Document ingested successfully", null
        );
    }

    public static IngestDocumentResult failure(String message, String errorCode) {
        return new IngestDocumentResult(false, null, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
