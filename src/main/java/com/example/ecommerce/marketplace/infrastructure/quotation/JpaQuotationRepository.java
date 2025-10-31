package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class JpaQuotationRepository implements QuotationRepository {
    private final SpringDataQuotationRequestRepository requestRepository;
    private final SpringDataQuotationOfferRepository offerRepository;
    private final QuotationMapper mapper;

    public JpaQuotationRepository(
            SpringDataQuotationRequestRepository requestRepository,
            SpringDataQuotationOfferRepository offerRepository,
            QuotationMapper mapper) {
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
        this.mapper = mapper;
    }

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
        List<QuotationRequestEntity> entities = requestRepository.findByRetailerId(retailerId);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationOffer> findOffersByRequestId(Long requestId) {
        List<QuotationOfferEntity> entities = offerRepository.findByQuotationRequestId(requestId);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationOffer> findOffersBySupplierId(Long supplierId) {
        List<QuotationOfferEntity> entities = offerRepository.findBySupplierId(supplierId);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}