package com.example.ecommerce.marketplace.domain.quotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing quotation-related persistence operations.
 * Works with the new unified Quotation entity.
 */
public interface QuotationRepository {
    
    /**
     * Save a quotation to the repository
     * @param quotation the quotation to save
     * @return the saved quotation with generated ID
     */
    Quotation save(Quotation quotation);
    
    /**
     * Find a quotation by its ID
     * @param id the quotation ID
     * @return the found quotation or null if not found
     */
    Quotation findById(Long id);
    
    /**
     * Find quotations with pagination and filtering
     * @param retailerId filter by retailer ID (optional)
     * @param supplierId filter by supplier ID (optional)
     * @param status filter by status (optional)
     * @param pageable pagination parameters
     * @return paginated list of quotations
     */
    Page<Quotation> findByFilter(Long retailerId, Long supplierId, QuotationStatus status, Pageable pageable);
    
    /**
     * Find all quotations for a specific retailer
     * @param retailerId the retailer ID
     * @return list of quotations
     */
    List<Quotation> findByRetailerId(Long retailerId);
    
    /**
     * Find all quotations for a specific supplier
     * @param supplierId the supplier ID
     * @return list of quotations
     */
    List<Quotation> findBySupplierId(Long supplierId);
    
    /**
     * Find quotations by status and date range for statistics
     * @param retailerId filter by retailer ID (optional)
     * @param supplierId filter by supplier ID (optional)
     * @param dateFrom start date (optional)
     * @param dateTo end date (optional)
     * @return list of quotations matching criteria
     */
    List<Quotation> findForStatistics(Long retailerId, Long supplierId, LocalDateTime dateFrom, LocalDateTime dateTo);
    
    /**
     * Count quotations by status
     * @param retailerId filter by retailer ID (optional)
     * @param supplierId filter by supplier ID (optional)
     * @param status the status to count
     * @param dateFrom start date (optional)
     * @param dateTo end date (optional)
     * @return count of quotations
     */
    long countByStatus(Long retailerId, Long supplierId, QuotationStatus status, LocalDateTime dateFrom, LocalDateTime dateTo);
}
