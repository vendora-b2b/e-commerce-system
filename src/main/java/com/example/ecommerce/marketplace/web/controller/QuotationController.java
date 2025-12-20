package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.quotation.*;
import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import com.example.ecommerce.marketplace.web.model.quotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for Quotation operations.
 */
@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotation", description = "Quotation API")
public class QuotationController {

    private final CreateQuotationUseCase createQuotationUseCase;
    private final GetQuotationUseCase getQuotationUseCase;
    private final ListQuotationsUseCase listQuotationsUseCase;
    private final RespondToQuotationUseCase respondToQuotationUseCase;
    private final FinalizeQuotationUseCase finalizeQuotationUseCase;
    private final CancelQuotationUseCase cancelQuotationUseCase;
    private final GenerateQuotationPdfUseCase generateQuotationPdfUseCase;

    /**
     * Create new quotation(s)
     */
    @PostMapping
    public ResponseEntity<CreateQuotationResponse> createQuotation(
            @RequestParam Long retailerId,
            @Valid @RequestBody CreateQuotationRequest request) {
        
        System.out.println("=== CONTROLLER DEBUG ===");
        System.out.println("Request: " + request);
        System.out.println("Request items: " + request.getItems());
        System.out.println("Request items size: " + (request.getItems() != null ? request.getItems().size() : "null"));
        
        CreateQuotationCommand command = request.toCommand();
        System.out.println("Command items: " + command.getItems());
        System.out.println("Command items size: " + (command.getItems() != null ? command.getItems().size() : "null"));
        
        CreateQuotationResult result = createQuotationUseCase.execute(retailerId, command);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateQuotationResponse.from(result));
    }

    /**
     * List quotations with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<QuotationListItemResponse>> listQuotations(
            @RequestParam String role,
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        PageRequest pageable = PageRequest.of(page, size, sorting);
        
        Page<Quotation> quotations = listQuotationsUseCase.execute(role, retailerId, supplierId, status, pageable);
        Page<QuotationListItemResponse> response = quotations.map(q -> 
            QuotationListItemResponse.from(q, 
                listQuotationsUseCase.getSupplierRepository(), 
                listQuotationsUseCase.getRetailerRepository()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get quotation by ID
     */
    @GetMapping("/{quotationId}")
    public ResponseEntity<QuotationDetailResponse> getQuotation(@PathVariable Long quotationId) {
        Quotation quotation = getQuotationUseCase.execute(quotationId);
        return ResponseEntity.ok(QuotationDetailResponse.from(quotation,
                getQuotationUseCase.getSupplierRepository(),
                getQuotationUseCase.getRetailerRepository(),
                getQuotationUseCase.getProductRepository(),
                getQuotationUseCase.getVariantRepository()));
    }

    /**
     * Supplier responds to quotation
     */
    @PatchMapping("/{quotationId}/respond")
    public ResponseEntity<RespondToQuotationResponse> respondToQuotation(
            @PathVariable Long quotationId,
            @Valid @RequestBody RespondToQuotationRequest request) {
        
        RespondToQuotationCommand command = request.toCommand(quotationId);
        Quotation quotation = respondToQuotationUseCase.execute(command);
        
        return ResponseEntity.ok(RespondToQuotationResponse.from(quotation));
    }

    /**
     * Retailer finalizes quotation
     */
    @PatchMapping("/{quotationId}/finalize")
    public ResponseEntity<FinalizeQuotationResponse> finalizeQuotation(
            @PathVariable Long quotationId,
            @Valid @RequestBody FinalizeQuotationRequest request) {
        
        FinalizeQuotationCommand command = request.toCommand(quotationId);
        FinalizeQuotationResult result = finalizeQuotationUseCase.execute(command);
        
        return ResponseEntity.ok(FinalizeQuotationResponse.from(result));
    }

    /**
     * Cancel quotation
     */
    @PatchMapping("/{quotationId}/cancel")
    public ResponseEntity<CancelQuotationResponse> cancelQuotation(
            @PathVariable Long quotationId,
            @RequestBody(required = false) CancelQuotationRequest request) {
        
        String reason = request != null ? request.getReason() : null;
        Quotation quotation = cancelQuotationUseCase.execute(quotationId, reason);
        
        return ResponseEntity.ok(CancelQuotationResponse.from(quotation));
    }

    /**
     * Download quotation as PDF.
     * Only finalized quotations can be downloaded.
     * 
     * @param quotationId the quotation ID
     * @param role the user role (RETAILER or SUPPLIER)
     * @return PDF file as byte stream
     */
    @GetMapping("/{quotationId}/pdf")
    @Operation(summary = "Download quotation as PDF", 
               description = "Generate and download a PDF document for a finalized quotation. " +
                           "Only FINALIZED quotations can be exported. " +
                           "The PDF content differs based on the user role (RETAILER or SUPPLIER).")
    public ResponseEntity<byte[]> downloadQuotationPdf(
            @PathVariable Long quotationId,
            @RequestParam(defaultValue = "RETAILER") String role) {
        
        try {
            // Generate PDF
            byte[] pdfContent = generateQuotationPdfUseCase.execute(quotationId, role);
            
            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("Quotation_%d_%s.pdf", quotationId, role));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request for validation errors
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Return 500 Internal Server Error for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Preview quotation PDF in browser (inline display).
     * 
     * @param quotationId the quotation ID
     * @param role the user role (RETAILER or SUPPLIER)
     * @return PDF file for inline preview
     */
    @GetMapping("/{quotationId}/pdf/preview")
    @Operation(summary = "Preview quotation PDF", 
               description = "Display PDF in browser without downloading. " +
                           "Opens PDF inline for preview purposes.")
    public ResponseEntity<byte[]> previewQuotationPdf(
            @PathVariable Long quotationId,
            @RequestParam(defaultValue = "RETAILER") String role) {
        
        try {
            byte[] pdfContent = generateQuotationPdfUseCase.execute(quotationId, role);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "inline; filename=preview.pdf");
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
