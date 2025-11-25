package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for quotation offer status updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationOfferStatusResponse {

    private Long offerId;
    private String status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}