package com.example.ecommerce.marketplace.web.model.user;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for supplier login.
 * Contains JWT tokens and supplier information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // in milliseconds
    private String username;
    private String role;
    private SupplierInfo supplierInfo;

    public static SupplierLoginResponse success(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String username,
            String role,
            Supplier supplier
    ) {
        SupplierLoginResponse response = new SupplierLoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUsername(username);
        response.setRole(role);
        response.setSupplierInfo(new SupplierInfo(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getBusinessLicense(),
                supplier.getRating(),
                supplier.getVerified()
        ));
        return response;
    }

    public static SupplierLoginResponse failure(String message) {
        SupplierLoginResponse response = new SupplierLoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
