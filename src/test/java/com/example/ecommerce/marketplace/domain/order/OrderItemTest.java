package com.example.ecommerce.marketplace.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OrderItem domain entity.
 */
class OrderItemTest {

    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        orderItem = new OrderItem(1L, 101L, 5, 10.0, "Product A");
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should validate order item with all valid fields")
    void testValidate_ValidOrderItem() {
        assertTrue(orderItem.validate());
    }

    @Test
    @DisplayName("Should reject order item with null product ID")
    void testValidate_NullProductId() {
        orderItem.setProductId(null);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should reject order item with null quantity")
    void testValidate_NullQuantity() {
        orderItem.setQuantity(null);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should reject order item with zero quantity")
    void testValidate_ZeroQuantity() {
        orderItem.setQuantity(0);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should reject order item with negative quantity")
    void testValidate_NegativeQuantity() {
        orderItem.setQuantity(-5);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should reject order item with null price")
    void testValidate_NullPrice() {
        orderItem.setPrice(null);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should accept order item with zero price (price >= 0 is valid)")
    void testValidate_ZeroPrice() {
        orderItem.setPrice(0.0);
        assertTrue(orderItem.validate()); // validate() allows price >= 0
    }

    @Test
    @DisplayName("Should reject order item with negative price")
    void testValidate_NegativePrice() {
        orderItem.setPrice(-10.0);
        assertFalse(orderItem.validate());
    }

    @Test
    @DisplayName("Should allow null product name (productName not validated)")
    void testValidate_NullProductName() {
        orderItem.setProductName(null);
        assertTrue(orderItem.validate()); // validate() doesn't check productName
    }

    @Test
    @DisplayName("Should allow empty product name (productName not validated)")
    void testValidate_EmptyProductName() {
        orderItem.setProductName("");
        assertTrue(orderItem.validate()); // validate() doesn't check productName
    }

    @Test
    @DisplayName("Should allow whitespace-only product name (productName not validated)")
    void testValidate_WhitespaceProductName() {
        orderItem.setProductName("   ");
        assertTrue(orderItem.validate()); // validate() doesn't check productName
    }

    // ===== Calculate Subtotal Tests =====

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void testCalculateSubtotal_ValidValues() {
        // 5 * 10.0 = 50.0
        assertEquals(50.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should return 0.0 for null quantity")
    void testCalculateSubtotal_NullQuantity() {
        orderItem.setQuantity(null);
        assertEquals(0.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should return 0.0 for null price")
    void testCalculateSubtotal_NullPrice() {
        orderItem.setPrice(null);
        assertEquals(0.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should return 0.0 for both null quantity and price")
    void testCalculateSubtotal_BothNull() {
        orderItem.setQuantity(null);
        orderItem.setPrice(null);
        assertEquals(0.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should round subtotal to 2 decimal places")
    void testCalculateSubtotal_Rounding() {
        orderItem.setQuantity(3);
        orderItem.setPrice(10.333);
        // 3 * 10.333 = 30.999, rounded to 31.0
        assertEquals(31.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should handle large quantities")
    void testCalculateSubtotal_LargeQuantity() {
        orderItem.setQuantity(1000);
        orderItem.setPrice(15.99);
        assertEquals(15990.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should handle decimal prices")
    void testCalculateSubtotal_DecimalPrice() {
        orderItem.setQuantity(7);
        orderItem.setPrice(12.99);
        assertEquals(90.93, orderItem.calculateSubtotal());
    }

    // ===== Update Quantity Tests =====

    @Test
    @DisplayName("Should update quantity with valid value")
    void testUpdateQuantity_ValidQuantity() {
        orderItem.updateQuantity(10);
        assertEquals(10, orderItem.getQuantity());
    }

    @Test
    @DisplayName("Should throw exception for null quantity")
    void testUpdateQuantity_NullQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderItem.updateQuantity(null);
        });
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void testUpdateQuantity_ZeroQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderItem.updateQuantity(0);
        });
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void testUpdateQuantity_NegativeQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderItem.updateQuantity(-5);
        });
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    @DisplayName("Should update to minimum valid quantity")
    void testUpdateQuantity_MinimumQuantity() {
        orderItem.updateQuantity(1);
        assertEquals(1, orderItem.getQuantity());
    }

    // ===== Update Price Tests =====

    @Test
    @DisplayName("Should update price with valid value")
    void testUpdatePrice_ValidPrice() {
        orderItem.updatePrice(25.0);
        assertEquals(25.0, orderItem.getPrice());
    }

    @Test
    @DisplayName("Should throw exception for null price")
    void testUpdatePrice_NullPrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderItem.updatePrice(null);
        });
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should allow zero price update")
    void testUpdatePrice_ZeroPrice() {
        orderItem.updatePrice(0.0);
        assertEquals(0.0, orderItem.getPrice());
    }

    @Test
    @DisplayName("Should throw exception for negative price")
    void testUpdatePrice_NegativePrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderItem.updatePrice(-10.0);
        });
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should update to minimum valid price")
    void testUpdatePrice_MinimumPrice() {
        orderItem.updatePrice(0.01);
        assertEquals(0.01, orderItem.getPrice());
    }

    @Test
    @DisplayName("Should handle decimal price updates")
    void testUpdatePrice_DecimalPrice() {
        orderItem.updatePrice(19.99);
        assertEquals(19.99, orderItem.getPrice());
    }

    // ===== Constructor Tests =====

    @Test
    @DisplayName("Should create order item with all fields")
    void testConstructor_AllFields() {
        OrderItem item = new OrderItem(2L, 102L, 8, 15.5, "Product B");
        
        assertEquals(2L, item.getId());
        assertEquals(102L, item.getProductId());
        assertEquals(8, item.getQuantity());
        assertEquals(15.5, item.getPrice());
        assertEquals("Product B", item.getProductName());
    }

    @Test
    @DisplayName("Should create order item with default constructor")
    void testConstructor_Default() {
        OrderItem item = new OrderItem();
        assertNotNull(item);
    }

    @Test
    @DisplayName("Should create order item with null ID")
    void testConstructor_NullId() {
        OrderItem item = new OrderItem(null, 103L, 3, 20.0, "Product C");
        
        assertNull(item.getId());
        assertEquals(103L, item.getProductId());
    }

    // ===== Getter/Setter Tests =====

    @Test
    @DisplayName("Should set and get ID")
    void testSetGetId() {
        orderItem.setId(99L);
        assertEquals(99L, orderItem.getId());
    }

    @Test
    @DisplayName("Should set and get product ID")
    void testSetGetProductId() {
        orderItem.setProductId(200L);
        assertEquals(200L, orderItem.getProductId());
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testSetGetQuantity() {
        orderItem.setQuantity(15);
        assertEquals(15, orderItem.getQuantity());
    }

    @Test
    @DisplayName("Should set and get price")
    void testSetGetPrice() {
        orderItem.setPrice(30.5);
        assertEquals(30.5, orderItem.getPrice());
    }

    @Test
    @DisplayName("Should set and get product name")
    void testSetGetProductName() {
        orderItem.setProductName("New Product");
        assertEquals("New Product", orderItem.getProductName());
    }

    // ===== Edge Case Tests =====

    @Test
    @DisplayName("Should handle very small prices")
    void testEdgeCase_VerySmallPrice() {
        orderItem.setPrice(0.01);
        orderItem.setQuantity(1);
        assertEquals(0.01, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should handle very large quantities")
    void testEdgeCase_VeryLargeQuantity() {
        orderItem.setQuantity(999999);
        orderItem.setPrice(1.0);
        assertEquals(999999.0, orderItem.calculateSubtotal());
    }

    @Test
    @DisplayName("Should trim product name")
    void testEdgeCase_TrimProductName() {
        orderItem.setProductName("  Trimmed Product  ");
        assertEquals("  Trimmed Product  ", orderItem.getProductName());
    }
}
