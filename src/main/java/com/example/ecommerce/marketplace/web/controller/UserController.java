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
        try {
            // Check username uniqueness
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Username '" + request.getUsername() + "' is already taken. Please choose a different username."));
            }

            // Check supplier email uniqueness
            if (supplierRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Email '" + request.getEmail() + "' is already registered. Please use a different email address."));
            }

            // Check business license uniqueness
            if (supplierRepository.existsByBusinessLicense(request.getBusinessLicense())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Business license '" + request.getBusinessLicense() + "' is already registered. Each business license can only be used once."));
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
                    .body(UserRegistrationResponse.failure("Invalid email format '" + request.getEmail() + "'. Please provide a valid email address (e.g., example@domain.com)."));
            }

            if (!supplier.validateBusinessLicense()) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Invalid business license format '" + request.getBusinessLicense() + "'. Business license must be alphanumeric and between 5-50 characters."));
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
                    .body(UserRegistrationResponse.failure("Username '" + request.getUsername() + "' is invalid. Username must be 3-50 characters and contain only letters, numbers, dots, underscores, or hyphens."));
            }

            // Save user
            User savedUser = userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserRegistrationResponse.success(savedUser, savedSupplier.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UserRegistrationResponse.failure("Registration failed due to a system error. Please try again later or contact support if the problem persists."));
        }
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
        try {
            // Check username uniqueness
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Username '" + request.getUsername() + "' is already taken. Please choose a different username."));
            }

            // Check retailer email uniqueness
            if (retailerRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Email '" + request.getEmail() + "' is already registered. Please use a different email address."));
            }

            // Check business license uniqueness
            if (retailerRepository.existsByBusinessLicense(request.getBusinessLicense())) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Business license '" + request.getBusinessLicense() + "' is already registered. Each business license can only be used once."));
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
                    .body(UserRegistrationResponse.failure("Invalid email format '" + request.getEmail() + "'. Please provide a valid email address (e.g., example@domain.com)."));
            }

            if (!retailer.validateBusinessLicense()) {
                return ResponseEntity.badRequest()
                    .body(UserRegistrationResponse.failure("Invalid business license format '" + request.getBusinessLicense() + "'. Business license must be alphanumeric and between 5-50 characters."));
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
                    .body(UserRegistrationResponse.failure("Username '" + request.getUsername() + "' is invalid. Username must be 3-50 characters and contain only letters, numbers, dots, underscores, or hyphens."));
            }

            // Save user
            User savedUser = userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserRegistrationResponse.success(savedUser, savedRetailer.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UserRegistrationResponse.failure("Registration failed due to a system error. Please try again later or contact support if the problem persists."));
        }
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
                        .body(TokenRefreshResponse.failure("Your refresh token is invalid or has expired. Please log in again to continue."));
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Fetch user from database
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshResponse.failure("User account not found. The account may have been deleted. Please register or contact support."));
            }

            // Check if user account is enabled and not locked
            if (user.getEnabled() == null || !user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TokenRefreshResponse.failure("Your account has been disabled. Please contact support for assistance."));
            }

            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TokenRefreshResponse.failure("Your account has been locked due to security reasons. Please contact support to unlock your account."));
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
                    .body(TokenRefreshResponse.failure("Token refresh failed due to a system error. Please try logging in again."));
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
                        .body(SupplierLoginResponse.failure("This account is not registered as a supplier. Please use the retailer login or register as a supplier."));
            }

            // Check if account is enabled
            if (user.getEnabled() == null || !user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(SupplierLoginResponse.failure("Your account has been disabled. Please contact support for assistance."));
            }

            // Check if account is locked
            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(SupplierLoginResponse.failure("Your account has been locked due to multiple failed login attempts. Please contact support to unlock your account."));
            }

            // Get supplier information
            Supplier supplier = supplierRepository.findById(user.getEntityId()).orElse(null);
            if (supplier == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(SupplierLoginResponse.failure("Supplier profile not found. Please contact support to resolve this issue."));
            }

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
            // Handle failed login attempt
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                user.recordFailedLogin(5); // Lock account after 5 failed attempts
                userRepository.save(user);
            });
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SupplierLoginResponse.failure("Invalid username or password. Please check your credentials and try again."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SupplierLoginResponse.failure("Login failed due to a system error. Please try again later or contact support if the problem persists."));
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
                        .body(RetailerLoginResponse.failure("This account is not registered as a retailer. Please use the supplier login or register as a retailer."));
            }

            // Check if account is enabled
            if (user.getEnabled() == null || !user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(RetailerLoginResponse.failure("Your account has been disabled. Please contact support for assistance."));
            }

            // Check if account is locked
            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(RetailerLoginResponse.failure("Your account has been locked due to multiple failed login attempts. Please contact support to unlock your account."));
            }

            // Get retailer information
            Retailer retailer = retailerRepository.findById(user.getEntityId()).orElse(null);
            if (retailer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(RetailerLoginResponse.failure("Retailer profile not found. Please contact support to resolve this issue."));
            }

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
            // Handle failed login attempt
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                user.recordFailedLogin(5); // Lock account after 5 failed attempts
                userRepository.save(user);
            });
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RetailerLoginResponse.failure("Invalid username or password. Please check your credentials and try again."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RetailerLoginResponse.failure("Login failed due to a system error. Please try again later or contact support if the problem persists."));
        }
    }
}
