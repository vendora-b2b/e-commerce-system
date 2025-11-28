package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response model for chat generation from the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGenerationResponse {
    
    private String response;
    private List<Map<String, Object>> sources;
    private String queryType;
}
