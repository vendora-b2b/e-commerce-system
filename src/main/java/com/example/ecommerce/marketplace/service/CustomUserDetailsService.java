package com.example.ecommerce.marketplace.service;

import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user-specific data from the User domain repository and adapts it
 * to Spring Security's UserDetails interface for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username for Spring Security authentication.
     * 
     * @param username the username identifying the user whose data is required
     * @return a fully populated UserDetails object (never null)
     * @throws UsernameNotFoundException if the user could not be found or has no GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user from repository
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username: " + username
            ));

        // Check if user account is enabled and not locked
        if (!user.canAuthenticate()) {
            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                throw new UsernameNotFoundException(
                    "Account is locked due to multiple failed login attempts"
                );
            }
            if (user.getEnabled() == null || !user.getEnabled()) {
                throw new UsernameNotFoundException(
                    "Account is disabled"
                );
            }
        }

        // Convert domain User to Spring Security UserDetails
        return new CustomUserDetails(user);
    }

    /**
     * Custom UserDetails implementation that wraps our domain User entity.
     * This adapter allows Spring Security to work with our domain model.
     */
    public static class CustomUserDetails implements UserDetails {

        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        /**
         * Returns the authorities granted to the user based on their role.
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convert UserRole to Spring Security GrantedAuthority
            // Note: Spring Security requires "ROLE_" prefix for role-based authorization
            String role = "ROLE_" + user.getRole().name();
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            // Account expiration not implemented in domain model
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getAccountLocked() == null || !user.getAccountLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // Credential expiration not implemented in domain model
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getEnabled() != null && user.getEnabled();
        }

        /**
         * Returns the wrapped domain User entity.
         * This allows accessing domain-specific properties beyond UserDetails interface.
         * 
         * @return the domain User entity
         */
        public User getUser() {
            return user;
        }

        /**
         * Convenience method to get the user's role.
         */
        public String getRole() {
            return user.getRole().name();
        }

        /**
         * Convenience method to get the entity ID (Supplier or Retailer ID).
         */
        public Long getEntityId() {
            return user.getEntityId();
        }

        /**
         * Convenience method to check if user is a supplier.
         */
        public boolean isSupplier() {
            return user.isSupplier();
        }

        /**
         * Convenience method to check if user is a retailer.
         */
        public boolean isRetailer() {
            return user.isRetailer();
        }
    }
}
