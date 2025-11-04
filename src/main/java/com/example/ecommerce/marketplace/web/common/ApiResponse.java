package com.example.ecommerce.marketplace.web.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic API response wrapper for consistent response format.
 * Used for simple success/failure responses without complex data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private String errorCode;

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }

    public static ApiResponse failure(String message, String errorCode) {
        return new ApiResponse(false, message, errorCode);
    }
}
