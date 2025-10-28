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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterRetailerUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterRetailerUseCase Unit Tests")
class RegisterRetailerUseCaseTest {

    @Mock
    private RetailerRepository retailerRepository;

    @InjectMocks
    private RegisterRetailerUseCase useCase;

    private RegisterRetailerCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new RegisterRetailerCommand(
            "Premium Retail Store",
            "contact@premiumretail.com",
            "+1234567890",
            "123 Retail Avenue",
            "profile.jpg",
            "Leading retail business",
            "BL12345678",
            10000.0
        );
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully register retailer with valid data")
    void testExecute_Success() {
        // Given
        Retailer savedRetailer = createSavedRetailer(1L);

        when(retailerRepository.existsByEmail("contact@premiumretail.com")).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense("BL12345678")).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

        // When
        RegisterRetailerResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getRetailerId());
        assertEquals("Retailer registered successfully", result.getMessage());
        assertNull(result.getErrorCode());

        verify(retailerRepository).existsByEmail("contact@premiumretail.com");
        verify(retailerRepository).existsByBusinessLicense("BL12345678");
        verify(retailerRepository).save(any(Retailer.class));
    }

    @Test
    @DisplayName("Should create retailer with initial state")
    void testExecute_InitialState() {
        // Given
        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> {
            Retailer retailer = invocation.getArgument(0);
            // Verify initial state
            assertEquals(RetailerLoyaltyTier.BRONZE, retailer.getLoyaltyTier(), "Initial tier should be BRONZE");
            assertEquals(0, retailer.getLoyaltyPoints(), "Initial points should be 0");
            assertEquals(0.0, retailer.getTotalPurchaseAmount(), "Initial purchase should be 0.0");
            assertNull(retailer.getId(), "ID should be null before save");

            retailer.setId(1L);
            return retailer;
        });

        // When
        RegisterRetailerResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle null credit limit as default 0.0")
    void testExecute_NullCreditLimit() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            null // null credit limit
        );

        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenAnswer(invocation -> {
            Retailer retailer = invocation.getArgument(0);
            assertEquals(0.0, retailer.getCreditLimit(), "Null credit limit should default to 0.0");
            retailer.setId(1L);
            return retailer;
        });

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when name is null")
    void testExecute_NullName() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            null,
            "contact@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getRetailerId());
        assertEquals("Retailer name is required", result.getMessage());
        assertEquals("INVALID_NAME", result.getErrorCode());

        verify(retailerRepository, never()).save(any());
        verify(retailerRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should fail when name is empty")
    void testExecute_EmptyName() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "   ",
            "contact@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_NAME", result.getErrorCode());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when email is null")
    void testExecute_NullEmail() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            null,
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Email is required", result.getMessage());
        assertEquals("INVALID_EMAIL", result.getErrorCode());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when email is empty")
    void testExecute_EmptyEmail() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "   ",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_EMAIL", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when email format is invalid")
    void testExecute_InvalidEmailFormat() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "invalid-email",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL12345678",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
        assertEquals("INVALID_EMAIL_FORMAT", result.getErrorCode());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license is null")
    void testExecute_NullBusinessLicense() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            null,
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Business license is required", result.getMessage());
        assertEquals("INVALID_LICENSE", result.getErrorCode());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license is empty")
    void testExecute_EmptyBusinessLicense() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "   ",
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when business license format is invalid - too short")
    void testExecute_InvalidLicenseFormat_TooShort() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL123",  // Too short (less than 8 characters)
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid business license format (8-20 uppercase alphanumeric characters)", result.getMessage());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license format is invalid - too long")
    void testExecute_InvalidLicenseFormat_TooLong() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL123456789012345678901",  // Too long (more than 20 characters)
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when business license contains lowercase")
    void testExecute_InvalidLicenseFormat_Lowercase() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "bl12345678",  // Contains lowercase
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when business license contains special characters")
    void testExecute_InvalidLicenseFormat_SpecialChars() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL@12345678",  // Contains @
            10000.0
        );

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
    }

    // ===== Uniqueness Tests =====

    @Test
    @DisplayName("Should fail when email already exists")
    void testExecute_EmailExists() {
        // Given
        when(retailerRepository.existsByEmail("contact@premiumretail.com")).thenReturn(true);

        // When
        RegisterRetailerResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Email already registered", result.getMessage());
        assertEquals("EMAIL_EXISTS", result.getErrorCode());

        verify(retailerRepository).existsByEmail("contact@premiumretail.com");
        verify(retailerRepository, never()).existsByBusinessLicense(anyString());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license already exists")
    void testExecute_LicenseExists() {
        // Given
        when(retailerRepository.existsByEmail("contact@premiumretail.com")).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense("BL12345678")).thenReturn(true);

        // When
        RegisterRetailerResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Business license already registered", result.getMessage());
        assertEquals("LICENSE_EXISTS", result.getErrorCode());

        verify(retailerRepository).existsByEmail("contact@premiumretail.com");
        verify(retailerRepository).existsByBusinessLicense("BL12345678");
        verify(retailerRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle optional fields as null")
    void testExecute_OptionalFieldsNull() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            null,  // Optional
            null,  // Optional
            null,  // Optional
            null,  // Optional
            "BL12345678",
            10000.0
        );

        Retailer savedRetailer = createSavedRetailer(1L);

        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        verify(retailerRepository).save(any(Retailer.class));
    }

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        Retailer savedRetailer = createSavedRetailer(1L);

        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(retailerRepository);
        inOrder.verify(retailerRepository).existsByEmail(anyString());
        inOrder.verify(retailerRepository).existsByBusinessLicense(anyString());
        inOrder.verify(retailerRepository).save(any(Retailer.class));
    }

    @Test
    @DisplayName("Should accept valid license at minimum length (8 characters)")
    void testExecute_ValidLicenseMinLength() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL123456",  // Exactly 8 characters
            10000.0
        );

        Retailer savedRetailer = createSavedRetailer(1L);

        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should accept valid license at maximum length (20 characters)")
    void testExecute_ValidLicenseMaxLength() {
        // Given
        RegisterRetailerCommand command = new RegisterRetailerCommand(
            "Test Retailer",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "BL123456789012345678",  // Exactly 20 characters
            10000.0
        );

        Retailer savedRetailer = createSavedRetailer(1L);

        when(retailerRepository.existsByEmail(anyString())).thenReturn(false);
        when(retailerRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

        // When
        RegisterRetailerResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    // Helper method
    private Retailer createSavedRetailer(Long id) {
        Retailer retailer = new Retailer();
        retailer.setId(id);
        retailer.setName("Premium Retail Store");
        retailer.setEmail("contact@premiumretail.com");
        retailer.setPhone("+1234567890");
        retailer.setAddress("123 Retail Avenue");
        retailer.setProfilePicture("profile.jpg");
        retailer.setProfileDescription("Leading retail business");
        retailer.setBusinessLicense("BL12345678");
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE);
        retailer.setCreditLimit(10000.0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.setLoyaltyPoints(0);
        return retailer;
    }
}
