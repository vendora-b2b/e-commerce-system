package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller.
 * Provides system health status and diagnostics endpoints.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "System health check and diagnostics API")
public class HealthController {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;

    /**
     * Health check endpoint.
     * GET /api/v1/health
     */
    @GetMapping("")
    @Operation(summary = "Health check", description = "Check if the application is running")
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(LocalDateTime.now());
        response.setService("E-Commerce Marketplace");
        response.setVersion("1.0.0");
        
        // Check database connectivity
        try {
            long userCount = userRepository.count();
            long supplierCount = supplierRepository.count();
            long retailerCount = retailerRepository.count();
            
            Map<String, Object> details = new HashMap<>();
            details.put("database", "connected");
            details.put("userCount", userCount);
            details.put("supplierCount", supplierCount);
            details.put("retailerCount", retailerCount);
            response.setDetails(details);
            
        } catch (Exception e) {
            response.setStatus("DOWN");
            Map<String, Object> details = new HashMap<>();
            details.put("database", "disconnected");
            details.put("error", e.getMessage());
            response.setDetails(details);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simple ping endpoint.
     * GET /api/v1/health/ping
     */
    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Simple ping endpoint")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for health check.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthResponse {
        private String status;
        private LocalDateTime timestamp;
        private String service;
        private String version;
        private Map<String, Object> details;
    }
}
