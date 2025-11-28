package com.example.ecommerce.marketplace.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request model for generating a chat response from the AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGenerationRequest {
    
    private String query;
    private List<ChatMessage> history;
    private UserProfile userProfile;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;  // "user" or "assistant"
        private String content;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private Long userId;
        private String userType;  // "retailer" or "supplier"
        private String name;
        private String loyaltyTier;  // For retailers
    }
}
