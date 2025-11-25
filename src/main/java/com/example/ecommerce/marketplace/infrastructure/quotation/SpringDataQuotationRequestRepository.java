package com.example.ecommerce.marketplace.infrastructure.quotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SpringDataQuotationRequestRepository extends JpaRepository<QuotationRequestEntity, Long> {

    /* Find all quotation requests by the associated retailer ID */
    List<QuotationRequestEntity> findByRetailerId(Long retailerId);
    
    /* Find quotation requests with filtering and pagination */
    @Query("SELECT qr FROM QuotationRequestEntity qr WHERE " +
           "(:retailerId IS NULL OR qr.retailerId = :retailerId) AND " +
           "(:supplierId IS NULL OR qr.supplierId = :supplierId) AND " +
           "(:status IS NULL OR qr.status = :status)")
    Page<QuotationRequestEntity> findRequestsByFilter(
            @Param("retailerId") Long retailerId,
            @Param("supplierId") Long supplierId,
            @Param("status") String status,
            Pageable pageable);
}
