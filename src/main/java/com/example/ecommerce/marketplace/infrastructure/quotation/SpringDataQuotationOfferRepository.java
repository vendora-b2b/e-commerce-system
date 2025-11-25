package com.example.ecommerce.marketplace.infrastructure.quotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SpringDataQuotationOfferRepository extends JpaRepository<QuotationOfferEntity, Long> {
    /* Find all quotation offers by the associated quotation request ID */
    List<QuotationOfferEntity> findByQuotationRequestId(Long requestId);
    
    /* Find all quotation offers by the associated supplier ID */
    List<QuotationOfferEntity> findBySupplierId(Long supplierId);
    
    /* Find quotation offers with filtering and pagination */
    @Query("SELECT qo FROM QuotationOfferEntity qo WHERE " +
           "(:requestId IS NULL OR qo.quotationRequestId = :requestId) AND " +
           "(:supplierId IS NULL OR qo.supplierId = :supplierId) AND " +
           "(:status IS NULL OR qo.status = :status)")
    Page<QuotationOfferEntity> findOffersByFilter(
            @Param("requestId") Long requestId,
            @Param("supplierId") Long supplierId,
            @Param("status") String status,
            Pageable pageable);
}
