package com.example.ecommerce.marketplace.application.order;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import com.example.ecommerce.marketplace.domain.product.PriceTier;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case for placing a new order in the marketplace.
 * Handles validation, inventory reservation, price calculation, and order creation.
 * Follows API specification behavior steps 1-14.
 */
@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final RetailerRepository retailerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Executes the place order use case following API spec behavior steps.
     *
     * @param command the order command containing order data
     * @return the result indicating success or failure with details
     */
    @Transactional
    public PlaceOrderResult execute(PlaceOrderCommand command) {
        // Step 1: Validate orderNumber is unique
        if (orderRepository.existsByOrderNumber(command.getOrderNumber())) {
            return PlaceOrderResult.failure("Order number already exists", "ORDER_NUMBER_EXISTS");
        }

        // Step 2: Validate retailerId exists
        if (!retailerRepository.findById(command.getRetailerId()).isPresent()) {
            return PlaceOrderResult.failure("Retailer not found", "RETAILER_NOT_FOUND");
        }

        // Step 3: Validate supplierId exists
        if (!supplierRepository.findById(command.getSupplierId()).isPresent()) {
            return PlaceOrderResult.failure("Supplier not found", "SUPPLIER_NOT_FOUND");
        }

        // Step 4: Validate orderItems is not empty
        if (command.getOrderItems() == null || command.getOrderItems().isEmpty()) {
            return PlaceOrderResult.failure("Order items cannot be empty", "EMPTY_ORDER_ITEMS");
        }

        // Step 5: For each orderItem - validate basic constraints
        for (PlaceOrderCommand.OrderItemCommand itemCommand : command.getOrderItems()) {
            // Validate variantId exists
            if (itemCommand.getVariantId() == null) {
                return PlaceOrderResult.failure("Variant ID is required", "INVALID_VARIANT_ID");
            }
            if (!productVariantRepository.findById(itemCommand.getVariantId()).isPresent()) {
                return PlaceOrderResult.failure(
                    "Variant not found with ID: " + itemCommand.getVariantId(),
                    "VARIANT_NOT_FOUND"
                );
            }

            // Validate quantity is positive
            if (itemCommand.getQuantity() == null || itemCommand.getQuantity() <= 0) {
                return PlaceOrderResult.failure("Quantity must be positive", "INVALID_QUANTITY");
            }

            // Validate price is positive
            if (itemCommand.getPrice() == null || itemCommand.getPrice() < 0) {
                return PlaceOrderResult.failure("Price cannot be negative", "INVALID_PRICE");
            }
        }

        // Step 6: BEGIN TRANSACTION (handled by @Transactional)

        // Step 7: For each orderItem - process inventory and calculate prices
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (PlaceOrderCommand.OrderItemCommand itemCommand : command.getOrderItems()) {
            // Query ProductVariant to get productId, color, size
            ProductVariant variant = productVariantRepository.findById(itemCommand.getVariantId())
                .orElseThrow(() -> new IllegalStateException("Variant not found")); // Should not happen, already validated

            Long productId = variant.getProductId();

            // Query Product to get basePrice and priceTiers
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found for variant"));

            // Query Inventory for variantId
            Inventory inventory = inventoryRepository.findByVariantId(itemCommand.getVariantId())
                .orElse(null);

            // If inventory not found, try fallback to productId + supplierId
            if (inventory == null) {
                inventory = inventoryRepository.findBySupplierIdAndProductId(
                    command.getSupplierId(),
                    productId
                ).orElse(null);
            }

            if (inventory == null) {
                return PlaceOrderResult.failure(
                    "Variant not stocked by supplier",
                    "VARIANT_NOT_STOCKED"
                );
            }

            // Calculate available stock
            int availableStock = inventory.getAvailableQuantity();

            // Check if quantity > available stock
            if (itemCommand.getQuantity() > availableStock) {
                return PlaceOrderResult.failure(
                    "Insufficient stock for variant ID: " + itemCommand.getVariantId(),
                    "INSUFFICIENT_STOCK"
                );
            }

            // Update Inventory: reserve stock (SELECT FOR UPDATE is handled internally by reserveStock)
            try {
                inventory.reserveStock(itemCommand.getQuantity());
                inventoryRepository.save(inventory);
            } catch (IllegalStateException e) {
                return PlaceOrderResult.failure(e.getMessage(), "INSUFFICIENT_STOCK");
            }

            // Calculate price: ((basePrice + priceAdjustment) × quantity) × (1 - priceTier.discount)
            Double basePrice = product.getBasePrice();
            Double priceAdjustment = variant.getPriceAdjustment() != null ? variant.getPriceAdjustment() : 0.0;
            Double unitPrice = basePrice + priceAdjustment;

            // Find applicable price tier based on quantity
            PriceTier applicableTier = product.getPriceTierForQuantity(itemCommand.getQuantity());
            Double discount = 0.0;
            if (applicableTier != null && applicableTier.getDiscountPercent() != null) {
                discount = applicableTier.getDiscountPercent() / 100.0;
            }

            // Calculate final price
            Double calculatedPrice = (unitPrice * itemCommand.getQuantity()) * (1 - discount);
            // Round to 2 decimal places
            calculatedPrice = Math.round(calculatedPrice * 100.0) / 100.0;

            // Create OrderItem with all fields
            OrderItem orderItem = new OrderItem(
                null, // ID will be generated
                productId,
                itemCommand.getVariantId(),
                itemCommand.getQuantity(),
                calculatedPrice, // Use calculated price
                itemCommand.getProductName()
            );

            orderItems.add(orderItem);
            totalAmount += calculatedPrice;
        }

        // Step 8: Calculate totalAmount (already done in loop)
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;

        // Step 9: Set orderDate to current timestamp if null
        LocalDateTime orderDate = command.getOrderDate();
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }

        // Step 10: Create Order record with status = PENDING
        Order order = new Order(
            null, // ID will be generated
            command.getOrderNumber(),
            command.getRetailerId(),
            command.getSupplierId(),
            orderItems,
            totalAmount,
            OrderStatus.PENDING,
            command.getShippingAddress(),
            orderDate,
            null // Delivery date is null initially
        );

        // Step 11: Create OrderItem records (done above)

        // Step 12: Save order (COMMIT TRANSACTION handled by @Transactional)
        Order savedOrder = orderRepository.save(order);

        // Step 13: Return 201 CREATED with complete order details
        return PlaceOrderResult.success(savedOrder);
    }
}
