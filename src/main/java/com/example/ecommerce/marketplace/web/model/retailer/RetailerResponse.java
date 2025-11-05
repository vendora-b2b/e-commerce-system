package com.example.ecommerce.marketplace.web.model.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for retailer information.
 * Represents a retailer entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetailerResponse {

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
     * Creates a RetailerResponse from a domain Retailer entity.
     */
    public static RetailerResponse fromDomain(Retailer retailer) {
        return new RetailerResponse(
            retailer.getId(),
            retailer.getName(),
            retailer.getEmail(),
            retailer.getPhone(),
            retailer.getAddress(),
            retailer.getProfilePicture(),
            retailer.getProfileDescription(),
            retailer.getBusinessLicense(),
            retailer.getLoyaltyTier(),
            retailer.getCreditLimit(),
            retailer.getTotalPurchaseAmount(),
            retailer.getLoyaltyPoints()
        );
    }
}
