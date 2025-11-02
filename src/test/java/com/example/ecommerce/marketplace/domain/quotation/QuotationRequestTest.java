package com.example.ecommerce.marketplace.domain.quotation;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class QuotationRequestTest {

    @Test
    void shouldCreateValidQuotationRequest() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        
        // when
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, "Specific requirements")
                .validUntil(validUntil)
                .notes("Urgent request")
                .build();

        // then
        assertNotNull(request);
        assertEquals("QR-12345", request.getRequestNumber());
        assertEquals(1L, request.getRetailerId());
        assertEquals(2L, request.getSupplierId());
        assertEquals(QuotationRequestStatus.DRAFT, request.getStatus());
        assertEquals(1, request.getRequestItems().size());
        assertEquals("Urgent request", request.getNotes());
    }

    @Test
    void shouldThrowExceptionWhenBuildingInvalidRequest() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);

        // when/then
        assertThrows(IllegalStateException.class, () ->
            QuotationRequest.builder()
                    .requestNumber("QR-12345")
                    .retailerId(1L)
                    // Missing supplierId
                    .addRequestItem(1L, 10, "Specific requirements")
                    .validUntil(validUntil)
                    .build()
        );
    }

    @Test
    void shouldFollowCorrectStatusFlow() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, "Specific requirements")
                .validUntil(validUntil)
                .build();

        // when/then
        assertEquals(QuotationRequestStatus.DRAFT, request.getStatus());
        
        request.submit();
        assertEquals(QuotationRequestStatus.PENDING, request.getStatus());
        
        request.markOfferReceived();
        assertEquals(QuotationRequestStatus.OFFERS_RECEIVED, request.getStatus());
        
        request.accept();
        assertEquals(QuotationRequestStatus.OFFER_ACCEPTED, request.getStatus());
    }

    @Test
    void shouldThrowExceptionOnInvalidStatusTransition() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationRequest request = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, "Specific requirements")
                .validUntil(validUntil)
                .build();

        // when/then
        assertThrows(IllegalStateException.class, request::markOfferReceived);
        assertThrows(IllegalStateException.class, request::accept);
    }

    @Test
    void shouldCorrectlyCheckExpiration() {
        // given
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(7);

        QuotationRequest expiredRequest = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, "Specific requirements")
                .validUntil(past)
                .build();

        QuotationRequest validRequest = QuotationRequest.builder()
                .requestNumber("QR-12346")
                .retailerId(1L)
                .supplierId(2L)
                .addRequestItem(1L, 10, "Specific requirements")
                .validUntil(future)
                .build();

        // when/then
        assertTrue(expiredRequest.isExpired());
        assertFalse(validRequest.isExpired());
    }
}
