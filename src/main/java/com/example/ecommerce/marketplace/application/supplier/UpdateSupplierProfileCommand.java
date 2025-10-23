package com.example.ecommerce.marketplace.application.supplier;

/**
 * Command object for updating supplier profile information.
 */
public class UpdateSupplierProfileCommand {

    private final Long supplierId;
    private final String name;
    private final String phone;
    private final String address;
    private final String profileDescription;

    public UpdateSupplierProfileCommand(Long supplierId, String name, String phone,
                                       String address, String profileDescription) {
        this.supplierId = supplierId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.profileDescription = profileDescription;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getProfileDescription() {
        return profileDescription;
    }
}
