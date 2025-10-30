package com.example.ecommerce.marketplace.application.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
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
 * Unit tests for UpdateRetailerProfileUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateRetailerProfileUseCase Unit Tests")
class UpdateRetailerProfileUseCaseTest {

    @Mock
    private RetailerRepository retailerRepository;

    @InjectMocks
    private UpdateRetailerProfileUseCase useCase;

    private Retailer existingRetailer;
    private UpdateRetailerProfileCommand validCommand;

    @BeforeEach
    void setUp() {
        existingRetailer = new Retailer();
        existingRetailer.setId(1L);
        existingRetailer.setName("Premium Retail Store");
        existingRetailer.setEmail("contact@premiumretail.com");
        existingRetailer.setPhone("+1234567890");
        existingRetailer.setAddress("123 Retail Avenue");
        existingRetailer.setProfilePicture("profile.jpg");
        existingRetailer.setProfileDescription("Leading retail business");
        existingRetailer.setBusinessLicense("BL12345678");
        existingRetailer.setLoyaltyTier(RetailerLoyaltyTier.SILVER);
        existingRetailer.setCreditLimit(10000.0);
        existingRetailer.setTotalPurchaseAmount(15000.0);
        existingRetailer.setLoyaltyPoints(1500);

        validCommand = new UpdateRetailerProfileCommand(
            1L,
            "Updated Retail Store",
            "+9876543210",
            "456 New Address",
            "Updated business description"
        );
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully update retailer profile")
    void testExecute_Success() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateRetailerProfileResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getRetailerId());
        assertEquals("Profile updated successfully", result.getMessage());
        assertNull(result.getErrorCode());

        // Verify domain logic was called
        assertEquals("Updated Retail Store", existingRetailer.getName());
        assertEquals("+9876543210", existingRetailer.getPhone());
        assertEquals("456 New Address", existingRetailer.getAddress());
        assertEquals("Updated business description", existingRetailer.getProfileDescription());

        verify(retailerRepository).findById(1L);
        verify(retailerRepository).save(existingRetailer);
    }

    @Test
    @DisplayName("Should update only provided fields")
    void testExecute_PartialUpdate() {
        // Given
        UpdateRetailerProfileCommand partialCommand = new UpdateRetailerProfileCommand(
            1L,
            "New Name Only",
            null,  // Don't update phone
            null,  // Don't update address
            null   // Don't update description
        );

        String originalPhone = existingRetailer.getPhone();
        String originalAddress = existingRetailer.getAddress();
        String originalDescription = existingRetailer.getProfileDescription();

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateRetailerProfileResult result = useCase.execute(partialCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("New Name Only", existingRetailer.getName());
        assertEquals(originalPhone, existingRetailer.getPhone());
        assertEquals(originalAddress, existingRetailer.getAddress());
        assertEquals(originalDescription, existingRetailer.getProfileDescription());
    }

    @Test
    @DisplayName("Should trim whitespace from updated fields")
    void testExecute_TrimWhitespace() {
        // Given
        UpdateRetailerProfileCommand command = new UpdateRetailerProfileCommand(
            1L,
            "  Trimmed Name  ",
            "  +1234567890  ",
            "  123 Street  ",
            "  Description  "
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateRetailerProfileResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Trimmed Name", existingRetailer.getName());
        assertEquals("+1234567890", existingRetailer.getPhone());
        assertEquals("123 Street", existingRetailer.getAddress());
        assertEquals("Description", existingRetailer.getProfileDescription());
    }

    @Test
    @DisplayName("Should not update email or business license")
    void testExecute_DoesNotUpdateEmailOrLicense() {
        // Given
        String originalEmail = existingRetailer.getEmail();
        String originalLicense = existingRetailer.getBusinessLicense();

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalEmail, existingRetailer.getEmail(), "Email should not change");
        assertEquals(originalLicense, existingRetailer.getBusinessLicense(), "License should not change");
    }

    @Test
    @DisplayName("Should preserve loyalty tier and points")
    void testExecute_PreservesLoyaltyData() {
        // Given
        RetailerLoyaltyTier originalTier = existingRetailer.getLoyaltyTier();
        Integer originalPoints = existingRetailer.getLoyaltyPoints();
        Double originalPurchases = existingRetailer.getTotalPurchaseAmount();

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then
        assertEquals(originalTier, existingRetailer.getLoyaltyTier(), "Loyalty tier should not change");
        assertEquals(originalPoints, existingRetailer.getLoyaltyPoints(), "Points should not change");
        assertEquals(originalPurchases, existingRetailer.getTotalPurchaseAmount(), "Purchase amount should not change");
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when retailer ID is null")
    void testExecute_NullRetailerId() {
        // Given
        UpdateRetailerProfileCommand command = new UpdateRetailerProfileCommand(
            null,  // Null ID
            "New Name",
            "+1234567890",
            "123 St",
            "Description"
        );

        // When
        UpdateRetailerProfileResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getRetailerId());
        assertEquals("Retailer ID is required", result.getMessage());
        assertEquals("INVALID_RETAILER_ID", result.getErrorCode());

        verify(retailerRepository, never()).findById(any());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when retailer not found")
    void testExecute_RetailerNotFound() {
        // Given
        when(retailerRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateRetailerProfileCommand command = new UpdateRetailerProfileCommand(
            99L,
            "New Name",
            "+1234567890",
            "123 St",
            "Description"
        );

        // When
        UpdateRetailerProfileResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Retailer not found", result.getMessage());
        assertEquals("RETAILER_NOT_FOUND", result.getErrorCode());

        verify(retailerRepository).findById(99L);
        verify(retailerRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle empty strings by not updating those fields")
    void testExecute_EmptyStrings() {
        // Given
        UpdateRetailerProfileCommand command = new UpdateRetailerProfileCommand(
            1L,
            "",  // Empty
            "",  // Empty
            "",  // Empty
            ""   // Empty
        );

        String originalName = existingRetailer.getName();
        String originalPhone = existingRetailer.getPhone();
        String originalAddress = existingRetailer.getAddress();

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UpdateRetailerProfileResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(originalName, existingRetailer.getName());
        assertEquals(originalPhone, existingRetailer.getPhone());
        assertEquals(originalAddress, existingRetailer.getAddress());
    }

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(retailerRepository);
        inOrder.verify(retailerRepository).findById(1L);
        inOrder.verify(retailerRepository).save(existingRetailer);
    }
}
