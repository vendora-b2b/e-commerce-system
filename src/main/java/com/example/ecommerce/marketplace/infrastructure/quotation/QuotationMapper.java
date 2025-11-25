package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QuotationMapper {
    
    public QuotationRequestEntity toEntity(QuotationRequest domain) {
        if (domain == null) {
            return null;
        }
        
        QuotationRequestEntity entity = new QuotationRequestEntity();
        entity.setId(domain.getId());
        entity.setRequestNumber(domain.getRequestNumber());
        entity.setRetailerId(domain.getRetailerId());
        entity.setSupplierId(domain.getSupplierId());
        entity.setStatus(domain.getStatus());
        entity.setRequestDate(domain.getRequestDate());
        entity.setValidUntil(domain.getValidUntil());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        
        entity.setRequestItems(domain.getRequestItems().stream()
                .map(this::toEntity)
                .collect(Collectors.toList()));
                
        return entity;
    }
    
    public QuotationRequest toDomain(QuotationRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        
        QuotationRequest.Builder builder = QuotationRequest.builder()
                .requestNumber(entity.getRequestNumber())
                .retailerId(entity.getRetailerId())
                .supplierId(entity.getSupplierId())
                .validUntil(entity.getValidUntil())
                .notes(entity.getNotes());
                
        entity.getRequestItems().forEach(item -> 
            builder.addRequestItem(
                item.getProductId(),
                item.getVariantId(),
                item.getQuantity(),
                item.getSpecifications()
            )
        );
        
        QuotationRequest domain = builder.build();
        // Set fields that are not parts of the builder
        field(domain, "id", entity.getId());
        field(domain, "status", entity.getStatus());
        field(domain, "requestDate", entity.getRequestDate());
        field(domain, "createdAt", entity.getCreatedAt());
        
        return domain;
    }
    
    public QuotationOfferEntity toEntity(QuotationOffer domain) {
        if (domain == null) {
            return null;
        }
        
        QuotationOfferEntity entity = new QuotationOfferEntity();
        entity.setId(domain.getId());
        entity.setOfferNumber(domain.getOfferNumber());
        entity.setQuotationRequestId(domain.getQuotationRequestId());
        entity.setRetailerId(domain.getRetailerId());
        entity.setSupplierId(domain.getSupplierId());
        entity.setStatus(domain.getStatus());
        entity.setOfferDate(domain.getOfferDate());
        entity.setValidUntil(domain.getValidUntil());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setNotes(domain.getNotes());
        entity.setTermsAndConditions(domain.getTermsAndConditions());
        entity.setCreatedAt(domain.getCreatedAt());
        
        entity.setOfferItems(domain.getOfferItems().stream()
                .map(this::toEntity)
                .collect(Collectors.toList()));
                
        return entity;
    }
    
    public QuotationOffer toDomain(QuotationOfferEntity entity) {
        if (entity == null) return null;
        
        QuotationOffer.Builder builder = QuotationOffer.builder()
                .offerNumber(entity.getOfferNumber())
                .quotationRequestId(entity.getQuotationRequestId())
                .retailerId(entity.getRetailerId())
                .supplierId(entity.getSupplierId())
                .validUntil(entity.getValidUntil())
                .notes(entity.getNotes())
                .termsAndConditions(entity.getTermsAndConditions());
                
        entity.getOfferItems().forEach(item -> 
            builder.addOfferItem(
                item.getProductId(),
                item.getVariantId(),
                item.getQuantity(),
                item.getQuotedPrice(),
                item.getSpecifications(),
                item.getNotes()
            )
        );
        
        QuotationOffer domain = builder.build();
        // Set fields that aren't part of the builder
        field(domain, "id", entity.getId());
        field(domain, "status", entity.getStatus());
        field(domain, "offerDate", entity.getOfferDate());
        field(domain, "totalAmount", entity.getTotalAmount());
        field(domain, "createdAt", entity.getCreatedAt());
        
        return domain;
    }
    
    private QuotationRequestItemEntity toEntity(QuotationRequest.QuotationRequestItem domain) {
        if (domain == null) {
            return null;
        }

        QuotationRequestItemEntity entity = new QuotationRequestItemEntity();
        entity.setProductId(domain.getProductId());
        entity.setVariantId(domain.getVariantId());
        entity.setQuantity(domain.getQuantity());
        entity.setSpecifications(domain.getSpecifications());
        return entity;
    }
    
    private QuotationOfferItemEntity toEntity(QuotationOffer.QuotationOfferItem domain) {
        if (domain == null) {
            return null;
        }

        QuotationOfferItemEntity entity = new QuotationOfferItemEntity();
        entity.setProductId(domain.getProductId());
        entity.setVariantId(domain.getVariantId());
        entity.setQuantity(domain.getQuantity());
        entity.setQuotedPrice(domain.getQuotedPrice());
        entity.setSpecifications(domain.getSpecifications());
        entity.setNotes(domain.getNotes());
        return entity;
    }
    
    // Utility method to set private fields using reflection
    private void field(Object obj, String fieldName, Object value) {
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
