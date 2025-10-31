package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateQuotationRequestUseCase using Mockito
 * Tests the creation of quotation requests and validation of input data.
 */
class CreateQuotationRequestUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    private CreateQuotationRequestUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateQuotationRequestUseCase(quotationRepository);
    }

    @Test
    void shouldCreateQuotationRequest() {
        // given
        Long retailerId = 1L;
        Long supplierId = 2L;
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);
        
        CreateQuotationRequestCommand command = new CreateQuotationRequestCommand(
                retailerId,
                supplierId,
                Collections.singletonList(
                    new CreateQuotationRequestCommand.RequestItem(1L, 10, "Test specs")
                ),
                validUntil,
                "Test notes"
        );

        when(quotationRepository.saveQuotationRequest(any(QuotationRequest.class)))
            .thenAnswer(invocation -> {
                QuotationRequest request = invocation.getArgument(0);
                // Set ID using reflection to simulate save
                field(request, "id", 1L);
                return request;
            });

        // when
        CreateQuotationRequestResult result = useCase.execute(command);

        // then
        assertNotNull(result);
        assertNotNull(result.getRequestNumber());
        assertEquals(1L, result.getRequestId());

        verify(quotationRepository).saveQuotationRequest(argThat(request -> 
            request.getRetailerId().equals(retailerId) &&
            request.getSupplierId().equals(supplierId) &&
            request.getStatus() == QuotationRequestStatus.DRAFT &&
            request.getRequestItems().size() == 1
        ));
    }

    @Test
    void shouldThrowExceptionForInvalidRequest() {
        // given
        CreateQuotationRequestCommand command = new CreateQuotationRequestCommand(
                null, // Invalid: null retailerId
                2L,
                Collections.singletonList(
                    new CreateQuotationRequestCommand.RequestItem(1L, 10, "Test specs")
                ),
                LocalDateTime.now().plusDays(7),
                "Test notes"
        );

        // when/then
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        assertEquals("Retailer ID is required", e.getMessage());
        verify(quotationRepository, never()).saveQuotationRequest(any());
    }

    // Utility method to set private fields
    private void field(Object obj, String fieldName, Object value) {
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}