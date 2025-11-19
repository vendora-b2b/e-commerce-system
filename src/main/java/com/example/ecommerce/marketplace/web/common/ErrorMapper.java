package com.example.ecommerce.marketplace.web.common;

import org.springframework.http.HttpStatus;

/**
 * Utility class to map application error codes to HTTP status codes.
 * Centralizes error code mapping logic for consistency across controllers.
 */
public class ErrorMapper {

    /**
     * Maps an application error code to an appropriate HTTP status code.
     *
     * @param errorCode the application error code
     * @return the corresponding HTTP status code
     */
    public static HttpStatus toHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (errorCode) {
            // Not Found errors
            case "SUPPLIER_NOT_FOUND", "RETAILER_NOT_FOUND", "PRODUCT_NOT_FOUND",
                 "REQUEST_NOT_FOUND" ->
                HttpStatus.NOT_FOUND;

            // Conflict errors (duplicate/uniqueness violations)
            case "EMAIL_EXISTS", "LICENSE_EXISTS", "ORDER_NUMBER_EXISTS", 
                 "SKU_EXISTS", "DUPLICATE_SKU", "DUPLICATE_VARIANT",
                 "PRICE_TIER_OVERLAP" ->
                HttpStatus.CONFLICT;

            // Bad Request errors (validation failures)
            case "INVALID_NAME", "INVALID_EMAIL", "INVALID_LICENSE",
                 "INVALID_EMAIL_FORMAT", "INVALID_LICENSE_FORMAT",
                 "INVALID_SUPPLIER_ID", "INVALID_RETAILER_ID",
                 "INVALID_ORDER_NUMBER", "INVALID_QUANTITY",
                 "INVALID_PRICE", "INVALID_PRODUCT_ID",
                 "INVALID_ORDER_NUMBER_FORMAT", "INVALID_SHIPPING_ADDRESS",
                 "EMPTY_ORDER_ITEMS", "INVALID_ORDER_ITEM",
                 "MINIMUM_QUANTITY_NOT_MET",
                 // Quotation errors
                 "INVALID_REQUEST_ID", "INVALID_ITEMS",
                 // Product errors
                 "INVALID_SKU", "INVALID_SKU_FORMAT", "INVALID_NAME_FORMAT",
                 "INVALID_BASE_PRICE", "INVALID_MOQ", "INVALID_MOQ_VALUE",
                 "VALIDATION_ERROR", "PRODUCT_HAS_PENDING_ORDERS",
                 // Variant errors
                 "INVALID_COLOR", "INVALID_SIZE", "INVALID_VARIANT_ID",
                 "VARIANT_PRODUCT_MISMATCH", "VARIANT_NOT_FOUND",
                 "LAST_VARIANT_CANNOT_BE_DELETED", "VARIANT_HAS_PENDING_ORDERS",
                 // Price tier errors
                 "INVALID_MIN_QUANTITY", "INVALID_MAX_QUANTITY", "INVALID_DISCOUNT_PERCENT" ->
                HttpStatus.BAD_REQUEST;

            // Default to Bad Request for unknown errors
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
