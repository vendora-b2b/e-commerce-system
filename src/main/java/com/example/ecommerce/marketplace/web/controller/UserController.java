package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import com.example.ecommerce.marketplace.service.auth.CustomUserDetailsService;
import com.example.ecommerce.marketplace.service.auth.JwtService;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.web.model.user.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * User authentication controller.
 * Provides user registration and login endpoints for suppliers and retailers.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "User registration and login API")
public class UserController {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // ===== REGISTRATION ENDPOINTS =====

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

    // ===== LOGIN ENDPOINTS =====

    /**
     * Refresh access token using a valid refresh token.
     * This endpoint allows clients to obtain a new access token without re-authentication.
     * POST /api/v1/users/refresh
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get a new access token using a valid refresh token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (!jwtService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshResponse.failure("Invalid or expired refresh token"));
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Fetch user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user account is enabled and not locked
            if (user.getEnabled() == null || !user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TokenRefreshResponse.failure("User account is disabled"));
            }

            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TokenRefreshResponse.failure("User account is locked"));
            }

            // Generate new access token with user's current role and entityId
            String newAccessToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    user.getRole(),
                    user.getEntityId()
            );

            return ResponseEntity.ok(TokenRefreshResponse.success(
                    newAccessToken,
                    jwtService.getAccessTokenExpiration()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenRefreshResponse.failure("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Login endpoint for suppliers.
     * Authenticates supplier credentials and returns JWT tokens with supplier information.
     * POST /api/v1/users/login/supplier
     */
    @PostMapping("/login/supplier")
    @Operation(summary = "Supplier login", description = "Authenticate supplier and get JWT tokens")
    public ResponseEntity<SupplierLoginResponse> loginSupplier(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Get authenticated user details
            CustomUserDetailsService.CustomUserDetails userDetails =
                    (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Verify user is a supplier
            if (!user.getRole().equals(UserRole.SUPPLIER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(SupplierLoginResponse.failure("Invalid credentials or user is not a supplier"));
            }

            // Get supplier information
            Supplier supplier = supplierRepository.findById(user.getEntityId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found for user"));

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    user.getRole(),
                    user.getEntityId()
            );
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            // Update last login time and reset failed attempts
            user.recordSuccessfulLogin();
            userRepository.save(user);

            // Build response with supplier information
            return ResponseEntity.ok(SupplierLoginResponse.success(
                    accessToken,
                    refreshToken,
                    jwtService.getAccessTokenExpiration(),
                    user.getUsername(),
                    user.getRole().name(),
                    supplier
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SupplierLoginResponse.failure("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SupplierLoginResponse.failure("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Login endpoint for retailers.
     * Authenticates retailer credentials and returns JWT tokens with retailer information.
     * POST /api/v1/users/login/retailer
     */
    @PostMapping("/login/retailer")
    @Operation(summary = "Retailer login", description = "Authenticate retailer and get JWT tokens")
    public ResponseEntity<RetailerLoginResponse> loginRetailer(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Get authenticated user details
            CustomUserDetailsService.CustomUserDetails userDetails =
                    (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Verify user is a retailer
            if (!user.getRole().equals(UserRole.RETAILER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(RetailerLoginResponse.failure("Invalid credentials or user is not a retailer"));
            }

            // Get retailer information
            Retailer retailer = retailerRepository.findById(user.getEntityId())
                    .orElseThrow(() -> new RuntimeException("Retailer not found for user"));

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    user.getRole(),
                    user.getEntityId()
            );
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            // Update last login time and reset failed attempts
            user.recordSuccessfulLogin();
            userRepository.save(user);

            // Build response with retailer information
            return ResponseEntity.ok(RetailerLoginResponse.success(
                    accessToken,
                    refreshToken,
                    jwtService.getAccessTokenExpiration(),
                    user.getUsername(),
                    user.getRole().name(),
                    retailer
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RetailerLoginResponse.failure("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RetailerLoginResponse.failure("Login failed: " + e.getMessage()));
        }
    }
}
