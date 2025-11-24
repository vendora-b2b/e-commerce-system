package com.example.ecommerce.marketplace.service.auth;

import com.example.ecommerce.marketplace.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation, validation, and claims extraction.
 * Handles both access tokens and refresh tokens with configurable expiration times.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    /**
     * Generates an access token for the authenticated user.
     * Includes custom claims: role and entityId.
     *
     * @param username the username
     * @param role the user's role
     * @param entityId the associated entity ID (Supplier or Retailer ID)
     * @return JWT access token
     */
    public String generateAccessToken(String username, UserRole role, Long entityId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        claims.put("entityId", entityId);
        claims.put("type", "access");
        return generateToken(claims, username, accessTokenExpiration);
    }

    /**
     * Generates a refresh token for the authenticated user.
     * Refresh tokens have longer expiration and fewer claims.
     *
     * @param username the username
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, username, refreshTokenExpiration);
    }

    /**
     * Generates a JWT token with the specified claims and expiration.
     *
     * @param extraClaims additional claims to include
     * @param username the subject (username)
     * @param expiration expiration time in milliseconds
     * @return JWT token
     */
    private String generateToken(Map<String, Object> extraClaims, String username, Long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username (subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user role from a JWT token.
     *
     * @param token the JWT token
     * @return the user role
     */
    public UserRole extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return role != null ? UserRole.valueOf(role) : null;
    }

    /**
     * Extracts the entity ID from a JWT token.
     *
     * @param token the JWT token
     * @return the entity ID
     */
    public Long extractEntityId(String token) {
        return extractClaim(token, claims -> claims.get("entityId", Long.class));
    }

    /**
     * Extracts the token type from a JWT token.
     *
     * @param token the JWT token
     * @return the token type (access or refresh)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates a JWT token against the provided UserDetails.
     * Checks if the token is expired and if the username matches.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Gets the signing key for JWT token generation and validation.
     * Uses HMAC-SHA with the configured secret.
     *
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates a JWT token and returns detailed error information.
     * Used for debugging and error reporting.
     *
     * @param token the JWT token
     * @return validation result message
     */
    public String validateTokenWithMessage(String token) {
        try {
            extractAllClaims(token);
            if (isTokenExpired(token)) {
                return "Token has expired";
            }
            return "Token is valid";
        } catch (ExpiredJwtException e) {
            return "Token has expired";
        } catch (MalformedJwtException e) {
            return "Invalid token format";
        } catch (SignatureException e) {
            return "Invalid token signature";
        } catch (UnsupportedJwtException e) {
            return "Unsupported token";
        } catch (IllegalArgumentException e) {
            return "Token is empty or null";
        } catch (Exception e) {
            return "Token validation failed: " + e.getMessage();
        }
    }

    /**
     * Checks if a token is an access token.
     *
     * @param token the JWT token
     * @return true if access token, false otherwise
     */
    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    /**
     * Checks if a token is a refresh token.
     *
     * @param token the JWT token
     * @return true if refresh token, false otherwise
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    /**
     * Gets the access token expiration time in milliseconds.
     *
     * @return access token expiration time
     */
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Gets the refresh token expiration time in milliseconds.
     *
     * @return refresh token expiration time
     */
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Validates a refresh token.
     * Checks if the token is valid, not expired, and is actually a refresh token.
     *
     * @param token the refresh token to validate
     * @return true if valid refresh token, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        try {
            // Extract all claims to ensure token is parseable
            extractAllClaims(token);

            // Check if token is expired
            if (isTokenExpired(token)) {
                return false;
            }

            // Verify it's actually a refresh token
            if (!isRefreshToken(token)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
