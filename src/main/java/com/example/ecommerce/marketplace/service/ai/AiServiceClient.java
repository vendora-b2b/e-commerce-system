package com.example.ecommerce.marketplace.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for communicating with the Python AI Service.
 * 
 * <p>This is an HTTP <strong>client</strong>, not a controller.
 * It makes <strong>outgoing</strong> HTTP calls to the Python AI Service using WebClient.</p>
 * 
 * <h3>Why no @GetMapping/@PostMapping?</h3>
 * <ul>
 *   <li>{@code @GetMapping}/{@code @PostMapping} are for <strong>receiving</strong> HTTP requests (Controllers)</li>
 *   <li>This class <strong>sends</strong> HTTP requests using {@code WebClient.get()}/{@code WebClient.post()}</li>
 * </ul>
 * 
 * <h3>Flow Diagram:</h3>
 * <pre>
 * Frontend ──@GetMapping──► ChatController ──► ChatUseCase ──► AiServiceClient ──WebClient──► Python AI
 *                          (receives request)                  (sends request)
 * </pre>
 * 
 * <h3>Why block() instead of returning Mono?</h3>
 * <p>The project uses synchronous architecture (Controllers return ResponseEntity, not Mono).
 * Calling {@code .block()} converts the async Mono to a synchronous result.</p>
 */
@Service
@Slf4j
public class AiServiceClient {

    private final WebClient webClient;
    private final Duration timeout;

    public AiServiceClient(
            @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl,
            @Value("${ai.service.timeout:30}") int timeoutSeconds
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        log.info("AI Service Client initialized with URL: {}", aiServiceUrl);
    }

    // ==================== Product Ingestion ====================

