package com.example.ecommerce.marketplace.application.retailer;

/**
 * Command object for updating retailer profile information.
 */
public class UpdateRetailerProfileCommand {

    private final Long retailerId;
    private final String name;
    private final String phone;
    private final String address;
    private final String profileDescription;

    public UpdateRetailerProfileCommand(Long retailerId, String name, String phone,
                                       String address, String profileDescription) {
        this.retailerId = retailerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.profileDescription = profileDescription;
    }

    public Long getRetailerId() {
        return retailerId;
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
