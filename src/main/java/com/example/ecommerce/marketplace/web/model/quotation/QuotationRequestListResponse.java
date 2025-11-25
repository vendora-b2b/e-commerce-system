package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for listing quotation requests with pagination.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequestListResponse {

    private List<QuotationRequestSummary> content;
    private PageInfo page;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationRequestSummary {
        private Long requestId;
        private String requestNumber;
        private Long retailerId;
        private Long supplierId;
        private String status;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime validUntil;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;
    }
}