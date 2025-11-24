package com.example.ecommerce.marketplace.web.model.user;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for retailer login.
 * Contains JWT tokens and retailer information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetailerLoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // in milliseconds
    private String username;
    private String role;
    private RetailerInfo retailerInfo;

    public static RetailerLoginResponse success(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String username,
            String role,
            Retailer retailer
    ) {
        RetailerLoginResponse response = new RetailerLoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUsername(username);
        response.setRole(role);
        response.setRetailerInfo(new RetailerInfo(
                retailer.getId(),
                retailer.getName(),
                retailer.getEmail(),
                retailer.getBusinessLicense(),
                retailer.getLoyaltyTier(),
                retailer.getCreditLimit(),
                retailer.getLoyaltyPoints(),
                retailer.getTotalPurchaseAmount()
        ));
        return response;
    }

    public static RetailerLoginResponse failure(String message) {
        RetailerLoginResponse response = new RetailerLoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
