package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for creating quotations.
 * Handles grouping items by supplier and creating multiple quotations if needed.
 */
@Service
@RequiredArgsConstructor
public class CreateQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    
    @Transactional
    public CreateQuotationResult execute(Long retailerId, CreateQuotationCommand command) {
        // Validate inputs
        System.out.println("=== CREATE QUOTATION DEBUG ===");
        System.out.println("Command items: " + command.getItems());
        System.out.println("Items size: " + (command.getItems() != null ? command.getItems().size() : "null"));
        
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
        
        // Validate retailer exists
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new IllegalArgumentException("Retailer not found: " + retailerId));
        
        // Group items by supplier (query variant->product->supplier for each item)
        Map<Long, List<EnrichedItemRequest>> itemsBySupplier = groupItemsBySupplier(command.getItems());
        
        List<CreateQuotationResult.QuotationSummary> createdQuotations = new ArrayList<>();
        
        for (Map.Entry<Long, List<EnrichedItemRequest>> entry : itemsBySupplier.entrySet()) {
            Long supplierId = entry.getKey();
            List<EnrichedItemRequest> items = entry.getValue();
            
            // Fetch supplier
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));
            
            // Generate quotation number
            String quotationNumber = generateQuotationNumber();
            
            // Build quotation
            Quotation.Builder builder = Quotation.builder()
                    .quotationNumber(quotationNumber)
                    .retailerId(retailerId)
                    .supplierId(supplierId)
                    .retailerNotes(command.getNotes())
                    .validUntil(LocalDateTime.now().plusDays(30));
            
            // Add items with productId set
            System.out.println("Adding items to builder. Items count: " + items.size());
            for (EnrichedItemRequest item : items) {
                System.out.println("Adding item - variantId: " + item.variantId + ", productId: " + item.productId + ", qty: " + item.requestedQuantity);
                builder.addItem(
                        item.variantId,
                        item.productId,      // Add productId parameter
                        item.requestedQuantity,
                        item.targetPrice,
                        item.deliveryDate,
                        item.notes
                );
            }

            System.out.println("Building quotation...");
            Quotation quotation = builder.build();  // ProductId already set!
            
            Quotation savedQuotation = quotationRepository.save(quotation);
            
            createdQuotations.add(new CreateQuotationResult.QuotationSummary(
                    savedQuotation.getId(),
                    savedQuotation.getQuotationNumber(),
                    savedQuotation.getSupplierId(),
                    supplier.getName(),
                    savedQuotation.getItemCount(),
                    savedQuotation.getStatus().name()
            ));
        }
        
        String message = String.format("Created %d quotation(s) for %d supplier(s)", 
                createdQuotations.size(), itemsBySupplier.size());
        
        return new CreateQuotationResult(createdQuotations, message);
    }
    
    private Map<Long, List<EnrichedItemRequest>> groupItemsBySupplier(
            List<CreateQuotationCommand.QuotationItemRequest> items) {
        // Query variant->product->supplier for each item and enrich the data
        List<EnrichedItemRequest> enrichedItems = items.stream()
                .map(item -> {
                    // Fetch variant
                    ProductVariant variant = variantRepository.findById(item.getVariantId())
                            .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + item.getVariantId()));
                    
                    // Fetch product
                    Product product = productRepository.findById(variant.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + variant.getProductId()));
                    
                    // Validate item data
                    if (item.getRequestedQuantity() <= 0) {
                        throw new IllegalArgumentException("Requested quantity must be greater than 0");
                    }
                    if (item.getTargetPrice() != null && item.getTargetPrice() <= 0) {
                        throw new IllegalArgumentException("Target price must be positive");
                    }
                    if (item.getDeliveryDate() != null && item.getDeliveryDate().isBefore(java.time.LocalDate.now())) {
                        throw new IllegalArgumentException("Delivery date cannot be in the past");
                    }
                    
                    return new EnrichedItemRequest(
                            item.getVariantId(),
                            product.getId(),
                            product.getSupplierId(),
                            item.getRequestedQuantity(),
                            item.getTargetPrice(),
                            item.getDeliveryDate(),
                            item.getNotes()
                    );
                })
                .collect(Collectors.toList());
        
        // Group by supplier ID
        return enrichedItems.stream()
                .collect(Collectors.groupingBy(item -> item.supplierId));
    }
    
    private String generateQuotationNumber() {
        // Simple implementation - in production, use a sequence or UUID
        return "QT-" + System.currentTimeMillis();
    }
    
    // Helper class to hold enriched item data
    private static class EnrichedItemRequest {
        final Long variantId;
        final Long productId;
        final Long supplierId;
        final Integer requestedQuantity;
        final Double targetPrice;
        final java.time.LocalDate deliveryDate;
        final String notes;
        
        EnrichedItemRequest(Long variantId, Long productId, Long supplierId,
                          Integer requestedQuantity, Double targetPrice,
                          java.time.LocalDate deliveryDate, String notes) {
            this.variantId = variantId;
            this.productId = productId;
            this.supplierId = supplierId;
            this.requestedQuantity = requestedQuantity;
            this.targetPrice = targetPrice;
            this.deliveryDate = deliveryDate;
            this.notes = notes;
        }
    }
}
