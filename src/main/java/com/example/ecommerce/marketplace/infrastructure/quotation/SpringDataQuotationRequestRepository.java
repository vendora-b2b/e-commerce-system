package com.example.ecommerce.marketplace.infrastructure.quotation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataQuotationRequestRepository extends JpaRepository<QuotationRequestEntity, Long> {

    /* Find all quotation requests by the associated retailer ID */
    List<QuotationRequestEntity> findByRetailerId(Long retailerId);
}
