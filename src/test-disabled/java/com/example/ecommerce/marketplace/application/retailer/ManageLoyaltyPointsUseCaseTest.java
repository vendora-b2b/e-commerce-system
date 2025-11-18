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
 * Unit tests for ManageLoyaltyPointsUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ManageLoyaltyPointsUseCase Unit Tests")
class ManageLoyaltyPointsUseCaseTest {

    @Mock
    private RetailerRepository retailerRepository;

    @InjectMocks
    private ManageLoyaltyPointsUseCase useCase;

    private Retailer existingRetailer;

    @BeforeEach
    void setUp() {
        existingRetailer = new Retailer();
        existingRetailer.setId(1L);
        existingRetailer.setName("Premium Retail Store");
        existingRetailer.setEmail("contact@premiumretail.com");
        existingRetailer.setPhone("+1234567890");
        existingRetailer.setAddress("123 Retail Avenue");
        existingRetailer.setBusinessLicense("BL12345678");
        existingRetailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        existingRetailer.setCreditLimit(10000.0);
        existingRetailer.setTotalPurchaseAmount(5000.0);
        existingRetailer.setLoyaltyPoints(500);
    }

    // ===== ADD Points Success Tests =====

    @Test
    @DisplayName("Should successfully add loyalty points")
    void testExecute_AddPoints_Success() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getRetailerId());
        assertEquals(600, result.getNewPointsBalance());
        assertEquals(RetailerLoyaltyTier.BRONZE, result.getNewTier());
        assertEquals("Successfully added 100 loyalty points", result.getMessage());
        assertNull(result.getErrorCode());

        assertEquals(600, existingRetailer.getLoyaltyPoints());
        verify(retailerRepository).findById(1L);
        verify(retailerRepository).save(existingRetailer);
    }

    @Test
    @DisplayName("Should update tier when adding points crosses threshold")
    void testExecute_AddPoints_TierUpdate() {
        // Given
        existingRetailer.setLoyaltyPoints(900);
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,  // Will push to 1000, triggering SILVER tier
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1000, result.getNewPointsBalance());
        assertEquals(RetailerLoyaltyTier.SILVER, result.getNewTier());
        assertEquals(RetailerLoyaltyTier.SILVER, existingRetailer.getLoyaltyTier());
    }

    @Test
    @DisplayName("Should add points when starting from null")
    void testExecute_AddPoints_FromNull() {
        // Given
        existingRetailer.setLoyaltyPoints(null);
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(100, result.getNewPointsBalance());
    }

    // ===== REDEEM Points Success Tests =====

    @Test
    @DisplayName("Should successfully redeem loyalty points")
    void testExecute_RedeemPoints_Success() {
        // Given
        existingRetailer.setLoyaltyPoints(500);
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            ManageLoyaltyPointsCommand.OperationType.REDEEM
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getRetailerId());
        assertEquals(400, result.getNewPointsBalance());
        assertEquals("Successfully redeemed 100 loyalty points", result.getMessage());
        assertNull(result.getErrorCode());

        assertEquals(400, existingRetailer.getLoyaltyPoints());
        verify(retailerRepository).save(existingRetailer);
    }

    @Test
    @DisplayName("Should redeem all available points")
    void testExecute_RedeemPoints_AllPoints() {
        // Given
        existingRetailer.setLoyaltyPoints(500);
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            500,
            ManageLoyaltyPointsCommand.OperationType.REDEEM
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(0, result.getNewPointsBalance());
    }

    @Test
    @DisplayName("Should update tier when redeeming points crosses threshold downward")
    void testExecute_RedeemPoints_TierDowngrade() {
        // Given
        existingRetailer.setLoyaltyPoints(1000);
        existingRetailer.setLoyaltyTier(RetailerLoyaltyTier.SILVER);
        existingRetailer.setTotalPurchaseAmount(0.0);

        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,  // Will push to 900, triggering BRONZE tier
            ManageLoyaltyPointsCommand.OperationType.REDEEM
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(900, result.getNewPointsBalance());
        assertEquals(RetailerLoyaltyTier.BRONZE, result.getNewTier());
    }

    @Test
    @DisplayName("Should fail to redeem when insufficient points")
    void testExecute_RedeemPoints_InsufficientPoints() {
        // Given
        existingRetailer.setLoyaltyPoints(50);
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,  // More than available
            ManageLoyaltyPointsCommand.OperationType.REDEEM
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Insufficient loyalty points for redemption", result.getMessage());
        assertEquals("INSUFFICIENT_POINTS", result.getErrorCode());
        assertNull(result.getRetailerId());
        assertNull(result.getNewPointsBalance());

        // Verify points didn't change
        assertEquals(50, existingRetailer.getLoyaltyPoints());
        verify(retailerRepository, never()).save(any());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when retailer ID is null")
    void testExecute_NullRetailerId() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            null,
            100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Retailer ID is required", result.getMessage());
        assertEquals("INVALID_RETAILER_ID", result.getErrorCode());

        verify(retailerRepository, never()).findById(any());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when points is null")
    void testExecute_NullPoints() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            null,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Points must be positive", result.getMessage());
        assertEquals("INVALID_POINTS", result.getErrorCode());

        verify(retailerRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fail when points is zero")
    void testExecute_ZeroPoints() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            0,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_POINTS", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when points is negative")
    void testExecute_NegativePoints() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            -100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_POINTS", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when operation type is null")
    void testExecute_NullOperationType() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            null
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Operation type is required", result.getMessage());
        assertEquals("INVALID_OPERATION", result.getErrorCode());

        verify(retailerRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fail when retailer not found")
    void testExecute_RetailerNotFound() {
        // Given
        when(retailerRepository.findById(99L)).thenReturn(Optional.empty());

        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            99L,
            100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        // When
        ManageLoyaltyPointsResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Retailer not found", result.getMessage());
        assertEquals("RETAILER_NOT_FOUND", result.getErrorCode());

        verify(retailerRepository).findById(99L);
        verify(retailerRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should verify repository is called in correct order for ADD")
    void testExecute_RepositoryCallOrder_Add() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            ManageLoyaltyPointsCommand.OperationType.ADD
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(command);

        // Then - verify order of calls
        var inOrder = inOrder(retailerRepository);
        inOrder.verify(retailerRepository).findById(1L);
        inOrder.verify(retailerRepository).save(existingRetailer);
    }

    @Test
    @DisplayName("Should verify repository is called in correct order for REDEEM")
    void testExecute_RepositoryCallOrder_Redeem() {
        // Given
        ManageLoyaltyPointsCommand command = new ManageLoyaltyPointsCommand(
            1L,
            100,
            ManageLoyaltyPointsCommand.OperationType.REDEEM
        );

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(existingRetailer));
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(command);

        // Then - verify order of calls
        var inOrder = inOrder(retailerRepository);
        inOrder.verify(retailerRepository).findById(1L);
        inOrder.verify(retailerRepository).save(existingRetailer);
    }
}
