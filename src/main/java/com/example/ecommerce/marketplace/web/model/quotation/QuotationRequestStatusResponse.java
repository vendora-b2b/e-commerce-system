package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for quotation request status updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequestStatusResponse {

    private Long requestId;
    private String status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}