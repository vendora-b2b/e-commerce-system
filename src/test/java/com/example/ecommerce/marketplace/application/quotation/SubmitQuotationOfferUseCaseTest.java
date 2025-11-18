package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.*;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubmitQuotationOfferUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;
    
    @Mock
    private SupplierRepository supplierRepository;

    private SubmitQuotationOfferUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new SubmitQuotationOfferUseCase(quotationRepository, supplierRepository);
    }

    @Test
    void shouldSubmitQuotationOffer() {
        // given
        Long requestId = 1L;
        Long retailerId = 2L;
        Long supplierId = 3L;
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);

        QuotationRequest existingRequest = QuotationRequest.builder()
                .requestNumber("QR-12345")
                .retailerId(retailerId)
                .supplierId(supplierId)
                .addRequestItem(1L, 10, "Original specs")
                .validUntil(validUntil)
                .build();
        existingRequest.submit(); // Submit the request first

        when(quotationRepository.findRequestById(requestId)).thenReturn(existingRequest);
        when(supplierRepository.findById(supplierId)).thenReturn(java.util.Optional.of(new com.example.ecommerce.marketplace.domain.supplier.Supplier()));
        when(quotationRepository.saveQuotationOffer(any(QuotationOffer.class)))
            .thenAnswer(invocation -> {
                QuotationOffer offer = invocation.getArgument(0);
                // Simulate repository setting the ID using reflection
                try {
                    java.lang.reflect.Field idField = QuotationOffer.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(offer, 1L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return offer;
            });

        SubmitQuotationOfferCommand command = new SubmitQuotationOfferCommand(
                requestId,
                supplierId,
                Collections.singletonList(
                    new SubmitQuotationOfferCommand.OfferItem(1L, 10, 100.0, "Test specs", "Notes")
                ),
                validUntil,
                "Test notes",
                "Standard terms"
        );

        // when
        SubmitQuotationOfferResult result = useCase.execute(command);

        // then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getOfferNumber());
        assertEquals(1L, result.getOfferId());
        assertEquals(1000.0, result.getTotalAmount()); // 10 * 100.0

        verify(quotationRepository).saveQuotationOffer(argThat(offer ->
            offer.getQuotationRequestId().equals(requestId) &&
            offer.getSupplierId().equals(supplierId) &&
            offer.getStatus() == QuotationOfferStatus.SUBMITTED &&
            offer.getOfferItems().size() == 1
        ));

        verify(quotationRepository).saveQuotationRequest(argThat(request ->
            request.getStatus() == QuotationRequestStatus.OFFERS_RECEIVED
        ));
    }

    @Test
    void shouldThrowExceptionWhenRequestNotFound() {
        // given
        Long requestId = 1L;
        when(quotationRepository.findRequestById(requestId)).thenReturn(null);

        SubmitQuotationOfferCommand command = new SubmitQuotationOfferCommand(
                requestId,
                3L,
                Collections.singletonList(
                    new SubmitQuotationOfferCommand.OfferItem(1L, 10, 100.0, "Test specs", "Notes")
                ),
                LocalDateTime.now().plusDays(7),
                "Test notes",
                "Standard terms"
        );

        // when
        SubmitQuotationOfferResult result = useCase.execute(command);

        // then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Quotation request not found", result.getMessage());
        verify(quotationRepository, never()).saveQuotationOffer(any(QuotationOffer.class));
    }
}
