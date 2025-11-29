package com.example.ecommerce.marketplace.application.ai;

/**
 * Command object for ingesting a knowledge document into the AI vector database.
 */
public class IngestDocumentCommand {

    private final String documentId;
    private final String title;
    private final String content;
    private final String documentType;  // "tax", "contract", "policy", "faq", etc.
    private final String category;
    private final String source;

    private IngestDocumentCommand(Builder builder) {
        this.documentId = builder.documentId;
        this.title = builder.title;
        this.content = builder.content;
        this.documentType = builder.documentType;
        this.category = builder.category;
        this.source = builder.source;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getCategory() {
        return category;
    }

    public String getSource() {
        return source;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String documentId;
        private String title;
        private String content;
        private String documentType;
        private String category;
        private String source;

        public Builder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public IngestDocumentCommand build() {
            return new IngestDocumentCommand(this);
        }
    }
}
