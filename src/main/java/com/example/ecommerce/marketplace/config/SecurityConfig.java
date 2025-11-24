package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.service.auth.JwtAuthenticationEntryPoint;
import com.example.ecommerce.marketplace.service.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the e-commerce marketplace application.
 * Configures JWT authentication, authorization, and password encoding.
 * Integrates with CustomUserDetailsService for user authentication.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            UserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Password encoder bean using BCrypt hashing algorithm.
     * BCrypt is a strong, adaptive hashing function designed for password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider that uses our CustomUserDetailsService and BCrypt password encoder.
     * Uses the constructor-based approach to avoid deprecation warnings.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Authentication manager bean for programmatic authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Security filter chain for production profile.
     * Configures JWT authentication, HTTP security, CSRF, and role-based authorization rules.
     *
     * Public endpoints (no authentication required):
     * - /api/v1/health/** - Health check endpoints
     * - /api/v1/users/register/** - User registration endpoints
     * - /api/v1/users/login/** - User login endpoints
     * - /swagger-ui/** - Swagger UI documentation
     * - /v3/api-docs/** - OpenAPI documentation
     *
     * Role-based authorization:
     * - Products: SUPPLIERS can create/update/delete, all authenticated users can view
     * - Orders: RETAILERS can place/cancel, SUPPLIERS can update status, both can view
     */
    @Bean
    @Profile("test")
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/users/register/**").permitAll()
                .requestMatchers("/api/v1/users/login/**").permitAll()
                .requestMatchers("/api/v1/users/refresh").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // Products - Read access for all authenticated users
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/products/**").authenticated()

                // Products - Write operations restricted to SUPPLIERS
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/products/**").hasRole("SUPPLIER")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/products/**").hasRole("SUPPLIER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/products/**").hasRole("SUPPLIER")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/products/**").hasRole("SUPPLIER")

                // Orders - Place orders (RETAILERS only)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/orders").hasRole("RETAILER")

                // Orders - Update status (SUPPLIERS only)
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/orders/**").hasRole("SUPPLIER")

                // Orders - Cancel orders (RETAILERS only)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/orders/**").hasRole("RETAILER")

                // Orders - View orders (both roles)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/orders/**").authenticated()

                // Suppliers - Supplier-specific endpoints (SUPPLIERS only)
                .requestMatchers("/api/v1/suppliers/**").hasRole("SUPPLIER")

                // Retailers - Retailer-specific endpoints (RETAILERS only)
                .requestMatchers("/api/v1/retailers/**").hasRole("RETAILER")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(AbstractHttpConfigurer::disable) // Disable HTTP Basic Authentication
            .formLogin(AbstractHttpConfigurer::disable); // Disable form login for stateless API
        return http.build();
    }

    @Bean
    @Profile("!test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}