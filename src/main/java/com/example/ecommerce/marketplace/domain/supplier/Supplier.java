package com.example.ecommerce.marketplace.domain.supplier;

import java.util.regex.Pattern;

/**
 * Represents a supplier entity in the e-commerce marketplace.
 * Suppliers provide products and respond to quotation requests from retailers.
 */
public class Supplier {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Double MIN_RATING = 0.0;
    private static final Double MAX_RATING = 5.0;

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
    private String profileDescription;
    private String businessLicense;
    private Double rating;
    private Boolean verified;

    public Supplier() {
    }

    public Supplier(Long id, String name, String email, String phone, String address,
                    String profilePicture, String profileDescription, String businessLicense,
                    Double rating, Boolean verified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profilePicture = profilePicture;
        this.profileDescription = profileDescription;
        this.businessLicense = businessLicense;
        this.rating = rating;
        this.verified = verified;
    }

    /**
     * Validates the email address format and deliverability.
     * @return true if email is valid, false otherwise
     */
    public boolean validateEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates the business license against regulatory requirements.
     * @return true if business license is valid, false otherwise
     */
    public boolean validateBusinessLicense() {
        if (businessLicense == null || businessLicense.trim().isEmpty()) {
            return false;
        }
        // Business license should be at least 5 characters and alphanumeric
        return businessLicense.trim().length() >= 5 &&
               businessLicense.trim().matches("^[A-Za-z0-9-]+$");
    }

    /**
     * Updates the supplier's rating based on new feedback or transaction.
     * @param newRating the rating to incorporate
     */
    public void updateRating(Double newRating) {
        if (newRating == null) {
            throw new IllegalArgumentException("New rating cannot be null");
        }
        if (newRating < MIN_RATING || newRating > MAX_RATING) {
            throw new IllegalArgumentException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
        }

        // If no existing rating, set the new rating
        if (this.rating == null) {
            this.rating = newRating;
        } else {
            // Calculate running average (simplified - in real system would track count)
            this.rating = (this.rating + newRating) / 2.0;
        }

        // Round to 2 decimal places
        this.rating = Math.round(this.rating * 100.0) / 100.0;
    }

    /**
     * Checks if the supplier is verified.
     * @return true if verified, false otherwise
     */
    public boolean isVerified() {
        return verified != null && verified;
    }

    /**
     * Updates the supplier's profile information.
     * @param name business name
     * @param phone contact phone
     * @param address business address
     * @param profileDescription business description
     */
    public void updateProfile(String name, String phone, String address, String profileDescription) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone.trim();
        }
        if (address != null && !address.trim().isEmpty()) {
            this.address = address.trim();
        }
        if (profileDescription != null) {
            this.profileDescription = profileDescription.trim();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
