package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.application.notification.NotificationService;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateQuotationUseCase Unit Tests")
class CreateQuotationUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RetailerRepository retailerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CreateQuotationUseCase useCase;

    private Retailer testRetailer;
    private Supplier testSupplier;
    private ProductVariant testVariant;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testRetailer = new Retailer();
        testRetailer.setId(1L);
        testRetailer.setName("Test Retailer");
        testRetailer.setEmail("retailer@test.com");

        testSupplier = new Supplier();
        testSupplier.setId(10L);
        testSupplier.setName("Test Supplier");
        testSupplier.setEmail("supplier@test.com");

        testVariant = new ProductVariant();
        testVariant.setId(100L);
        testVariant.setProductId(50L);
        testVariant.setSku("VAR-001");

        testProduct = new Product();
        testProduct.setId(50L);
        testProduct.setSupplierId(10L);
        testProduct.setName("Test Product");
    }

    @Test
    @DisplayName("Should successfully create quotation with single item")
    void testExecute_Success_SingleItem() {
        Long retailerId = 1L;
        CreateQuotationCommand.QuotationItemRequest itemRequest = 
            new CreateQuotationCommand.QuotationItemRequest(
                100L, 10, 25.0, LocalDate.now().plusDays(7), "Urgent delivery needed"
            );
        CreateQuotationCommand command = new CreateQuotationCommand(
            List.of(itemRequest), "Please expedite this order"
        );

        when(retailerRepository.findById(retailerId)).thenReturn(Optional.of(testRetailer));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(testVariant));
        when(productRepository.findById(50L)).thenReturn(Optional.of(testProduct));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(testSupplier));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(invocation -> {
            Quotation q = invocation.getArgument(0);
            return q;
        });

        CreateQuotationResult result = useCase.execute(retailerId, command);

        assertNotNull(result);
        assertEquals(1, result.getQuotations().size());
        assertEquals("Test Supplier", result.getQuotations().get(0).getSupplierName());
        assertEquals(1, result.getQuotations().get(0).getItemCount());
        assertEquals(QuotationStatus.PENDING_SUPPLIER.name(), result.getQuotations().get(0).getStatus());
        assertTrue(result.getMessage().contains("Created 1 quotation(s)"));

        verify(quotationRepository).save(any(Quotation.class));
        verify(retailerRepository).findById(retailerId);
    }

    @Test
    @DisplayName("Should throw exception when items list is empty")
    void testExecute_EmptyItems_ThrowsException() {
        Long retailerId = 1L;
        CreateQuotationCommand command = new CreateQuotationCommand(new ArrayList<>(), "Notes");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(retailerId, command)
        );

        assertEquals("At least one item is required", exception.getMessage());
        verify(quotationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when retailer not found")
    void testExecute_RetailerNotFound_ThrowsException() {
        Long retailerId = 999L;
        CreateQuotationCommand.QuotationItemRequest itemRequest = 
            new CreateQuotationCommand.QuotationItemRequest(100L, 5, null, null, null);
        CreateQuotationCommand command = new CreateQuotationCommand(List.of(itemRequest), null);

        when(retailerRepository.findById(retailerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(retailerId, command)
        );

        assertEquals("Retailer not found: 999", exception.getMessage());
        verify(quotationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when variant not found")
    void testExecute_VariantNotFound_ThrowsException() {
        Long retailerId = 1L;
        Long invalidVariantId = 9999L;
        CreateQuotationCommand.QuotationItemRequest itemRequest = 
            new CreateQuotationCommand.QuotationItemRequest(invalidVariantId, 5, null, null, null);
        CreateQuotationCommand command = new CreateQuotationCommand(List.of(itemRequest), null);

        when(retailerRepository.findById(retailerId)).thenReturn(Optional.of(testRetailer));
        when(variantRepository.findById(invalidVariantId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(retailerId, command)
        );

        assertEquals("Variant not found: 9999", exception.getMessage());
        verify(quotationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create multiple quotations when items belong to different suppliers")
    void testExecute_MultipleSuppliers_CreatesMultipleQuotations() {
        Long retailerId = 1L;

        ProductVariant variant1 = new ProductVariant();
        variant1.setId(101L);
        variant1.setProductId(51L);

        ProductVariant variant2 = new ProductVariant();
        variant2.setId(102L);
        variant2.setProductId(52L);

        Product product1 = new Product();
        product1.setId(51L);
        product1.setSupplierId(10L);

        Product product2 = new Product();
        product2.setId(52L);
        product2.setSupplierId(20L);

        Supplier supplier1 = new Supplier();
        supplier1.setId(10L);
        supplier1.setName("Supplier One");

        Supplier supplier2 = new Supplier();
        supplier2.setId(20L);
        supplier2.setName("Supplier Two");

        CreateQuotationCommand.QuotationItemRequest item1 = 
            new CreateQuotationCommand.QuotationItemRequest(101L, 5, 10.0, null, null);
        CreateQuotationCommand.QuotationItemRequest item2 = 
            new CreateQuotationCommand.QuotationItemRequest(102L, 8, 15.0, null, null);
        CreateQuotationCommand command = new CreateQuotationCommand(List.of(item1, item2), "Multi-supplier order");

        when(retailerRepository.findById(retailerId)).thenReturn(Optional.of(testRetailer));
        when(variantRepository.findById(101L)).thenReturn(Optional.of(variant1));
        when(variantRepository.findById(102L)).thenReturn(Optional.of(variant2));
        when(productRepository.findById(51L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(52L)).thenReturn(Optional.of(product2));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(supplier1));
        when(supplierRepository.findById(20L)).thenReturn(Optional.of(supplier2));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateQuotationResult result = useCase.execute(retailerId, command);

        assertNotNull(result);
        assertEquals(2, result.getQuotations().size());
        assertTrue(result.getMessage().contains("Created 2 quotation(s)"));
        assertTrue(result.getMessage().contains("2 supplier(s)"));

        verify(quotationRepository, times(2)).save(any(Quotation.class));
    }
}

