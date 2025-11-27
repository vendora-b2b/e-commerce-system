package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Test controller to demonstrate different error handling patterns.
 * THIS IS FOR TESTING ONLY - REMOVE IN PRODUCTION
 */
@RestController
@RequestMapping("/api/v1/test-errors")
@Tag(name = "Error Testing", description = "Test different error types (REMOVE IN PRODUCTION)")
public class ErrorTestController {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestRequest {
        @NotEmpty(message = "Name is required and cannot be empty")
        private String name;
        
        private Integer age;
    }

    /**
     * Test validation errors (@Valid annotation)
     * POST /api/v1/test-errors/validation
     * Try with: { "name": "", "age": 25 }
     */
    @PostMapping("/validation")
    @Operation(summary = "Test validation errors")
    public ResponseEntity<String> testValidation(@Valid @RequestBody TestRequest request) {
        return ResponseEntity.ok("Validation passed: " + request.getName());
    }

    /**
     * Test IllegalStateException (domain business rules)
     * POST /api/v1/test-errors/business-rule
     */
    @PostMapping("/business-rule")
    @Operation(summary = "Test business rule violation")
    public ResponseEntity<String> testBusinessRule() {
        throw new IllegalStateException("Cannot process order: insufficient inventory");
    }

    /**
     * Test IllegalArgumentException (input validation)
     * POST /api/v1/test-errors/invalid-input
     */
    @PostMapping("/invalid-input")
    @Operation(summary = "Test invalid input error")
    public ResponseEntity<String> testInvalidInput() {
        throw new IllegalArgumentException("Quantity must be greater than zero");
    }

    /**
     * Test custom business exception
     * POST /api/v1/test-errors/custom-business
     */
    @PostMapping("/custom-business")
    @Operation(summary = "Test custom business exception")
    public ResponseEntity<String> testCustomBusiness() {
        throw new CustomBusinessException("CREDIT_LIMIT_EXCEEDED", "Customer has exceeded their credit limit of $5000");
    }

    /**
     * Test type mismatch (try passing string for integer)
     * GET /api/v1/test-errors/type-mismatch/{age}
     * Try with: /api/v1/test-errors/type-mismatch/abc
     */
    @GetMapping("/type-mismatch/{age}")
    @Operation(summary = "Test type mismatch error")
    public ResponseEntity<String> testTypeMismatch(@PathVariable Integer age) {
        return ResponseEntity.ok("Age is: " + age);
    }

    /**
     * Test generic exception (falls back to 500)
     * POST /api/v1/test-errors/generic
     */
    @PostMapping("/generic")
    @Operation(summary = "Test generic exception")
    public ResponseEntity<String> testGeneric() {
        throw new RuntimeException("This is an unexpected error");
    }
}