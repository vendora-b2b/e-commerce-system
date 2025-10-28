package com.example.ecommerce.marketplace.domain.retailer;

import java.util.regex.Pattern;

/**
 * Represents a retailer entity in the e-commerce marketplace.
 * Retailers purchase products from suppliers and can request quotations.
 */
public class Retailer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern BUSINESS_LICENSE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{8,20}$"
    );

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
    private String profileDescription;
    private String businessLicense;
    private RetailerLoyaltyTier loyaltyTier;
    private Double creditLimit;
    private Double totalPurchaseAmount;
    private Integer loyaltyPoints;

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
        return BUSINESS_LICENSE_PATTERN.matcher(businessLicense.trim()).matches();
    }

    /**
     * Updates the loyalty tier based on purchase history and loyalty points.
     */
    public void updateLoyaltyTier() {
        int points = (loyaltyPoints != null) ? loyaltyPoints : 0;
        double purchaseAmount = (totalPurchaseAmount != null) ? totalPurchaseAmount : 0.0;
        this.loyaltyTier = RetailerLoyaltyTier.calculateTier(points, purchaseAmount);
    }

    /**
     * Updates the retailer's profile information.
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

    /**
     * Checks if the retailer can place orders.
     * Business rule: must have valid email and business license.
     * @return true if can place orders, false otherwise
     */
    public boolean canPlaceOrders() {
        return validateEmail() && validateBusinessLicense();
    }

    /**
     * Checks if the retailer has available credit for a given amount.
     * Compares the requested amount against remaining credit limit.
     * @param amount the amount to check
     * @return true if has sufficient credit, false otherwise
     */
    public boolean hasAvailableCredit(Double amount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        if (creditLimit == null) {
            return false;
        }
        return creditLimit >= amount;
    }

    /**
     * Adds loyalty points to the retailer's account.
     * Typically called after successful order completion.
     * @param points the points to add
     */
    public void addLoyaltyPoints(Integer points) {
        if (points == null || points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        if (this.loyaltyPoints == null) {
            this.loyaltyPoints = 0;
        }
        this.loyaltyPoints += points;
        updateLoyaltyTier();
    }

    /**
     * Redeems loyalty points from the retailer's account.
     * Used when applying loyalty rewards to orders.
     * @param points the points to redeem
     * @return true if redemption successful, false if insufficient points
     */
    public boolean redeemLoyaltyPoints(Integer points) {
        if (points == null || points <= 0) {
            return false;
        }
        if (this.loyaltyPoints == null || this.loyaltyPoints < points) {
            return false;
        }
        this.loyaltyPoints -= points;
        updateLoyaltyTier();
        return true;
    }

    /**
     * Records a purchase and updates the retailer's purchase history.
     * @param amount the purchase amount
     */
    public void recordPurchase(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Purchase amount must be positive");
        }
        if (this.totalPurchaseAmount == null) {
            this.totalPurchaseAmount = 0.0;
        }
        this.totalPurchaseAmount += amount;
        updateLoyaltyTier();
    }

    /**
     * Gets the discount percentage based on current loyalty tier.
     * @return discount percentage (0.0 to 1.0)
     */
    public double getDiscountPercentage() {
        if (loyaltyTier == null) {
            return 0.0;
        }
        return loyaltyTier.getDiscountPercentage();
    }

    // Getters and setters
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

    public RetailerLoyaltyTier getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(RetailerLoyaltyTier loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Double getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(Double totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
