package com.example.ecommerce.marketplace.web.model.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelQuotationRequest {
    private String reason;
}
