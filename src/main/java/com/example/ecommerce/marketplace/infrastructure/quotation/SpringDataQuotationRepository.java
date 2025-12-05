package com.example.ecommerce.marketplace.infrastructure.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for QuotationEntity.
 */
public interface SpringDataQuotationRepository extends JpaRepository<QuotationEntity, Long> {
    
    List<QuotationEntity> findByRetailerId(Long retailerId);
    
    List<QuotationEntity> findBySupplierId(Long supplierId);
    
    @Query("SELECT q FROM QuotationEntity q WHERE " +
           "(:retailerId IS NULL OR q.retailerId = :retailerId) AND " +
           "(:supplierId IS NULL OR q.supplierId = :supplierId) AND " +
           "(:status IS NULL OR q.status = :status)")
    Page<QuotationEntity> findByFilter(
            @Param("retailerId") Long retailerId,
            @Param("supplierId") Long supplierId,
            @Param("status") QuotationStatus status,
            Pageable pageable
    );
    
    @Query("SELECT q FROM QuotationEntity q WHERE " +
           "(:retailerId IS NULL OR q.retailerId = :retailerId) AND " +
           "(:supplierId IS NULL OR q.supplierId = :supplierId) AND " +
           "(:dateFrom IS NULL OR q.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR q.createdAt <= :dateTo)")
    List<QuotationEntity> findForStatistics(
            @Param("retailerId") Long retailerId,
            @Param("supplierId") Long supplierId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );
    
    @Query("SELECT COUNT(q) FROM QuotationEntity q WHERE " +
           "(:retailerId IS NULL OR q.retailerId = :retailerId) AND " +
           "(:supplierId IS NULL OR q.supplierId = :supplierId) AND " +
           "q.status = :status AND " +
           "(:dateFrom IS NULL OR q.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR q.createdAt <= :dateTo)")
    long countByStatus(
            @Param("retailerId") Long retailerId,
            @Param("supplierId") Long supplierId,
            @Param("status") QuotationStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );
}
