package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User management controller.
 * Provides user registration, authentication check, and user-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration and management API")
public class UserController {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Check-in endpoint to test authentication.
     * Returns authenticated user information.
     * Requires authentication.
     * GET /api/v1/users/check-in
     */
    @GetMapping("/check-in")
    @Operation(summary = "Check-in authenticated user", description = "Test authentication and get current user info")
    public ResponseEntity<Map<String, Object>> checkIn(
        @AuthenticationPrincipal
        org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("authenticated", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Cast to CustomUserDetails to get additional user information
        CustomUserDetailsService.CustomUserDetails customUserDetails =
            (CustomUserDetailsService.CustomUserDetails) userDetails;

        User user = customUserDetails.getUser();

        response.put("authenticated", true);
        response.put("message", "Successfully authenticated");
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("entityId", user.getEntityId());
        response.put("enabled", user.getEnabled());
        response.put("accountLocked", user.getAccountLocked());
        response.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Register a new supplier with user account.
     * Creates both Supplier and User entities in a single transaction.
     * POST /api/v1/users/register/supplier
     */
    @PostMapping("/register/supplier")
    @Transactional
    @Operation(summary = "Register supplier with user account", description = "Create both supplier and user account")
    public ResponseEntity<UserRegistrationResponse> registerSupplier(
        @Valid @RequestBody RegisterSupplierWithUserRequest request
    ) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Username already taken"));
        }

        // Check supplier email uniqueness
        if (supplierRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Supplier with this email already exists"));
        }

        // Check business license uniqueness
        if (supplierRepository.existsByBusinessLicense(request.getBusinessLicense())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Business license already registered"));
        }

        // Create Supplier entity
        Supplier supplier = new Supplier(
            null, // ID will be generated
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getProfilePicture(),
            request.getProfileDescription(),
            request.getBusinessLicense(),
            null, // Initial rating is null
            false // Initial verified status is false
        );

        // Validate supplier
        if (!supplier.validateEmail()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid email format"));
        }

        if (!supplier.validateBusinessLicense()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid business license format"));
        }

        // Save supplier first
        Supplier savedSupplier = supplierRepository.save(supplier);

        // Create User entity linked to the supplier
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.SUPPLIER);
        user.setEntityId(savedSupplier.getId()); // Link to supplier
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        // Validate user
        if (!user.validate()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid user data"));
        }

        // Save user
        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserRegistrationResponse.success(savedUser, savedSupplier.getName()));
    }

    /**
     * Register a new retailer with user account.
     * Creates both Retailer and User entities in a single transaction.
     * POST /api/v1/users/register/retailer
     */
    @PostMapping("/register/retailer")
    @Transactional
    @Operation(summary = "Register retailer with user account", description = "Create both retailer and user account")
    public ResponseEntity<UserRegistrationResponse> registerRetailer(
        @Valid @RequestBody RegisterRetailerWithUserRequest request
    ) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Username already taken"));
        }

        // Check retailer email uniqueness
        if (retailerRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Retailer with this email already exists"));
        }

        // Check business license uniqueness
        if (retailerRepository.existsByBusinessLicense(request.getBusinessLicense())) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Business license already registered"));
        }

        // Create Retailer entity
        Retailer retailer = new Retailer();
        retailer.setName(request.getName());
        retailer.setEmail(request.getEmail());
        retailer.setPhone(request.getPhone());
        retailer.setAddress(request.getAddress());
        retailer.setProfilePicture(request.getProfilePicture());
        retailer.setProfileDescription(request.getProfileDescription());
        retailer.setBusinessLicense(request.getBusinessLicense());
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE); // Initial tier
        retailer.setCreditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : 0.0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.setLoyaltyPoints(0);

        // Validate retailer
        if (!retailer.validateEmail()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid email format"));
        }

        if (!retailer.validateBusinessLicense()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid business license format"));
        }

        // Save retailer first
        Retailer savedRetailer = retailerRepository.save(retailer);

        // Create User entity linked to the retailer
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.RETAILER);
        user.setEntityId(savedRetailer.getId()); // Link to retailer
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        // Validate user
        if (!user.validate()) {
            return ResponseEntity.badRequest()
                .body(UserRegistrationResponse.failure("Invalid user data"));
        }

        // Save user
        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserRegistrationResponse.success(savedUser, savedRetailer.getName()));
    }

    // ===== REQUEST/RESPONSE DTOs =====

    /**
     * Request DTO for supplier registration with user account.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterSupplierWithUserRequest {

        // User credentials
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        private String password;

        // Supplier information
        @NotBlank(message = "Supplier name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Business license is required")
        private String businessLicense;

        // Optional fields
        private String phone;
        private String address;
        private String profilePicture;
        private String profileDescription;
    }

    /**
     * Request DTO for retailer registration with user account.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRetailerWithUserRequest {

        // User credentials
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        private String password;

        // Retailer information
        @NotBlank(message = "Retailer name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Business license is required")
        private String businessLicense;

        // Optional fields
        private String phone;
        private String address;
        private String profilePicture;
        private String profileDescription;
        private Double creditLimit;
    }

    /**
     * Response DTO for user registration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegistrationResponse {
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
}
