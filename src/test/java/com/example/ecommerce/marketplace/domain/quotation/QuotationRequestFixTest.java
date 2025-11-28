package com.example.ecommerce.marketplace.domain.quotation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Test class to verify the fixes for quotation request issues.
 * Tests for validUntil null handling and proper cancel logic.
 */
public class QuotationRequestFixTest {

    @Test
    void shouldSetDefaultValidUntilWhenNull() {
        // given - create a request with null validUntil
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, 100.0, "Test specs")
                .validUntil(null) // Explicitly set to null
                .build();
        
        // when - submit the request (this should set default validUntil)
        request.submit();
        
        // then - validUntil should be set to default value
        assertNotNull(request.getValidUntil());
        assertTrue(request.getValidUntil().isAfter(LocalDateTime.now()));
        assertTrue(request.getValidUntil().isBefore(LocalDateTime.now().plusDays(31)));
    }

    @Test 
    void shouldNotAllowCancelAfterOffersAreSent() {
        // given - a request that has offers sent
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, 100.0, "Test specs")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();
        
        // Submit and mark as received
        request.submit();
        request.markRequestReceived();
        
        // when/then - trying to cancel after offers are sent should fail
        request.markOffersSent(); // This automatically expires the request
        
        // The request should now be expired, so cancel should fail
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            request.cancel();
        });
        
        assertTrue(exception.getMessage().contains("Cannot cancel expired request"));
    }
    
    @Test
    void shouldAllowCancelBeforeOffersAreSent() {
        // given - a request that is still pending or received
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, 100.0, "Test specs")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();
        
        request.submit(); // Status: PENDING
        
        // when - cancel before offers are sent
        assertDoesNotThrow(() -> {
            request.cancel();
        });
        
        // then - status should be CANCELLED
        assertEquals(QuotationRequestStatus.CANCELLED, request.getStatus());
    }
    
    @Test
    void shouldAllowCancelWhenRequestReceived() {
        // given - a request that is received but no offers sent yet
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, 100.0, "Test specs")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();
        
        request.submit(); // Status: PENDING
        request.markRequestReceived(); // Status: REQUEST_RECEIVED
        
        // when - cancel while request is received (before offers sent)
        assertDoesNotThrow(() -> {
            request.cancel();
        });
        
        // then - status should be CANCELLED
        assertEquals(QuotationRequestStatus.CANCELLED, request.getStatus());
    }
    
    @Test
    void shouldAutomaticallyExpireAfterOffersAreSent() {
        // given - a request that has received status
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, 100.0, "Test specs")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();
        
        request.submit();
        request.markRequestReceived();
        
        // when - mark offers as sent
        request.markOffersSent();
        
        // then - request should be automatically expired
        assertEquals(QuotationRequestStatus.EXPIRED, request.getStatus());
    }
}