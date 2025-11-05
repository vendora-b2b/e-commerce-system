package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaceOrderUseCase.
 */
@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RetailerRepository retailerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PlaceOrderUseCase placeOrderUseCase;

    private PlaceOrderCommand validCommand;
    private List<PlaceOrderCommand.OrderItemCommand> orderItemCommands;
    private LocalDateTime testOrderDate;

    @BeforeEach
    void setUp() {
        testOrderDate = LocalDateTime.now();
        
        orderItemCommands = new ArrayList<>();
        orderItemCommands.add(new PlaceOrderCommand.OrderItemCommand(101L, 5, 10.0, "Product A"));
        orderItemCommands.add(new PlaceOrderCommand.OrderItemCommand(102L, 3, 20.0, "Product B"));

        validCommand = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            orderItemCommands,
            "123 Main St, City",
            testOrderDate
        );
    }

    // ===== Successful Order Placement Tests =====

    @Test
    @DisplayName("Should place order successfully with valid command")
    void testExecute_ValidCommand_Success() {
        // Arrange
        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        assertEquals(1L, result.getOrder().getId());
        assertEquals("Order placed successfully", result.getMessage());
        assertNull(result.getErrorCode());

        verify(orderRepository).existsByOrderNumber("ORD-2024-001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should save order with correct details")
    void testExecute_VerifyOrderDetails() {
        // Arrange
        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);

            // Verify order details
            assertEquals("ORD-2024-001", order.getOrderNumber());
            assertEquals(5L, order.getRetailerId());
            assertEquals(10L, order.getSupplierId());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals("123 Main St, City", order.getShippingAddress());
            assertEquals(110.0, order.getTotalAmount());
            assertEquals(2, order.getOrderItems().size());

            order.setId(1L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should create order items correctly")
    void testExecute_OrderItemsCreation() {
        // Arrange
        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);

            List<OrderItem> items = order.getOrderItems();
            assertEquals(2, items.size());

            OrderItem item1 = items.get(0);
            assertEquals(101L, item1.getProductId());
            assertEquals(5, item1.getQuantity());
            assertEquals(10.0, item1.getPrice());
            assertEquals("Product A", item1.getProductName());

            OrderItem item2 = items.get(1);
            assertEquals(102L, item2.getProductId());
            assertEquals(3, item2.getQuantity());
            assertEquals(20.0, item2.getPrice());
            assertEquals("Product B", item2.getProductName());

            order.setId(1L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertTrue(result.isSuccess());
    }

    // ===== Validation Failure Tests =====

    @Test
    @DisplayName("Should fail when order number is null")
    void testExecute_NullOrderNumber_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            null,
            5L,
            10L,
            orderItemCommands,
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertNull(result.getOrder());
        assertEquals("Order number is required", result.getMessage());
        assertEquals("INVALID_ORDER_NUMBER", result.getErrorCode());
        
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when order number is empty")
    void testExecute_EmptyOrderNumber_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "",
            5L,
            10L,
            orderItemCommands,
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order number is required", result.getMessage());
        assertEquals("INVALID_ORDER_NUMBER", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when retailer ID is null")
    void testExecute_NullRetailerId_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            null,
            10L,
            orderItemCommands,
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Retailer ID is required", result.getMessage());
        assertEquals("INVALID_RETAILER_ID", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when supplier ID is null")
    void testExecute_NullSupplierId_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            null,
            orderItemCommands,
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Supplier ID is required", result.getMessage());
        assertEquals("INVALID_SUPPLIER_ID", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when order items are null")
    void testExecute_NullOrderItems_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            null,
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order must contain at least one item", result.getMessage());
        assertEquals("EMPTY_ORDER_ITEMS", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when order items are empty")
    void testExecute_EmptyOrderItems_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            new ArrayList<>(),
            "123 Main St",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order must contain at least one item", result.getMessage());
        assertEquals("EMPTY_ORDER_ITEMS", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when shipping address is null")
    void testExecute_NullShippingAddress_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            orderItemCommands,
            null,
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Shipping address is required", result.getMessage());
        assertEquals("INVALID_SHIPPING_ADDRESS", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when shipping address is empty")
    void testExecute_EmptyShippingAddress_Failure() {
        // Arrange
        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            orderItemCommands,
            "",
            testOrderDate
        );

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Shipping address is required", result.getMessage());
        assertEquals("INVALID_SHIPPING_ADDRESS", result.getErrorCode());
    }

    // ===== Foreign Key Validation Tests =====

    @Test
    @DisplayName("Should fail when retailer does not exist")
    void testExecute_RetailerNotFound_Failure() {
        // Arrange
        when(retailerRepository.findById(5L)).thenReturn(Optional.empty());

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Retailer not found", result.getMessage());
        assertEquals("RETAILER_NOT_FOUND", result.getErrorCode());

        verify(retailerRepository).findById(5L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when supplier does not exist")
    void testExecute_SupplierNotFound_Failure() {
        // Arrange
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.empty());

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Supplier not found", result.getMessage());
        assertEquals("SUPPLIER_NOT_FOUND", result.getErrorCode());

        verify(retailerRepository).findById(5L);
        verify(supplierRepository).findById(10L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when product does not exist")
    void testExecute_ProductNotFound_Failure() {
        // Arrange
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.empty());

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Product not found"));
        assertTrue(result.getMessage().contains("101"));
        assertEquals("PRODUCT_NOT_FOUND", result.getErrorCode());

        verify(retailerRepository).findById(5L);
        verify(supplierRepository).findById(10L);
        verify(productRepository).findById(101L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when second product does not exist")
    void testExecute_SecondProductNotFound_Failure() {
        // Arrange
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.empty());

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Product not found"));
        assertTrue(result.getMessage().contains("102"));
        assertEquals("PRODUCT_NOT_FOUND", result.getErrorCode());

        verify(productRepository).findById(101L);
        verify(productRepository).findById(102L);
        verify(orderRepository, never()).save(any());
    }

    // ===== Order Number Uniqueness Tests =====

    @Test
    @DisplayName("Should fail when order number already exists")
    void testExecute_DuplicateOrderNumber_Failure() {
        // Arrange
        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber("ORD-2024-001")).thenReturn(true);

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Order number already exists", result.getMessage());
        assertEquals("ORDER_NUMBER_EXISTS", result.getErrorCode());

        verify(orderRepository).existsByOrderNumber("ORD-2024-001");
        verify(orderRepository, never()).save(any());
    }

    // ===== Order Item Validation Tests =====

    @Test
    @DisplayName("Should fail when order item has null product ID")
    void testExecute_OrderItemNullProductId_Failure() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> invalidItems = new ArrayList<>();
        invalidItems.add(new PlaceOrderCommand.OrderItemCommand(null, 5, 10.0, "Product A"));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            invalidItems,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations (null productId skips product validation)
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Product ID is required for all items", result.getMessage());
        assertEquals("INVALID_PRODUCT_ID", result.getErrorCode());

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when order item has invalid quantity")
    void testExecute_OrderItemInvalidQuantity_Failure() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> invalidItems = new ArrayList<>();
        invalidItems.add(new PlaceOrderCommand.OrderItemCommand(101L, 0, 10.0, "Product A"));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            invalidItems,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Quantity must be greater than 0", result.getMessage());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when order item has negative price")
    void testExecute_OrderItemInvalidPrice_Failure() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> invalidItems = new ArrayList<>();
        invalidItems.add(new PlaceOrderCommand.OrderItemCommand(101L, 5, -10.0, "Product A"));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            invalidItems,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Price cannot be negative", result.getMessage());
        assertEquals("INVALID_PRICE", result.getErrorCode());
    }

    @Test
    @DisplayName("Should allow order item with null product name (not validated)")
    void testExecute_OrderItemNullProductName_Success() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> items = new ArrayList<>();
        items.add(new PlaceOrderCommand.OrderItemCommand(101L, 5, 10.0, null));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-001",
            5L,
            10L,
            items,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess()); // validate() doesn't check productName
    }

    // ===== Total Amount Calculation Tests =====

    @Test
    @DisplayName("Should calculate total amount correctly")
    void testExecute_TotalAmountCalculation() {
        // Arrange
        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findById(102L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);

            // (5 * 10.0) + (3 * 20.0) = 50.0 + 60.0 = 110.0
            assertEquals(110.0, order.getTotalAmount());

            order.setId(1L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(validCommand);

        // Assert
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle single order item")
    void testExecute_SingleOrderItem() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> singleItem = new ArrayList<>();
        singleItem.add(new PlaceOrderCommand.OrderItemCommand(101L, 2, 25.0, "Product A"));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD-2024-002",
            5L,
            10L,
            singleItem,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);

            assertEquals(50.0, order.getTotalAmount());
            assertEquals(1, order.getOrderItems().size());

            order.setId(2L);
            return order;
        });

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        assertEquals(2L, result.getOrder().getId());
        assertEquals(1, result.getOrder().getOrderItems().size());
    }

    // ===== Order Number Format Validation Tests =====

    @Test
    @DisplayName("Should fail when order number is too short")
    void testExecute_OrderNumberTooShort_Failure() {
        // Arrange
        List<PlaceOrderCommand.OrderItemCommand> items = new ArrayList<>();
        items.add(new PlaceOrderCommand.OrderItemCommand(101L, 2, 25.0, "Product A"));

        PlaceOrderCommand command = new PlaceOrderCommand(
            "ORD1",
            5L,
            10L,
            items,
            "123 Main St",
            testOrderDate
        );

        // Mock foreign key validations
        when(retailerRepository.findById(5L)).thenReturn(Optional.of(new Retailer()));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

        // Act
        PlaceOrderResult result = placeOrderUseCase.execute(command);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Invalid order number format (minimum 5 alphanumeric characters)", result.getMessage());
        assertEquals("INVALID_ORDER_NUMBER_FORMAT", result.getErrorCode());

        verify(orderRepository, never()).existsByOrderNumber(anyString());
        verify(orderRepository, never()).save(any());
    }
}
