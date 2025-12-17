package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.inventory.InventoryStatus;
import com.example.ecommerce.marketplace.domain.product.Category;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.web.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for InternalAiController.
 * Tests the internal API endpoints used by the AI service for real-time data fetching.
 */
@ExtendWith(MockitoExtension.class)
class InternalAiControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InternalAiController internalAiController;

    private Product testProduct;
    private Supplier testSupplier;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(internalAiController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

        // Create test category
        Category testCategory = new Category(1L, "Electronics", "electronics",
            LocalDateTime.now(), LocalDateTime.now());

        // Create test product
        testProduct = new Product(
            1L,
            "TEST-SKU-001",
            "Test Product",
            "A test product for unit testing",
            Arrays.asList(testCategory),
            100L,
            99.99,
            10,
            "piece",
            Arrays.asList("image1.jpg"),
            Collections.emptyList(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Create test supplier
        testSupplier = new Supplier(
            100L,
            "Test Supplier",
            "test@supplier.com",
            "1234567890",
            "123 Test Street",
            "profile.jpg",
            "A great supplier",
            "BL-12345",
            4.5,
            true
        );

        // Create test inventory using setters (no all-args constructor)
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(1L);
        testInventory.setVariantId(null);
        testInventory.setSupplierId(100L);
        testInventory.setAvailableQuantity(50);
        testInventory.setReservedQuantity(5);
        testInventory.setReorderLevel(10);
        testInventory.setReorderQuantity(20);
        testInventory.setWarehouseLocation("Warehouse A");
        testInventory.setLastRestocked(LocalDateTime.now().minusDays(7));
        testInventory.setLastUpdated(LocalDateTime.now());
        testInventory.setStatus(InventoryStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("GET /internal/ai/products/{productId}")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() throws Exception {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sku", is("TEST-SKU-001")))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.basePrice", is(99.99)))
                .andExpect(jsonPath("$.supplierId", is(100)));

            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/999"))
                .andExpect(status().isNotFound());

            verify(productRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("GET /internal/ai/products/search")
    class SearchProductsTests {

        @Test
        @DisplayName("Should return products with default pagination")
        void shouldReturnProductsWithDefaultPagination() throws Exception {
            // Arrange
            Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
            when(productRepository.findWithFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].name", is("Test Product")));

            verify(productRepository).findWithFilters(isNull(), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter by category")
        void shouldFilterByCategory() throws Exception {
            // Arrange
            Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
            when(productRepository.findWithFilters(isNull(), isNull(), eq("electronics"), any(Pageable.class)))
                .thenReturn(productPage);

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/search")
                    .param("category", "electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)));

            verify(productRepository).findWithFilters(isNull(), isNull(), eq("electronics"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter by supplier ID")
        void shouldFilterBySupplierId() throws Exception {
            // Arrange
            Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
            when(productRepository.findWithFilters(isNull(), eq(100L), isNull(), any(Pageable.class)))
                .thenReturn(productPage);

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/search")
                    .param("supplierId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)));

            verify(productRepository).findWithFilters(isNull(), eq(100L), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should limit results")
        void shouldLimitResults() throws Exception {
            // Arrange
            Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
            when(productRepository.findWithFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

            // Act & Assert
            mockMvc.perform(get("/internal/ai/products/search")
                    .param("limit", "5"))
                .andExpect(status().isOk());

            verify(productRepository).findWithFilters(any(), any(), any(), argThat(pageable -> 
                pageable.getPageSize() == 5));
        }
    }

    @Nested
    @DisplayName("POST /internal/ai/products/batch")
    class BatchGetProductsTests {

        @Test
        @DisplayName("Should return products for given IDs")
        void shouldReturnProductsForGivenIds() throws Exception {
            // Arrange
            when(productRepository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(testProduct));

            String requestBody = "{\"productIds\": [1, 2]}";

            // Act & Assert
            mockMvc.perform(post("/internal/ai/products/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].id", is(1)));

            verify(productRepository).findAllById(Arrays.asList(1L, 2L));
        }

        @Test
        @DisplayName("Should return empty list for empty product IDs")
        void shouldReturnEmptyListForEmptyProductIds() throws Exception {
            // Arrange
            String requestBody = "{\"productIds\": []}";

            // Act & Assert
            mockMvc.perform(post("/internal/ai/products/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(0)));

            verify(productRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("Should return empty list when productIds is null")
        void shouldReturnEmptyListWhenProductIdsIsNull() throws Exception {
            // Arrange
            String requestBody = "{}";

            // Act & Assert
            mockMvc.perform(post("/internal/ai/products/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(0)));

            verify(productRepository, never()).findAllById(any());
        }
    }

    @Nested
    @DisplayName("GET /internal/ai/suppliers/{supplierId}")
    class GetSupplierByIdTests {

        @Test
        @DisplayName("Should return supplier when found")
        void shouldReturnSupplierWhenFound() throws Exception {
            // Arrange
            when(supplierRepository.findById(100L)).thenReturn(Optional.of(testSupplier));

            // Act & Assert
            mockMvc.perform(get("/internal/ai/suppliers/100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Test Supplier")))
                .andExpect(jsonPath("$.email", is("test@supplier.com")))
                .andExpect(jsonPath("$.rating", is(4.5)))
                .andExpect(jsonPath("$.verified", is(true)));

            verify(supplierRepository).findById(100L);
        }

        @Test
        @DisplayName("Should return 404 when supplier not found")
        void shouldReturn404WhenSupplierNotFound() throws Exception {
            // Arrange
            when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/internal/ai/suppliers/999"))
                .andExpect(status().isNotFound());

            verify(supplierRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("GET /internal/ai/inventory/{productId}")
    class GetInventoryStatusTests {

        @Test
        @DisplayName("Should return inventory status for product")
        void shouldReturnInventoryStatusForProduct() throws Exception {
            // Arrange
            when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

            // Act & Assert
            mockMvc.perform(get("/internal/ai/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.availableQuantity", is(50)))
                .andExpect(jsonPath("$.reservedQuantity", is(5)))
                .andExpect(jsonPath("$.inStock", is(true)));

            verify(inventoryRepository).findByProductId(1L);
        }

        @Test
        @DisplayName("Should return inventory status for variant")
        void shouldReturnInventoryStatusForVariant() throws Exception {
            // Arrange
            Inventory variantInventory = new Inventory();
            variantInventory.setId(2L);
            variantInventory.setProductId(1L);
            variantInventory.setVariantId(5L);
            variantInventory.setSupplierId(100L);
            variantInventory.setAvailableQuantity(30);
            variantInventory.setReservedQuantity(3);
            variantInventory.setReorderLevel(10);
            variantInventory.setReorderQuantity(20);
            variantInventory.setWarehouseLocation("Warehouse B");
            variantInventory.setLastRestocked(LocalDateTime.now());
            variantInventory.setLastUpdated(LocalDateTime.now());
            variantInventory.setStatus(InventoryStatus.AVAILABLE);
            
            when(inventoryRepository.findByProductIdAndVariantId(1L, 5L))
                .thenReturn(Optional.of(variantInventory));

            // Act & Assert
            mockMvc.perform(get("/internal/ai/inventory/1")
                    .param("variantId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.variantId", is(5)))
                .andExpect(jsonPath("$.availableQuantity", is(30)));

            verify(inventoryRepository).findByProductIdAndVariantId(1L, 5L);
        }

        @Test
        @DisplayName("Should return 404 when inventory not found")
        void shouldReturn404WhenInventoryNotFound() throws Exception {
            // Arrange
            when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/internal/ai/inventory/999"))
                .andExpect(status().isNotFound());

            verify(inventoryRepository).findByProductId(999L);
        }

        @Test
        @DisplayName("Should indicate out of stock when quantity is zero")
        void shouldIndicateOutOfStockWhenQuantityIsZero() throws Exception {
            // Arrange
            Inventory emptyInventory = new Inventory();
            emptyInventory.setId(3L);
            emptyInventory.setProductId(2L);
            emptyInventory.setVariantId(null);
            emptyInventory.setSupplierId(100L);
            emptyInventory.setAvailableQuantity(0);
            emptyInventory.setReservedQuantity(0);
            emptyInventory.setReorderLevel(10);
            emptyInventory.setReorderQuantity(20);
            emptyInventory.setWarehouseLocation("Warehouse C");
            emptyInventory.setLastRestocked(LocalDateTime.now());
            emptyInventory.setLastUpdated(LocalDateTime.now());
            emptyInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
            when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.of(emptyInventory));

            // Act & Assert
            mockMvc.perform(get("/internal/ai/inventory/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(2)))
                .andExpect(jsonPath("$.availableQuantity", is(0)))
                .andExpect(jsonPath("$.inStock", is(false)));

            verify(inventoryRepository).findByProductId(2L);
        }
    }
}