    /**
     * Ingest a product into the AI service vector database.
     * 
     * POST /ai/ingest/product
     *
     * @param request the product data to ingest
     * @return response map with ingestion status
     */
    public Map<String, Object> ingestProduct(ProductIngestRequest request) {
        log.debug("Ingesting product to AI service: {}", request.getSku());
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/ai/ingest/product")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();
            
            log.info("Product ingested successfully: {}", request.getSku());
            return response != null ? response : Collections.emptyMap();
        } catch (WebClientResponseException e) {
            log.error("Failed to ingest product {}: {} - {}", request.getSku(), e.getStatusCode(), e.getMessage());
            throw new AiServiceException("Failed to ingest product: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to ingest product {}: {}", request.getSku(), e.getMessage());
            throw new AiServiceException("Failed to ingest product: " + e.getMessage(), e);
        }
    }

    /**
     * Bulk ingest products into the AI service.
     * 
     * POST /ai/ingest/products/bulk
     *
     * @param products list of products to ingest
     * @return response map with bulk ingestion status
     */
    public Map<String, Object> ingestProductsBulk(List<ProductIngestRequest> products) {
        log.debug("Bulk ingesting {} products to AI service", products.size());
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/ai/ingest/products/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("products", products))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMinutes(5)) // Longer timeout for bulk operations
                    .block();
            
            log.info("Bulk product ingestion completed for {} products", products.size());
            return response != null ? response : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Bulk product ingestion failed: {}", e.getMessage());
            throw new AiServiceException("Bulk product ingestion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a product from the AI service vector database.
     * 
     * DELETE /ai/ingest/product/{productId}
     *
     * @param productId the product ID to delete
     * @return response map with deletion status
     */
    public Map<String, Object> deleteProduct(Long productId) {
        log.debug("Deleting product from AI service: {}", productId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.delete()
                    .uri("/ai/ingest/product/{productId}", productId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();
            
            log.info("Product deleted from AI service: {}", productId);
            return response != null ? response : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Failed to delete product {}: {}", productId, e.getMessage());
            throw new AiServiceException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    // ==================== Document Ingestion ====================

    /**
     * Ingest a document into the AI service knowledge base.
     * 
     * POST /ai/ingest/document
     *
     * @param document the document data to ingest (title, content, type, etc.)
     * @return response map with ingestion status
     */
    public Map<String, Object> ingestDocument(Map<String, Object> document) {
        log.debug("Ingesting document to AI service: {}", document.get("document_id"));
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/ai/ingest/document")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(document)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();
            
            log.info("Document ingested successfully: {}", document.get("document_id"));
            return response != null ? response : Collections.emptyMap();
        } catch (WebClientResponseException e) {
            log.error("Failed to ingest document {}: {} - {}", document.get("document_id"), e.getStatusCode(), e.getMessage());
            throw new AiServiceException("Failed to ingest document: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to ingest document {}: {}", document.get("document_id"), e.getMessage());
            throw new AiServiceException("Failed to ingest document: " + e.getMessage(), e);
        }
    }

    // ==================== Chat Generation ====================

    /**
     * Generate a chat response from the AI service.
     * 
     * POST /ai/chat/generate
     *
     * @param request the chat generation request with query and context
     * @return the generated chat response
     */
    public ChatGenerationResponse generateChatResponse(ChatGenerationRequest request) {
        log.debug("Generating chat response for query: {}", request.getQuery());
        
        try {
            ChatGenerationResponse response = webClient.post()
                    .uri("/ai/chat/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatGenerationResponse.class)
                    .timeout(timeout)
                    .block();
            
            log.debug("Chat response generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Chat generation failed: {}", e.getMessage());
            throw new AiServiceException("Chat generation failed: " + e.getMessage(), e);
        }
    }

    // ==================== Recommendations ====================

    /**
     * Track a user interaction for recommendation learning.
     * 
     * POST /ai/recommend/analytics/track
     *
     * @param request the interaction data (userId, productId, action)
     * @return response map with tracking status
     */
    public Map<String, Object> trackInteraction(TrackInteractionRequest request) {
        log.debug("Tracking interaction: user={}, action={}", request.getUserId(), request.getAction());
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/ai/recommend/analytics/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();
            
            log.debug("Interaction tracked successfully");
            return response != null ? response : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Failed to track interaction: {}", e.getMessage());
            throw new AiServiceException("Failed to track interaction: " + e.getMessage(), e);
        }
    }

    /**
     * Get personalized recommendations for a user.
     * 
     * GET /ai/recommend/user/{userId}?limit={limit}
     *
     * @param userId the user ID
     * @param limit  maximum number of recommendations
     * @return recommendation response with product IDs and scores
     */
    public RecommendationResponse getUserRecommendations(Long userId, int limit) {
        log.debug("Getting recommendations for user: {}", userId);
        
        try {
            RecommendationResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ai/recommend/user/{userId}")
                            .queryParam("limit", limit)
                            .build(userId))
                    .retrieve()
                    .bodyToMono(RecommendationResponse.class)
                    .timeout(timeout)
                    .block();
            
            log.debug("User recommendations retrieved: {} items", 
                    response != null ? response.getRecommendations().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to get user recommendations: {}", e.getMessage());
            throw new AiServiceException("Failed to get user recommendations: " + e.getMessage(), e);
        }
    }

    /**
     * Get similar products for a given product.
     * 
     * GET /ai/recommend/similar/{productId}?limit={limit}
     *
     * @param productId the product ID to find similar items for
     * @param limit     maximum number of similar products
     * @return recommendation response with similar product IDs and scores
     */
    public RecommendationResponse getSimilarProducts(Long productId, int limit) {
        log.debug("Getting similar products for product: {}", productId);
        
        try {
            RecommendationResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ai/recommend/similar/{productId}")
                            .queryParam("limit", limit)
                            .build(productId))
                    .retrieve()
                    .bodyToMono(RecommendationResponse.class)
                    .timeout(timeout)
                    .block();
            
            log.debug("Similar products retrieved: {} items", 
                    response != null ? response.getRecommendations().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to get similar products: {}", e.getMessage());
            throw new AiServiceException("Failed to get similar products: " + e.getMessage(), e);
        }
    }

    /**
     * Get homepage recommendations for a user.
     * 
     * GET /ai/recommend/homepage/{userId}?limit={limit}
     *
     * @param userId the user ID (can be null for anonymous)
     * @param limit  maximum number of recommendations
     * @return recommendation response with product IDs and scores
     */
    public RecommendationResponse getHomepageRecommendations(Long userId, int limit) {
        log.debug("Getting homepage recommendations for user: {}", userId);
        
        try {
            RecommendationResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ai/recommend/homepage/{userId}")
                            .queryParam("limit", limit)
                            .build(userId != null ? userId : 0))
                    .retrieve()
                    .bodyToMono(RecommendationResponse.class)
                    .timeout(timeout)
                    .block();
            
            log.debug("Homepage recommendations retrieved: {} items", 
                    response != null ? response.getRecommendations().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to get homepage recommendations: {}", e.getMessage());
            throw new AiServiceException("Failed to get homepage recommendations: " + e.getMessage(), e);
        }
    }

    // ==================== Health Check ====================

    /**
     * Health check for the AI service.
     * 
     * GET /health
     *
     * @return true if the AI service is healthy, false otherwise
     */
    public boolean healthCheck() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            return response != null && "healthy".equals(response.get("status"));
        } catch (Exception e) {
            log.warn("AI Service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

