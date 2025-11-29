package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionCommand;
import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionResult;
import com.example.ecommerce.marketplace.application.analytics.TrackUserInteractionUseCase;
import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import com.example.ecommerce.marketplace.web.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AnalyticsController.
 * Tests HTTP request/response handling for analytics tracking endpoints.
 * Uses standalone MockMvc setup for isolated controller testing.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TrackUserInteractionUseCase trackUserInteractionUseCase;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime serialization
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Nested
    @DisplayName("POST /api/v1/analytics/track - Track Interaction")
    class TrackInteractionTests {

        @Test
        @DisplayName("Should return 202 ACCEPTED when interaction is tracked successfully")
        void trackInteraction_ShouldReturn202_WhenSuccess() throws Exception {
            // Given
            Long userId = 1L;
            Long productId = 100L;
            Long interactionId = 1000L;
            InteractionType interactionType = InteractionType.VIEW;

            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("productId", productId);
            request.put("interactionType", interactionType.name());

            TrackUserInteractionResult successResult = TrackUserInteractionResult.success(
                interactionId, interactionType
            );

            when(trackUserInteractionUseCase.execute(any(TrackUserInteractionCommand.class)))
                .thenReturn(successResult);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.interactionId").value(interactionId))
                .andExpect(jsonPath("$.interactionType").value(interactionType.name()))
                .andExpect(jsonPath("$.message").value("Interaction tracked successfully"));

            verify(trackUserInteractionUseCase, times(1)).execute(any(TrackUserInteractionCommand.class));
        }

        @Test
        @DisplayName("Should track VIEW interaction successfully")
        void trackInteraction_ViewType_ShouldReturn202() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.VIEW);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.interactionType").value("VIEW"));
        }

        @Test
        @DisplayName("Should track CLICK interaction successfully")
        void trackInteraction_ClickType_ShouldReturn202() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "CLICK");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.CLICK);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.interactionType").value("CLICK"));
        }

        @Test
        @DisplayName("Should track ADD_TO_CART interaction successfully")
        void trackInteraction_AddToCartType_ShouldReturn202() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "ADD_TO_CART");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.ADD_TO_CART);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.interactionType").value("ADD_TO_CART"));
        }

        @Test
        @DisplayName("Should track PURCHASE interaction successfully")
        void trackInteraction_PurchaseType_ShouldReturn202() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "PURCHASE");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.PURCHASE);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.interactionType").value("PURCHASE"));
        }

        @Test
        @DisplayName("Should include optional variantId when provided")
        void trackInteraction_WithVariantId_ShouldReturn202() throws Exception {
            // Given
            Long variantId = 200L;
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("variantId", variantId);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.VIEW);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            verify(trackUserInteractionUseCase).execute(argThat(cmd -> 
                cmd.getVariantId() != null && cmd.getVariantId().equals(variantId)
            ));
        }

        @Test
        @DisplayName("Should include optional sessionId when provided")
        void trackInteraction_WithSessionId_ShouldReturn202() throws Exception {
            // Given
            String sessionId = "session-abc-123";
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("sessionId", sessionId);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.VIEW);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            verify(trackUserInteractionUseCase).execute(argThat(cmd -> 
                sessionId.equals(cmd.getSessionId())
            ));
        }

        @Test
        @DisplayName("Should include optional metadata when provided")
        void trackInteraction_WithMetadata_ShouldReturn202() throws Exception {
            // Given
            Map<String, String> metadata = new HashMap<>();
            metadata.put("searchQuery", "laptop");
            metadata.put("referrer", "homepage");

            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "VIEW");
            request.put("metadata", metadata);

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.VIEW);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            verify(trackUserInteractionUseCase).execute(argThat(cmd -> 
                cmd.getMetadata() != null && 
                "laptop".equals(cmd.getMetadata().get("searchQuery"))
            ));
        }

        @Test
        @DisplayName("Should include all optional fields when provided")
        void trackInteraction_WithAllFields_ShouldReturn202() throws Exception {
            // Given
            Map<String, String> metadata = new HashMap<>();
            metadata.put("referrer", "search");

            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("variantId", 200L);
            request.put("sessionId", "session-xyz");
            request.put("interactionType", "PURCHASE");
            request.put("metadata", metadata);

            TrackUserInteractionResult result = TrackUserInteractionResult.success(1L, InteractionType.PURCHASE);
            when(trackUserInteractionUseCase.execute(any())).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.interactionId").value(1L))
                .andExpect(jsonPath("$.interactionType").value("PURCHASE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/analytics/track - Error Handling")
    class TrackInteractionErrorTests {

        @Test
        @DisplayName("Should return 404 when product is not found")
        void trackInteraction_WhenProductNotFound_ShouldReturn404() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 999L);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult failureResult = TrackUserInteractionResult.failure(
                "Product not found", "PRODUCT_NOT_FOUND"
            );

            when(trackUserInteractionUseCase.execute(any(TrackUserInteractionCommand.class)))
                .thenReturn(failureResult);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found"));
        }

        @Test
        @DisplayName("Should return 500 when tracking fails")
        void trackInteraction_WhenTrackingFails_ShouldReturn500() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult failureResult = TrackUserInteractionResult.failure(
                "Failed to track interaction", "TRACKING_FAILED"
            );

            when(trackUserInteractionUseCase.execute(any(TrackUserInteractionCommand.class)))
                .thenReturn(failureResult);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("TRACKING_FAILED"))
                .andExpect(jsonPath("$.message").value("Failed to track interaction"));
        }

        @Test
        @DisplayName("Should return 400 for unknown error code")
        void trackInteraction_WhenUnknownError_ShouldReturn400() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "VIEW");

            TrackUserInteractionResult failureResult = TrackUserInteractionResult.failure(
                "Unknown error occurred", "UNKNOWN_ERROR"
            );

            when(trackUserInteractionUseCase.execute(any(TrackUserInteractionCommand.class)))
                .thenReturn(failureResult);

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("UNKNOWN_ERROR"))
                .andExpect(jsonPath("$.message").value("Unknown error occurred"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/analytics/track - Validation Errors")
    class TrackInteractionValidationTests {

        @Test
        @DisplayName("Should return 400 when userId is missing")
        void trackInteraction_WhenUserIdMissing_ShouldReturn400() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("productId", 100L);
            request.put("interactionType", "VIEW");
            // userId is missing

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(trackUserInteractionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 400 when productId is missing")
        void trackInteraction_WhenProductIdMissing_ShouldReturn400() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("interactionType", "VIEW");
            // productId is missing

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(trackUserInteractionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 400 when interactionType is missing")
        void trackInteraction_WhenInteractionTypeMissing_ShouldReturn400() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            // interactionType is missing

            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(trackUserInteractionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void trackInteraction_WhenEmptyBody_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest());

            verify(trackUserInteractionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 400 when interactionType is invalid")
        void trackInteraction_WhenInvalidInteractionType_ShouldReturn400() throws Exception {
            // Given - invalid enum value will cause Jackson deserialization error which should return 400
            Map<String, Object> request = new HashMap<>();
            request.put("userId", 1L);
            request.put("productId", 100L);
            request.put("interactionType", "INVALID_TYPE");

            // When & Then - invalid enum triggers HttpMessageNotReadableException -> 400
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"));

            // Use case should not be called because deserialization fails first
            verify(trackUserInteractionUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 415 when Content-Type is not JSON")
        void trackInteraction_WhenNotJson_ShouldReturn415() throws Exception {
            // When & Then - non-JSON content type should return 415
            mockMvc.perform(post("/api/v1/analytics/track")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

            verify(trackUserInteractionUseCase, never()).execute(any());
        }
    }
}
