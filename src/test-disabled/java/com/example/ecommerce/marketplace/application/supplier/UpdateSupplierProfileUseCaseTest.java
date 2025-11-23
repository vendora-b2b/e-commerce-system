package com.example.ecommerce.marketplace.application.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateSupplierProfileUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSupplierProfileUseCase Unit Tests")
class UpdateSupplierProfileUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private UpdateSupplierProfileUseCase useCase;

    private Supplier existingSupplier;
    private UpdateSupplierProfileCommand validCommand;

    @BeforeEach
    void setUp() {
        existingSupplier = new Supplier(
            1L,
            "Tech Supplies Inc",
            "contact@techsupplies.com",
            "+1234567890",
            "123 Business St",
            "profile.jpg",
            "Leading tech supplier",
            "LIC12345",
            4.5,
            true
        );

        validCommand = new UpdateSupplierProfileCommand(
            1L,
            "Updated Tech Supplies",
            "+9876543210",
            "456 New Address",
            "Updated description"
        );
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully update supplier profile")
    void testExecute_Success() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateSupplierProfileResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getSupplierId());
        assertEquals("Supplier profile updated successfully", result.getMessage());
        assertNull(result.getErrorCode());

        // Verify domain logic was called
        assertEquals("Updated Tech Supplies", existingSupplier.getName());
        assertEquals("+9876543210", existingSupplier.getPhone());
        assertEquals("456 New Address", existingSupplier.getAddress());
        assertEquals("Updated description", existingSupplier.getProfileDescription());

        verify(supplierRepository).findById(1L);
        verify(supplierRepository).save(existingSupplier);
    }

    @Test
    @DisplayName("Should update only provided fields")
    void testExecute_PartialUpdate() {
        // Given
        UpdateSupplierProfileCommand partialCommand = new UpdateSupplierProfileCommand(
            1L,
            "New Name Only",
            null,  // Don't update phone
            null,  // Don't update address
            null   // Don't update description
        );

        String originalPhone = existingSupplier.getPhone();
        String originalAddress = existingSupplier.getAddress();
        String originalDescription = existingSupplier.getProfileDescription();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateSupplierProfileResult result = useCase.execute(partialCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("New Name Only", existingSupplier.getName());
        assertEquals(originalPhone, existingSupplier.getPhone());
        assertEquals(originalAddress, existingSupplier.getAddress());
        assertEquals(originalDescription, existingSupplier.getProfileDescription());
    }

    @Test
    @DisplayName("Should trim whitespace from updated fields")
    void testExecute_TrimWhitespace() {
        // Given
        UpdateSupplierProfileCommand command = new UpdateSupplierProfileCommand(
            1L,
            "  Trimmed Name  ",
            "  +1234567890  ",
            "  123 Street  ",
            "  Description  "
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateSupplierProfileResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Trimmed Name", existingSupplier.getName());
        assertEquals("+1234567890", existingSupplier.getPhone());
        assertEquals("123 Street", existingSupplier.getAddress());
        assertEquals("Description", existingSupplier.getProfileDescription());
    }

    @Test
    @DisplayName("Should not update email or business license")
    void testExecute_DoesNotUpdateEmailOrLicense() {
        // Given
        String originalEmail = existingSupplier.getEmail();
        String originalLicense = existingSupplier.getBusinessLicense();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalEmail, existingSupplier.getEmail(), "Email should not change");
        assertEquals(originalLicense, existingSupplier.getBusinessLicense(), "License should not change");
    }

    @Test
    @DisplayName("Should preserve rating and verification status")
    void testExecute_PreservesRatingAndVerification() {
        // Given
        Double originalRating = existingSupplier.getRating();
        Boolean originalVerified = existingSupplier.getVerified();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalRating, existingSupplier.getRating(), "Rating should not change");
        assertEquals(originalVerified, existingSupplier.getVerified(), "Verification status should not change");
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when supplier ID is null")
    void testExecute_NullSupplierId() {
        // Given
        UpdateSupplierProfileCommand command = new UpdateSupplierProfileCommand(
            null,  // Null ID
            "New Name",
            "+1234567890",
            "123 St",
            "Description"
        );

        // When
        UpdateSupplierProfileResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSupplierId());
        assertEquals("Supplier ID is required", result.getMessage());
        assertEquals("INVALID_SUPPLIER_ID", result.getErrorCode());

        verify(supplierRepository, never()).findById(any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when supplier not found")
    void testExecute_SupplierNotFound() {
        // Given
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateSupplierProfileCommand command = new UpdateSupplierProfileCommand(
            999L,
            "New Name",
            "+1234567890",
            "123 St",
            "Description"
        );

        // When
        UpdateSupplierProfileResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSupplierId());
        assertEquals("Supplier not found", result.getMessage());
        assertEquals("SUPPLIER_NOT_FOUND", result.getErrorCode());

        verify(supplierRepository).findById(999L);
        verify(supplierRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle empty strings as non-updates")
    void testExecute_EmptyStringsIgnored() {
        // Given
        UpdateSupplierProfileCommand command = new UpdateSupplierProfileCommand(
            1L,
            "",      // Empty - should not update
            "   ",   // Whitespace - should not update
            "",      // Empty - should not update
            ""       // Empty - should not update
        );

        String originalName = existingSupplier.getName();
        String originalPhone = existingSupplier.getPhone();
        String originalAddress = existingSupplier.getAddress();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateSupplierProfileResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(originalName, existingSupplier.getName());
        assertEquals(originalPhone, existingSupplier.getPhone());
        assertEquals(originalAddress, existingSupplier.getAddress());
        // Description is set to empty string (trimmed) as per domain logic
    }

    @Test
    @DisplayName("Should successfully update all fields to new values")
    void testExecute_UpdateAllFields() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateSupplierProfileResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertNotEquals("Tech Supplies Inc", existingSupplier.getName());
        assertNotEquals("+1234567890", existingSupplier.getPhone());
        assertNotEquals("123 Business St", existingSupplier.getAddress());
        assertNotEquals("Leading tech supplier", existingSupplier.getProfileDescription());
    }

    @Test
    @DisplayName("Should call save after updating profile")
    void testExecute_CallsSaveAfterUpdate() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(supplierRepository);
        inOrder.verify(supplierRepository).findById(1L);
        inOrder.verify(supplierRepository).save(existingSupplier);
    }

    @Test
    @DisplayName("Should only call findById once")
    void testExecute_FindByIdCalledOnce() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        verify(supplierRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should only call save once")
    void testExecute_SaveCalledOnce() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        verify(supplierRepository, times(1)).save(existingSupplier);
    }

    @Test
    @DisplayName("Should return saved supplier ID in result")
    void testExecute_ReturnsSavedSupplierId() {
        // Given
        Supplier savedSupplier = new Supplier(
            1L, "Updated Name", existingSupplier.getEmail(), existingSupplier.getPhone(),
            existingSupplier.getAddress(), existingSupplier.getProfilePicture(),
            existingSupplier.getProfileDescription(), existingSupplier.getBusinessLicense(),
            existingSupplier.getRating(), existingSupplier.getVerified()
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        UpdateSupplierProfileResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getSupplierId());
    }
}
