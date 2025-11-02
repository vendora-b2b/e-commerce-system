package com.example.ecommerce.marketplace.infrastructure.quotation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataQuotationOfferRepository extends JpaRepository<QuotationOfferEntity, Long> {
    /* Find all quotation offers by the associated quotation request ID */
    List<QuotationOfferEntity> findByQuotationRequestId(Long requestId);
    
    /* Find all quotation offers by the associated supplier ID */
    List<QuotationOfferEntity> findBySupplierId(Long supplierId);
}
