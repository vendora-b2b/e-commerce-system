package com.example.ecommerce.marketplace.application.ai;

/**
 * Command object for ingesting a supplier into the AI vector database.
 * Contains supplier attributes for semantic search.
 */
public class IngestSupplierCommand {

    private final Long supplierId;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final String businessLicense;

    private IngestSupplierCommand(Builder builder) {
        this.supplierId = builder.supplierId;
        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.businessLicense = builder.businessLicense;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long supplierId;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String businessLicense;

        public Builder supplierId(Long supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder businessLicense(String businessLicense) {
            this.businessLicense = businessLicense;
            return this;
        }

        public IngestSupplierCommand build() {
            return new IngestSupplierCommand(this);
        }
    }
}
