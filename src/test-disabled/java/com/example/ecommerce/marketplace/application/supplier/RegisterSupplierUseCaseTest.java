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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterSupplierUseCase using Mockito.
 * Tests business logic in isolation without real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSupplierUseCase Unit Tests")
class RegisterSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private RegisterSupplierUseCase useCase;

    private RegisterSupplierCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new RegisterSupplierCommand(
            "Tech Supplies Inc",
            "contact@techsupplies.com",
            "+1234567890",
            "123 Business St",
            "profile.jpg",
            "Leading tech supplier",
            "LIC12345"
        );
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully register supplier with valid data")
    void testExecute_Success() {
        // Given
        Supplier savedSupplier = new Supplier(
            1L,
            "Tech Supplies Inc",
            "contact@techsupplies.com",
            "+1234567890",
            "123 Business St",
            "profile.jpg",
            "Leading tech supplier",
            "LIC12345",
            null,
            false
        );

        when(supplierRepository.existsByEmail("contact@techsupplies.com")).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense("LIC12345")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        RegisterSupplierResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getSupplierId());
        assertEquals("Supplier registered successfully", result.getMessage());
        assertNull(result.getErrorCode());

        verify(supplierRepository).existsByEmail("contact@techsupplies.com");
        verify(supplierRepository).existsByBusinessLicense("LIC12345");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should create supplier with initial state")
    void testExecute_InitialState() {
        // Given
        when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            // Verify initial state
            assertNull(supplier.getRating(), "Initial rating should be null");
            assertFalse(supplier.isVerified(), "Initial verified should be false");
            assertNull(supplier.getId(), "ID should be null before save");
            return new Supplier(1L, supplier.getName(), supplier.getEmail(), supplier.getPhone(),
                supplier.getAddress(), supplier.getProfilePicture(), supplier.getProfileDescription(),
                supplier.getBusinessLicense(), null, false);
        });

        // When
        RegisterSupplierResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when name is null")
    void testExecute_NullName() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            null,
            "contact@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC12345"
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSupplierId());
        assertEquals("Supplier name is required", result.getMessage());
        assertEquals("INVALID_NAME", result.getErrorCode());

        verify(supplierRepository, never()).save(any());
        verify(supplierRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should fail when name is empty")
    void testExecute_EmptyName() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "   ",
            "contact@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC12345"
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_NAME", result.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when email is null")
    void testExecute_NullEmail() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            null,
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC12345"
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Email is required", result.getMessage());
        assertEquals("INVALID_EMAIL", result.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when email is empty")
    void testExecute_EmptyEmail() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "   ",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC12345"
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_EMAIL", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when email format is invalid")
    void testExecute_InvalidEmailFormat() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "invalid-email",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC12345"
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
        assertEquals("INVALID_EMAIL_FORMAT", result.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license is null")
    void testExecute_NullBusinessLicense() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            null
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Business license is required", result.getMessage());
        assertEquals("INVALID_LICENSE", result.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license is empty")
    void testExecute_EmptyBusinessLicense() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "   "
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when business license format is invalid - too short")
    void testExecute_InvalidLicenseFormat_TooShort() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC"  // Too short
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Invalid business license format (minimum 5 alphanumeric characters)", result.getMessage());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license contains special characters")
    void testExecute_InvalidLicenseFormat_SpecialChars() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC@12345"  // Contains @
        );

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_LICENSE_FORMAT", result.getErrorCode());
    }

    // ===== Uniqueness Tests =====

    @Test
    @DisplayName("Should fail when email already exists")
    void testExecute_EmailExists() {
        // Given
        when(supplierRepository.existsByEmail("contact@techsupplies.com")).thenReturn(true);

        // When
        RegisterSupplierResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Email already registered", result.getMessage());
        assertEquals("EMAIL_EXISTS", result.getErrorCode());

        verify(supplierRepository).existsByEmail("contact@techsupplies.com");
        verify(supplierRepository, never()).existsByBusinessLicense(anyString());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when business license already exists")
    void testExecute_LicenseExists() {
        // Given
        when(supplierRepository.existsByEmail("contact@techsupplies.com")).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense("LIC12345")).thenReturn(true);

        // When
        RegisterSupplierResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Business license already registered", result.getMessage());
        assertEquals("LICENSE_EXISTS", result.getErrorCode());

        verify(supplierRepository).existsByEmail("contact@techsupplies.com");
        verify(supplierRepository).existsByBusinessLicense("LIC12345");
        verify(supplierRepository, never()).save(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle valid license with hyphens")
    void testExecute_LicenseWithHyphens() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            "+1234567890",
            "123 St",
            null,
            null,
            "LIC-12-345"  // Valid with hyphens
        );

        Supplier savedSupplier = new Supplier(1L, "Test Supplier", "test@test.com", "+1234567890",
            "123 St", null, null, "LIC-12-345", null, false);

        when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle optional fields as null")
    void testExecute_OptionalFieldsNull() {
        // Given
        RegisterSupplierCommand command = new RegisterSupplierCommand(
            "Test Supplier",
            "test@test.com",
            null,  // Optional
            null,  // Optional
            null,  // Optional
            null,  // Optional
            "LIC12345"
        );

        Supplier savedSupplier = new Supplier(1L, "Test Supplier", "test@test.com", null,
            null, null, null, "LIC12345", null, false);

        when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        RegisterSupplierResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        Supplier savedSupplier = new Supplier(1L, "Test", "test@test.com", null,
            null, null, null, "LIC12345", null, false);

        when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierRepository.existsByBusinessLicense(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(supplierRepository);
        inOrder.verify(supplierRepository).existsByEmail(anyString());
        inOrder.verify(supplierRepository).existsByBusinessLicense(anyString());
        inOrder.verify(supplierRepository).save(any(Supplier.class));
    }
}
