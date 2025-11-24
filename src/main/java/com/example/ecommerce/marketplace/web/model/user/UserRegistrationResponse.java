package com.example.ecommerce.marketplace.web.model.user;

import com.example.ecommerce.marketplace.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for user registration.
 * Returns user account details after successful registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String role;
    private Long entityId;
    private String entityName; // Supplier or Retailer name

    public static UserRegistrationResponse success(User user, String entityName) {
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setSuccess(true);
        response.setMessage("Registration successful");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setEntityId(user.getEntityId());
        response.setEntityName(entityName);
        return response;
    }

    public static UserRegistrationResponse failure(String message) {
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
