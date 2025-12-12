package com.example.ecommerce.marketplace.application.report;

import com.example.ecommerce.marketplace.domain.order.Order;
import com.example.ecommerce.marketplace.domain.order.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service to generate CSV reports using Apache Commons CSV.
 */
@Service
@Slf4j
public class CsvReportGenerator {

    //private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generate(List<Order> orders, String entityName, String entityType,
                          LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("Date", "Order ID", entityType.equals("SUPPLIER") ? "Retailer ID" : "Supplier ID",
                               "Status", "Product Name", "Quantity", "Unit Price", "Line Total", "Order Total")
                     .build())) {

            // Sort orders chronologically (oldest to newest)
            orders.sort((o1, o2) -> o1.getOrderDate().compareTo(o2.getOrderDate()));

            for (Order order : orders) {
                if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                    // Write order without items
                    csvPrinter.printRecord(
                        order.getOrderDate() != null ? order.getOrderDate().format(DATETIME_FORMATTER) : "",
                        order.getOrderNumber() != null ? order.getOrderNumber() : "",
                        entityType.equals("SUPPLIER") ? order.getRetailerId() : order.getSupplierId(),
                        order.getStatus() != null ? order.getStatus().name() : "",
                        "",
                        0,
                        0.0,
                        0.0,
                        order.getTotalAmount() != null ? order.getTotalAmount() : 0.0
                    );
                } else {
                    // Write each order item as a separate row
                    boolean firstItem = true;
                    for (OrderItem item : order.getOrderItems()) {
                        csvPrinter.printRecord(
                            firstItem && order.getOrderDate() != null ? 
                                order.getOrderDate().format(DATETIME_FORMATTER) : "",
                            firstItem && order.getOrderNumber() != null ? order.getOrderNumber() : "",
                            firstItem ? (entityType.equals("SUPPLIER") ? 
                                order.getRetailerId() : order.getSupplierId()) : "",
                            firstItem && order.getStatus() != null ? order.getStatus().name() : "",
                            item.getProductName() != null ? item.getProductName() : "",
                            item.getQuantity() != null ? item.getQuantity() : 0,
                            item.getPrice() != null ? item.getPrice() : 0.0,
                            item.getPrice() != null && item.getQuantity() != null ? 
                                item.getPrice() * item.getQuantity() : 0.0,
                            firstItem && order.getTotalAmount() != null ? order.getTotalAmount() : ""
                        );
                        firstItem = false;
                    }
                }
            }

            csvPrinter.flush();
            return outputStream.toByteArray();
        }
    }
}
