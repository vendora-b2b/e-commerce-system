package com.example.ecommerce.marketplace.web.model.user;

import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Retailer information included in login response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetailerInfo {
    private Long id;
    private String name;
    private String email;
    private String businessLicense;
    private RetailerLoyaltyTier loyaltyTier;
    private Double creditLimit;
    private Integer loyaltyPoints;
    private Double totalPurchaseAmount;
}
