package com.example.ecommerce.marketplace.application.report;

import com.example.ecommerce.marketplace.domain.order.Order;
// import com.example.ecommerce.marketplace.domain.order.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
import java.util.List;

/**
 * Service to generate PDF reports using Apache PDFBox.
 */
@Service
@Slf4j
public class PdfReportGenerator {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADER = 14;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 8;
    private static final float LINE_HEIGHT = 15;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public byte[] generate(List<Order> orders, String entityName, String entityType, 
                          LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Header
            yPosition = addHeader(contentStream, yPosition, startDate, endDate, entityName, entityType);
            
            // Executive Summary
            yPosition = addExecutiveSummary(contentStream, page, yPosition, orders, document);
            
            // Financial Breakdown
            yPosition = addFinancialBreakdown(contentStream, page, yPosition, orders, entityType, document);
            
            contentStream.close();
            
            // Add page numbers
            addPageNumbers(document);
            
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private float addHeader(PDPageContentStream contentStream, float yPosition, 
                           LocalDateTime startDate, LocalDateTime endDate,
                           String entityName, String entityType) throws IOException {
        
        // Vendora Logo/Title
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("VENDORA");
        contentStream.endText();
        yPosition -= 25;

        // Financial Report Title
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADER);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Financial Report");
        contentStream.endText();
        yPosition -= 20;

        // Generated Date
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        // Period
        String periodText = "Period: ";
        if (startDate != null && endDate != null) {
            periodText += startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
        } else if (startDate != null) {
            periodText += "From " + startDate.format(DATE_FORMATTER);
        } else if (endDate != null) {
            periodText += "Until " + endDate.format(DATE_FORMATTER);
        } else {
            periodText += "All Time";
        }
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(periodText);
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        // Entity Info
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Entity: " + entityName + " (" + entityType + ")");
        contentStream.endText();
        yPosition -= 30;

        return yPosition;
    }

    private float addExecutiveSummary(PDPageContentStream contentStream, PDPage page,
                                     float yPosition, List<Order> orders, PDDocument document) throws IOException {
        
        // Check if we need a new page
        if (yPosition < 200) {
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = page.getMediaBox().getHeight() - MARGIN;
        }

        // Section Title
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADER);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("EXECUTIVE SUMMARY");
        contentStream.endText();
        yPosition -= 25;

        // Calculate metrics
        double totalRevenue = orders.stream()
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
            .sum();
        int totalOrders = orders.size();
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
        long pendingCount = orders.stream()
            .filter(o -> "PENDING".equals(o.getStatus().name()))
            .count();

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        
        // Total Revenue/Spend
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(String.format("1. Total Revenue/Spend: $%,.2f", totalRevenue));
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        // Total Orders
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(String.format("2. Total Orders: %d (%d Pending)", totalOrders, pendingCount));
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        // Average Order Value
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(String.format("3. Average Order Value: $%,.2f", avgOrderValue));
        contentStream.endText();
        yPosition -= 30;

        return yPosition;
    }

    private float addFinancialBreakdown(PDPageContentStream contentStream, PDPage page,
                                       float yPosition, List<Order> orders, String entityType,
                                       PDDocument document) throws IOException {
        
        // Check if we need a new page
        if (yPosition < 200) {
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = page.getMediaBox().getHeight() - MARGIN;
        }

        // Section Title
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADER);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("FINANCIAL BREAKDOWN (Ledger)");
        contentStream.endText();
        yPosition -= 25;

        // Table Headers
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_SMALL);
        float col1 = MARGIN;
        float col2 = MARGIN + 80;
        float col3 = MARGIN + 180;
        float col4 = MARGIN + 280;
        float col5 = MARGIN + 340;
        float col6 = MARGIN + 420;

        contentStream.beginText();
        contentStream.newLineAtOffset(col1, yPosition);
        contentStream.showText("Date");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col2, yPosition);
        contentStream.showText("Order ID");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col3, yPosition);
        contentStream.showText(entityType.equals("SUPPLIER") ? "Retailer" : "Supplier");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col4, yPosition);
        contentStream.showText("Status");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col5, yPosition);
        contentStream.showText("Items");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col6, yPosition);
        contentStream.showText("Total");
        contentStream.endText();
        yPosition -= 15;

        // Draw header line
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
        contentStream.stroke();
        yPosition -= 10;

        // Table Data
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
        
        // Sort orders chronologically (oldest to newest)
        orders.sort((o1, o2) -> o1.getOrderDate().compareTo(o2.getOrderDate()));

        for (Order order : orders) {
            // Check if we need a new page
            if (yPosition < 80) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = page.getMediaBox().getHeight() - MARGIN;
            }

            String dateStr = order.getOrderDate() != null ? 
                order.getOrderDate().format(DATE_FORMATTER) : "N/A";
            String orderNum = order.getOrderNumber() != null ? 
                order.getOrderNumber() : "N/A";
            String entityId = entityType.equals("SUPPLIER") ? 
                String.valueOf(order.getRetailerId()) : String.valueOf(order.getSupplierId());
            String status = order.getStatus() != null ? order.getStatus().name() : "N/A";
            int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
            String total = order.getTotalAmount() != null ? 
                String.format("$%,.2f", order.getTotalAmount()) : "$0.00";

            contentStream.beginText();
            contentStream.newLineAtOffset(col1, yPosition);
            contentStream.showText(truncate(dateStr, 12));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col2, yPosition);
            contentStream.showText(truncate(orderNum, 15));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col3, yPosition);
            contentStream.showText(entityId);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col4, yPosition);
            contentStream.showText(truncate(status, 10));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col5, yPosition);
            contentStream.showText(String.valueOf(itemCount));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col6, yPosition);
            contentStream.showText(total);
            contentStream.endText();

            yPosition -= 12;
        }

        yPosition -= 20;

        // Footer on last page
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), FONT_SIZE_SMALL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, 50);
        contentStream.showText("This report is generated automatically by Vendora System.");
        contentStream.endText();

        return yPosition;
    }

    private void addPageNumbers(PDDocument document) throws IOException {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
                String pageText = String.format("Page %d of %d", i + 1, totalPages);
                float textWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                    .getStringWidth(pageText) / 1000 * FONT_SIZE_SMALL;
                float xPosition = (page.getMediaBox().getWidth() - textWidth) / 2;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, 30);
                contentStream.showText(pageText);
                contentStream.endText();
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 2) + ".." : text;
    }
}
