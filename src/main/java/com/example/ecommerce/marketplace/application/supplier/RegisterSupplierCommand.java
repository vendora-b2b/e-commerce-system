package com.example.ecommerce.marketplace.application.supplier;

/**
 * Command object for registering a new supplier.
 * Contains all necessary data for supplier registration.
 */
public class RegisterSupplierCommand {

    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final String profilePicture;
    private final String profileDescription;
    private final String businessLicense;

    public RegisterSupplierCommand(String name, String email, String phone, String address,
                                   String profilePicture, String profileDescription, String businessLicense) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profilePicture = profilePicture;
        this.profileDescription = profileDescription;
        this.businessLicense = businessLicense;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }
}
