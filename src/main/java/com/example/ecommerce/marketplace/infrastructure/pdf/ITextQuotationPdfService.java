package com.example.ecommerce.marketplace.infrastructure.pdf;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.Quotation.QuotationItem;
import com.example.ecommerce.marketplace.domain.quotation.QuotationItemStatus;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of QuotationPdfService using iText 7.
 * Generates professional PDF documents for finalized quotations.
 */
@Service
@Slf4j
public class ITextQuotationPdfService implements QuotationPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    
    // Brand colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(37, 99, 235);    // Blue #2563EB
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(34, 197, 94);    // Green #22C55E
    private static final DeviceRgb WARNING_COLOR = new DeviceRgb(245, 158, 11);   // Orange #F59E0B
    private static final DeviceRgb DANGER_COLOR = new DeviceRgb(239, 68, 68);     // Red #EF4444
    private static final DeviceRgb GRAY_BG = new DeviceRgb(243, 244, 246);        // Gray #F3F4F6
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(75, 85, 99);         // Gray #4B5563

    @Override
    public byte[] generateQuotationPdf(Quotation quotation, String userRole) {
        log.info("Generating PDF for quotation {} with role {}", quotation.getId(), userRole);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Set page margins
            document.setMargins(36, 36, 36, 36);
            
            // Build PDF sections
            addHeader(document, quotation);
            addPartiesSection(document, quotation, userRole);
            addSummarySection(document, quotation);
            addItemsSection(document, quotation, userRole);
            addFinancialSummary(document, quotation);
            addTimelineSection(document, quotation);
            addFooter(document);
            
            document.close();
            
            log.info("Successfully generated PDF for quotation {}", quotation.getId());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to generate PDF for quotation {}", quotation.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, Quotation quotation) {
        // Main title
        Paragraph title = new Paragraph("QUOTATION")
            .setFontSize(26)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(8);
        document.add(title);
        
        // Quotation number
        Paragraph quotationNumber = new Paragraph(String.format("Quotation #%s", quotation.getQuotationNumber()))
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(GRAY_TEXT)
            .setMarginBottom(5);
        document.add(quotationNumber);
        
        // Status badge
        Paragraph statusBadge = new Paragraph("✓ FINALIZED")
            .setFontSize(12)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setBackgroundColor(SUCCESS_COLOR)
            .setPadding(6)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        document.add(statusBadge);
        
        // Divider line
        SolidLine line = new SolidLine(2f);
        line.setColor(GRAY_BG);
        LineSeparator separator = new LineSeparator(line);
        document.add(separator);
        document.add(new Paragraph().setMarginBottom(15));
    }

    private void addPartiesSection(Document document, Quotation quotation, String userRole) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .useAllAvailableWidth()
            .setMarginBottom(20);
        
        // Column headers
        Cell retailerHeader = new Cell()
            .add(new Paragraph("RETAILER" + ("RETAILER".equals(userRole) ? " (You)" : ""))
                .setBold()
                .setFontSize(11))
            .setBackgroundColor(GRAY_BG)
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER);
        
        Cell supplierHeader = new Cell()
            .add(new Paragraph("SUPPLIER" + ("SUPPLIER".equals(userRole) ? " (You)" : ""))
                .setBold()
                .setFontSize(11))
            .setBackgroundColor(GRAY_BG)
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER);
        
        table.addCell(retailerHeader);
        table.addCell(supplierHeader);
        
        // Party details
        Cell retailerDetails = new Cell()
            .add(new Paragraph(String.format("Retailer ID: %d", quotation.getRetailerId()))
                .setFontSize(10)
                .setMarginBottom(3))
            .add(new Paragraph("Contact information available in system")
                .setFontSize(9)
                .setFontColor(GRAY_TEXT))
            .setPadding(10)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        Cell supplierDetails = new Cell()
            .add(new Paragraph(String.format("Supplier ID: %d", quotation.getSupplierId()))
                .setFontSize(10)
                .setMarginBottom(3))
            .add(new Paragraph("Contact information available in system")
                .setFontSize(9)
                .setFontColor(GRAY_TEXT))
            .setPadding(10)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        table.addCell(retailerDetails);
        table.addCell(supplierDetails);
        
        document.add(table);
    }

    private void addSummarySection(Document document, Quotation quotation) {
        // Section title
        Paragraph sectionTitle = new Paragraph("FINALIZATION SUMMARY")
            .setFontSize(14)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 2))
            .setPaddingBottom(5);
        document.add(sectionTitle);
        
        // Calculate statistics
        List<QuotationItem> items = quotation.getItems();
        long acceptedCount = items.stream()
            .filter(item -> RetailerAction.ACCEPT.equals(item.getRetailerAction()))
            .count();
        long rejectedCount = items.stream()
            .filter(item -> RetailerAction.REJECT.equals(item.getRetailerAction()))
            .count();
        long acknowledgedCount = items.stream()
            .filter(item -> RetailerAction.ACKNOWLEDGE.equals(item.getRetailerAction()))
            .count();
        
        double totalValue = items.stream()
            .filter(item -> RetailerAction.ACCEPT.equals(item.getRetailerAction()))
            .mapToDouble(item -> (item.getOfferedPrice() != null && item.getOfferedQuantity() != null) 
                ? item.getOfferedPrice() * item.getOfferedQuantity() : 0.0)
            .sum();
        
        // Summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
            .useAllAvailableWidth()
            .setMarginBottom(20);
        
        addSummaryRow(summaryTable, "Created On", quotation.getCreatedAt().format(DATETIME_FORMATTER));
        addSummaryRow(summaryTable, "Finalized On", quotation.getFinalizedAt() != null 
            ? quotation.getFinalizedAt().format(DATETIME_FORMATTER) : "N/A");
        addSummaryRow(summaryTable, "Valid Until", quotation.getValidUntil().format(DATETIME_FORMATTER));
        addEmptyRow(summaryTable);
        addSummaryRow(summaryTable, "Items Accepted", String.format("%d items ($%.2f)", acceptedCount, totalValue));
        addSummaryRow(summaryTable, "Items Rejected", String.format("%d items", rejectedCount));
        addSummaryRow(summaryTable, "Items Acknowledged", String.format("%d items (Supplier could not fulfill)", acknowledgedCount));
        
        document.add(summaryTable);
    }

    private void addSummaryRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setBold().setFontSize(10).setFontColor(GRAY_TEXT))
            .setBorder(Border.NO_BORDER)
            .setPaddingBottom(8);
        
        Cell valueCell = new Cell()
            .add(new Paragraph(value).setFontSize(10))
            .setBorder(Border.NO_BORDER)
            .setPaddingBottom(8);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addEmptyRow(Table table) {
        table.addCell(new Cell(1, 2).add(new Paragraph(" ")).setBorder(Border.NO_BORDER).setPadding(3));
    }

    private void addItemsSection(Document document, Quotation quotation, String userRole) {
        List<QuotationItem> items = quotation.getItems();
        
        // Accepted Items
        List<QuotationItem> acceptedItems = items.stream()
            .filter(item -> RetailerAction.ACCEPT.equals(item.getRetailerAction()))
            .collect(Collectors.toList());
        
        if (!acceptedItems.isEmpty()) {
            addItemsSectionHeader(document, "ACCEPTED ITEMS", acceptedItems.size(), SUCCESS_COLOR);
            for (QuotationItem item : acceptedItems) {
                addItemCard(document, item, SUCCESS_COLOR);
            }
        }
        
        // Rejected Items
        List<QuotationItem> rejectedItems = items.stream()
            .filter(item -> RetailerAction.REJECT.equals(item.getRetailerAction()))
            .collect(Collectors.toList());
        
        if (!rejectedItems.isEmpty()) {
            addItemsSectionHeader(document, "REJECTED ITEMS", rejectedItems.size(), DANGER_COLOR);
            for (QuotationItem item : rejectedItems) {
                addItemCard(document, item, DANGER_COLOR);
            }
        }
        
        // Acknowledged Items (Supplier couldn't fulfill)
        List<QuotationItem> acknowledgedItems = items.stream()
            .filter(item -> RetailerAction.ACKNOWLEDGE.equals(item.getRetailerAction()))
            .collect(Collectors.toList());
        
        if (!acknowledgedItems.isEmpty()) {
            addItemsSectionHeader(document, "ACKNOWLEDGED ITEMS (Supplier Could Not Fulfill)", 
                acknowledgedItems.size(), WARNING_COLOR);
            for (QuotationItem item : acknowledgedItems) {
                addItemCard(document, item, WARNING_COLOR);
            }
        }
    }

    private void addItemsSectionHeader(Document document, String title, int count, DeviceRgb color) {
        Paragraph header = new Paragraph(String.format("%s (%d)", title, count))
            .setFontSize(13)
            .setBold()
            .setFontColor(color)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setBorderBottom(new SolidBorder(color, 1))
            .setPaddingBottom(5);
        document.add(header);
    }

    private void addItemCard(Document document, QuotationItem item, DeviceRgb statusColor) {
        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1}))
            .useAllAvailableWidth()
            .setMarginBottom(10)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        // Product info
        String productInfo = item.getVariantId() != null 
            ? String.format("Variant ID: %d", item.getVariantId())
            : String.format("Product ID: %d", item.getProductId());
        
        String notes = item.getRetailerNotes() != null && !item.getRetailerNotes().isEmpty()
            ? item.getRetailerNotes()
            : "No notes provided";
        
        Cell productCell = new Cell()
            .add(new Paragraph(productInfo)
                .setBold()
                .setFontSize(10)
                .setMarginBottom(3))
            .add(new Paragraph(notes)
                .setFontSize(9)
                .setFontColor(GRAY_TEXT))
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
        
        // Quantity
        Integer quantity = item.getOfferedQuantity() != null ? item.getOfferedQuantity() : 0;
        Cell quantityCell = new Cell()
            .add(new Paragraph("Quantity").setBold().setFontSize(8).setMarginBottom(2))
            .add(new Paragraph(String.valueOf(quantity)).setFontSize(10))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
        
        // Price
        Double price = item.getOfferedPrice() != null ? item.getOfferedPrice() : 0.0;
        Cell priceCell = new Cell()
            .add(new Paragraph("Unit Price").setBold().setFontSize(8).setMarginBottom(2))
            .add(new Paragraph(String.format("$%.2f", price)).setFontSize(10))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
        
        // Subtotal
        double subtotal = (item.getOfferedPrice() != null && item.getOfferedQuantity() != null)
            ? item.getOfferedPrice() * item.getOfferedQuantity() : 0.0;
        Cell subtotalCell = new Cell()
            .add(new Paragraph("Subtotal").setBold().setFontSize(8).setMarginBottom(2))
            .add(new Paragraph(String.format("$%.2f", subtotal)).setFontSize(10).setBold())
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
        
        itemTable.addCell(productCell);
        itemTable.addCell(quantityCell);
        itemTable.addCell(priceCell);
        itemTable.addCell(subtotalCell);
        
        document.add(itemTable);
    }

    private void addFinancialSummary(Document document, Quotation quotation) {
        // Calculate totals
        double totalAccepted = quotation.getItems().stream()
            .filter(item -> RetailerAction.ACCEPT.equals(item.getRetailerAction()))
            .mapToDouble(item -> (item.getOfferedPrice() != null && item.getOfferedQuantity() != null)
                ? item.getOfferedPrice() * item.getOfferedQuantity() : 0.0)
            .sum();
        
        // Section title
        Paragraph sectionTitle = new Paragraph("FINANCIAL SUMMARY")
            .setFontSize(14)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(20)
            .setMarginBottom(10)
            .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 2))
            .setPaddingBottom(5);
        document.add(sectionTitle);
        
        // Financial table
        Table financeTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .useAllAvailableWidth()
            .setMarginBottom(20);
        
        // Subtotal
        financeTable.addCell(createFinanceCell("Subtotal (Accepted Items)", false));
        financeTable.addCell(createFinanceCell(String.format("$%.2f", totalAccepted), true));
        
        // Tax (placeholder)
        financeTable.addCell(createFinanceCell("Tax (if applicable)", false));
        financeTable.addCell(createFinanceCell("$0.00", true));
        
        // Total
        financeTable.addCell(createFinanceCell("TOTAL", true)
            .setBackgroundColor(GRAY_BG)
            .setBold());
        financeTable.addCell(createFinanceCell(String.format("$%.2f", totalAccepted), true)
            .setBackgroundColor(GRAY_BG)
            .setBold());
        
        document.add(financeTable);
    }

    private Cell createFinanceCell(String text, boolean rightAlign) {
        return new Cell()
            .add(new Paragraph(text).setFontSize(11))
            .setTextAlignment(rightAlign ? TextAlignment.RIGHT : TextAlignment.LEFT)
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
    }

    private void addTimelineSection(Document document, Quotation quotation) {
        // Section title
        Paragraph sectionTitle = new Paragraph("TIMELINE")
            .setFontSize(14)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 2))
            .setPaddingBottom(5);
        document.add(sectionTitle);
        
        // Timeline events
        if (quotation.getCreatedAt() != null) {
            addTimelineEvent(document, "Quotation Created", 
                quotation.getCreatedAt().format(DATETIME_FORMATTER));
        }
        
        if (quotation.getRespondedAt() != null) {
            addTimelineEvent(document, "Supplier Responded", 
                quotation.getRespondedAt().format(DATETIME_FORMATTER));
        }
        
        if (quotation.getFinalizedAt() != null) {
            addTimelineEvent(document, "Retailer Finalized", 
                quotation.getFinalizedAt().format(DATETIME_FORMATTER));
        }
        
        document.add(new Paragraph().setMarginBottom(10));
    }

    private void addTimelineEvent(Document document, String event, String timestamp) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 3.5f}))
            .useAllAvailableWidth()
            .setMarginBottom(5);
        
        Cell bulletCell = new Cell()
            .add(new Paragraph("●").setFontSize(10).setFontColor(PRIMARY_COLOR))
            .setBorder(Border.NO_BORDER)
            .setPadding(2);
        
        Cell contentCell = new Cell()
            .add(new Paragraph(event).setBold().setFontSize(10).setMarginBottom(2))
            .add(new Paragraph(timestamp).setFontSize(9).setFontColor(GRAY_TEXT))
            .setBorder(Border.NO_BORDER)
            .setPadding(2);
        
        table.addCell(bulletCell);
        table.addCell(contentCell);
        
        document.add(table);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph().setMarginTop(20));
        
        // Footer text
        Paragraph footer = new Paragraph("This is a finalized quotation document generated by Vendora B2B E-Commerce System. " +
            "For any questions or concerns, please contact the supplier or retailer directly through the platform.")
            .setFontSize(9)
            .setFontColor(GRAY_TEXT)
            .setTextAlignment(TextAlignment.CENTER)
            .setItalic();
        
        SolidLine footerLine = new SolidLine(1f);
        footerLine.setColor(GRAY_BG);
        document.add(new LineSeparator(footerLine));
        document.add(new Paragraph().setMarginTop(10));
        document.add(footer);
    }
}
