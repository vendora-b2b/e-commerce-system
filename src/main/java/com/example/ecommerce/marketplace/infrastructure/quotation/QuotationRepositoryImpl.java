package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of QuotationRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class QuotationRepositoryImpl implements QuotationRepository {
    
    private final SpringDataQuotationRequestRepository requestRepository;
    private final SpringDataQuotationOfferRepository offerRepository;
    private final QuotationMapper mapper;

    @Override
    public QuotationRequest saveQuotationRequest(QuotationRequest request) {
        QuotationRequestEntity entity = mapper.toEntity(request);
        QuotationRequestEntity savedEntity = requestRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public QuotationOffer saveQuotationOffer(QuotationOffer offer) {
        QuotationOfferEntity entity = mapper.toEntity(offer);
        QuotationOfferEntity savedEntity = offerRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public QuotationRequest findRequestById(Long id) {
        return requestRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public QuotationOffer findOfferById(Long id) {
        return offerRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<QuotationRequest> findRequestsByRetailerId(Long retailerId) {
        return requestRepository.findByRetailerId(retailerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationOffer> findOffersByRequestId(Long requestId) {
        return offerRepository.findByQuotationRequestId(requestId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationOffer> findOffersBySupplierId(Long supplierId) {
        return offerRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}