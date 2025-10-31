package com.example.ecommerce.marketplace.domain.quotation;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class QuotationOfferTest {

    @Test
    void shouldCreateValidQuotationOffer() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        
        // when
        QuotationOffer offer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(validUntil)
                .notes("Competitive offer")
                .termsAndConditions("Standard terms")
                .build();

        // then
        assertNotNull(offer);
        assertEquals("QO-12345", offer.getOfferNumber());
        assertEquals(1L, offer.getQuotationRequestId());
        assertEquals(2L, offer.getRetailerId());
        assertEquals(3L, offer.getSupplierId());
        assertEquals(QuotationOfferStatus.DRAFT, offer.getStatus());
        assertEquals(1, offer.getOfferItems().size());
        assertEquals("Competitive offer", offer.getNotes());
        assertEquals("Standard terms", offer.getTermsAndConditions());
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationOffer offer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 2, 100.0, "Item 1")
                .addOfferItem(2L, 3, 50.0, "Item 2")
                .validUntil(validUntil)
                .termsAndConditions("Standard terms")
                .build();

        // when
        offer.submit();

        // then
        assertEquals(350.0, offer.getTotalAmount()); // (2 * 100) + (3 * 50)
    }

    @Test
    void shouldFollowCorrectStatusFlow() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationOffer offer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(validUntil)
                .termsAndConditions("Standard terms")
                .build();

        // when/then
        assertEquals(QuotationOfferStatus.DRAFT, offer.getStatus());
        
        offer.submit();
        assertEquals(QuotationOfferStatus.SUBMITTED, offer.getStatus());
        
        offer.accept();
        assertEquals(QuotationOfferStatus.ACCEPTED, offer.getStatus());
    }

    @Test
    void shouldThrowExceptionOnInvalidStatusTransition() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationOffer offer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(validUntil)
                .termsAndConditions("Standard terms")
                .build();

        // when/then
        assertThrows(IllegalStateException.class, offer::accept);
        assertThrows(IllegalStateException.class, offer::reject);
    }

    @Test
    void shouldNotAllowWithdrawalOfAcceptedOffer() {
        // given
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        QuotationOffer offer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(validUntil)
                .termsAndConditions("Standard terms")
                .build();

        // when
        offer.submit();
        offer.accept();

        // then
        assertThrows(IllegalStateException.class, offer::withdraw);
    }

    @Test
    void shouldCorrectlyCheckExpiration() {
        // given
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(7);

        QuotationOffer expiredOffer = QuotationOffer.builder()
                .offerNumber("QO-12345")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(past)
                .termsAndConditions("Standard terms")
                .build();

        QuotationOffer validOffer = QuotationOffer.builder()
                .offerNumber("QO-12346")
                .quotationRequestId(1L)
                .retailerId(2L)
                .supplierId(3L)
                .addOfferItem(1L, 10, 100.0, "Standard specs")
                .validUntil(future)
                .termsAndConditions("Standard terms")
                .build();

        // when/then
        assertTrue(expiredOffer.isExpired());
        assertFalse(validOffer.isExpired());
    }
}