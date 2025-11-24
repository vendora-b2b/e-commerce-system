package com.example.ecommerce.marketplace.service.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts HTTP requests to validate JWT tokens.
 * Extends OncePerRequestFilter to ensure single execution per request.
 * Extracts JWT from Authorization header, validates it, and sets authentication in SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip filter for public endpoints
        String requestPath = request.getServletPath();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // Extract username from token
            final String username = jwtService.extractUsername(jwt);

            // If username is present and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Check if it's an access token (not refresh token)
                    if (!jwtService.isAccessToken(jwt)) {
                        // Reject refresh tokens for regular authentication
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the exception (in production, use proper logging)
            logger.error("JWT authentication failed: " + e.getMessage(), e);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the request path is a public endpoint that should skip JWT validation.
     * Public endpoints include registration, login, refresh token, health checks, and API documentation.
     *
     * @param requestPath the request path
     * @return true if public endpoint, false otherwise
     */
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.startsWith("/api/v1/users/register/") ||
               requestPath.startsWith("/api/v1/users/login/") ||
               requestPath.equals("/api/v1/users/refresh") ||
               requestPath.startsWith("/api/v1/health/") ||
               requestPath.startsWith("/swagger-ui") ||
               requestPath.startsWith("/v3/api-docs") ||
               requestPath.equals("/swagger-ui.html");
    }
}
