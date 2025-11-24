package com.example.ecommerce.marketplace.web.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for token refresh.
 * Returns a new access token when refresh is successful.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private Long expiresIn; // in milliseconds

    public static TokenRefreshResponse success(String accessToken, Long expiresIn) {
        TokenRefreshResponse response = new TokenRefreshResponse();
        response.setSuccess(true);
        response.setMessage("Token refreshed successfully");
        response.setAccessToken(accessToken);
        response.setExpiresIn(expiresIn);
        return response;
    }

    public static TokenRefreshResponse failure(String message) {
        TokenRefreshResponse response = new TokenRefreshResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
