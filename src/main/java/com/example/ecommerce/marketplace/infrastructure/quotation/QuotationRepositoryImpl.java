package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of QuotationRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class QuotationRepositoryImpl implements QuotationRepository {
    
    private final SpringDataQuotationRepository jpaRepository;
    private final QuotationMapper mapper;
    
    @Override
    public Quotation save(Quotation quotation) {
        QuotationEntity entity = mapper.toEntity(quotation);
        QuotationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Quotation findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }
    
    @Override
    public Page<Quotation> findByFilter(Long retailerId, Long supplierId, 
                                       QuotationStatus status, Pageable pageable) {
        Page<QuotationEntity> entityPage = jpaRepository.findByFilter(
                retailerId, supplierId, status, pageable);
        return entityPage.map(mapper::toDomain);
    }
    
    @Override
    public List<Quotation> findByRetailerId(Long retailerId) {
        return jpaRepository.findByRetailerId(retailerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Quotation> findBySupplierId(Long supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Quotation> findForStatistics(Long retailerId, Long supplierId, 
                                            LocalDateTime dateFrom, LocalDateTime dateTo) {
        return jpaRepository.findForStatistics(retailerId, supplierId, dateFrom, dateTo).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByStatus(Long retailerId, Long supplierId, QuotationStatus status, 
                             LocalDateTime dateFrom, LocalDateTime dateTo) {
        return jpaRepository.countByStatus(retailerId, supplierId, status, dateFrom, dateTo);
    }
}
