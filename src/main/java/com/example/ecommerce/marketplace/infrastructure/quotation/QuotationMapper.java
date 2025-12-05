package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

/**
 * Mapper between Quotation domain entity and QuotationEntity JPA entity.
 */
@Component
public class QuotationMapper {
    
    public QuotationEntity toEntity(Quotation domain) {
        if (domain == null) {
            return null;
        }
        
        QuotationEntity entity = new QuotationEntity();
        entity.setId(domain.getId());
        entity.setQuotationNumber(domain.getQuotationNumber());
        entity.setRetailerId(domain.getRetailerId());
        entity.setSupplierId(domain.getSupplierId());
        entity.setStatus(domain.getStatus());
        entity.setRetailerNotes(domain.getRetailerNotes());
        entity.setSupplierNotes(domain.getSupplierNotes());
        entity.setTermsAndConditions(domain.getTermsAndConditions());
        entity.setValidUntil(domain.getValidUntil());
        entity.setOrderId(domain.getOrderId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setRespondedAt(domain.getRespondedAt());
        entity.setFinalizedAt(domain.getFinalizedAt());
        entity.setCancelledAt(domain.getCancelledAt());
        entity.setCancellationReason(domain.getCancellationReason());
        
        entity.setItems(domain.getItems().stream()
                .map(this::toItemEntity)
                .collect(Collectors.toList()));
        
        return entity;
    }
    
    public Quotation toDomain(QuotationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Quotation.Builder builder = Quotation.builder()
                .quotationNumber(entity.getQuotationNumber())
                .retailerId(entity.getRetailerId())
                .supplierId(entity.getSupplierId())
                .retailerNotes(entity.getRetailerNotes())
                .validUntil(entity.getValidUntil());
        
        // Convert items FIRST, then add them to the builder
        for (QuotationItemEntity itemEntity : entity.getItems()) {
            builder.addItem(
                    itemEntity.getVariantId(),
                    itemEntity.getProductId(),
                    itemEntity.getRequestedQuantity(),
                    itemEntity.getTargetPrice(),
                    itemEntity.getRequestedDeliveryDate(),
                    itemEntity.getRetailerNotes()
            );
        }
        
        // Now build with items present
        Quotation domain = builder.build();
        
        // Set fields that aren't part of the builder using reflection
        setField(domain, "id", entity.getId());
        setField(domain, "status", entity.getStatus());
        setField(domain, "supplierNotes", entity.getSupplierNotes());
        setField(domain, "termsAndConditions", entity.getTermsAndConditions());
        setField(domain, "orderId", entity.getOrderId());
        setField(domain, "createdAt", entity.getCreatedAt());
        setField(domain, "respondedAt", entity.getRespondedAt());
        setField(domain, "finalizedAt", entity.getFinalizedAt());
        setField(domain, "cancelledAt", entity.getCancelledAt());
        setField(domain, "cancellationReason", entity.getCancellationReason());
        
        // Set additional item fields that weren't in the constructor
        java.util.List<Quotation.QuotationItem> domainItems = domain.getItems();
        for (int i = 0; i < domainItems.size(); i++) {
            Quotation.QuotationItem item = domainItems.get(i);
            QuotationItemEntity itemEntity = entity.getItems().get(i);
            
            setField(item, "id", itemEntity.getId());
            setField(item, "itemStatus", itemEntity.getItemStatus());
            setField(item, "offeredQuantity", itemEntity.getOfferedQuantity());
            setField(item, "offeredPrice", itemEntity.getOfferedPrice());
            setField(item, "offeredDeliveryDate", itemEntity.getOfferedDeliveryDate());
            setField(item, "leadTimeDays", itemEntity.getLeadTimeDays());
            setField(item, "supplierNotes", itemEntity.getSupplierNotes());
            setField(item, "rejectionReason", itemEntity.getRejectionReason());
            setField(item, "retailerAction", itemEntity.getRetailerAction());
        }
        
        return domain;
    }
    
    private QuotationItemEntity toItemEntity(Quotation.QuotationItem domain) {
        if (domain == null) {
            return null;
        }
        
        QuotationItemEntity entity = new QuotationItemEntity();
        entity.setId(domain.getId());
        entity.setVariantId(domain.getVariantId());
        entity.setProductId(domain.getProductId());
        entity.setRequestedQuantity(domain.getRequestedQuantity());
        entity.setTargetPrice(domain.getTargetPrice());
        entity.setRequestedDeliveryDate(domain.getRequestedDeliveryDate());
        entity.setRetailerNotes(domain.getRetailerNotes());
        entity.setItemStatus(domain.getItemStatus());
        entity.setOfferedQuantity(domain.getOfferedQuantity());
        entity.setOfferedPrice(domain.getOfferedPrice());
        entity.setOfferedDeliveryDate(domain.getOfferedDeliveryDate());
        entity.setLeadTimeDays(domain.getLeadTimeDays());
        entity.setSupplierNotes(domain.getSupplierNotes());
        entity.setRejectionReason(domain.getRejectionReason());
        entity.setRetailerAction(domain.getRetailerAction());
        
        return entity;
    }
    
    private Quotation.QuotationItem toItemDomain(QuotationItemEntity entity, Long quotationId) {
        if (entity == null) {
            return null;
        }
        
        // Create a QuotationItem instance using reflection since constructor is private
        try {
            Class<?> itemClass = Quotation.QuotationItem.class;
            java.lang.reflect.Constructor<?> constructor = itemClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            
            Quotation.QuotationItem item = (Quotation.QuotationItem) constructor.newInstance(
                    entity.getVariantId(),
                    entity.getRequestedQuantity(),
                    entity.getTargetPrice(),
                    entity.getRequestedDeliveryDate(),
                    entity.getRetailerNotes()
            );
            
            // Set fields using reflection
            setField(item, "id", entity.getId());
            setField(item, "productId", entity.getProductId());
            setField(item, "itemStatus", entity.getItemStatus());
            setField(item, "offeredQuantity", entity.getOfferedQuantity());
            setField(item, "offeredPrice", entity.getOfferedPrice());
            setField(item, "offeredDeliveryDate", entity.getOfferedDeliveryDate());
            setField(item, "leadTimeDays", entity.getLeadTimeDays());
            setField(item, "supplierNotes", entity.getSupplierNotes());
            setField(item, "rejectionReason", entity.getRejectionReason());
            setField(item, "retailerAction", entity.getRetailerAction());
            
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create QuotationItem from entity", e);
        }
    }
    
    // Utility method to set private fields using reflection
    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
    
    // Helper to find field in class hierarchy
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
