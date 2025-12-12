package com.example.ecommerce.marketplace.application.report;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.order.OrderStatus;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for generating financial reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateReportUseCase {

    private final OrderRepository orderRepository;
    private final RetailerRepository retailerRepository;
    private final SupplierRepository supplierRepository;
    private final PdfReportGenerator pdfReportGenerator;
    private final CsvReportGenerator csvReportGenerator;

    public GenerateReportResult execute(GenerateReportCommand command) {
        try {
            // Validate entity exists
            String entityName;
            if ("RETAILER".equals(command.entityType())) {
                Optional<Retailer> retailer = retailerRepository.findById(command.entityId());
                if (retailer.isEmpty()) {
                    return GenerateReportResult.failure("RETAILER_NOT_FOUND", "Retailer not found");
                }
                entityName = retailer.get().getName();
            } else if ("SUPPLIER".equals(command.entityType())) {
                Optional<Supplier> supplier = supplierRepository.findById(command.entityId());
                if (supplier.isEmpty()) {
                    return GenerateReportResult.failure("SUPPLIER_NOT_FOUND", "Supplier not found");
                }
                entityName = supplier.get().getName();
            } else {
                return GenerateReportResult.failure("INVALID_ENTITY_TYPE", "Entity type must be RETAILER or SUPPLIER");
            }

            // Fetch orders
            List<Order> orders;
            if ("RETAILER".equals(command.entityType())) {
                orders = orderRepository.findByRetailerId(command.entityId());
            } else {
                orders = orderRepository.findBySupplierId(command.entityId());
            }

            // Filter by date range
            final LocalDateTime start = command.startDate();
            final LocalDateTime end = command.endDate();
            orders = orders.stream()
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    if (orderDate == null) return false;
                    
                    if (start != null && end != null) {
                        return !orderDate.isBefore(start) && !orderDate.isAfter(end);
                    } else if (start != null) {
                        return !orderDate.isBefore(start);
                    } else if (end != null) {
                        return !orderDate.isAfter(end);
                    }
                    return true;
                })
                .collect(Collectors.toList());

            // Filter by status
            orders = orders.stream()
                .filter(order -> {
                    OrderStatus status = order.getStatus();
                    if (status == OrderStatus.CANCELLED) {
                        return command.includeCancelled();
                    }
                    if (status == OrderStatus.DELIVERED) {
                        return command.includeCompleted();
                    }
                    // Include all other statuses if includeCompleted is true
                    return command.includeCompleted();
                })
                .collect(Collectors.toList());

            // Generate report based on type
            byte[] reportData;
            if ("CSV".equalsIgnoreCase(command.type())) {
                reportData = csvReportGenerator.generate(orders, entityName, command.entityType(), 
                    command.startDate(), command.endDate());
            } else {
                reportData = pdfReportGenerator.generate(orders, entityName, command.entityType(), 
                    command.startDate(), command.endDate());
            }

            return GenerateReportResult.success(reportData);

        } catch (Exception e) {
            log.error("Error generating report", e);
            return GenerateReportResult.failure("REPORT_GENERATION_ERROR", e.getMessage());
        }
    }
}
