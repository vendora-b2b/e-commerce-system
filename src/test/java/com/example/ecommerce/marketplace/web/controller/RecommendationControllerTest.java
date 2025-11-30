package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.application.recommendation.*;
import com.example.ecommerce.marketplace.web.common.CustomBusinessException;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RecommendationController.
 * Uses standalone MockMvc setup for isolated controller testing.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GetProductRecommendationsUseCase getProductRecommendationsUseCase;

    @Mock
    private GetHomepageRecommendationsUseCase getHomepageRecommendationsUseCase;

    @Mock
    private GetSimilarProductsUseCase getSimilarProductsUseCase;

    @InjectMocks
    private RecommendationController recommendationController;

    private List<GetSimilarProductsResult.ProductRecommendation> similarProducts;
    private List<GetProductRecommendationsResult.ProductRecommendation> userRecommendations;
    private List<GetHomepageRecommendationsResult.ProductRecommendation> homepageRecommendations;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime serialization
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

        similarProducts = Arrays.asList(
            new GetSimilarProductsResult.ProductRecommendation(101L, "SKU-101", "Similar Product 1", 0.95),
            new GetSimilarProductsResult.ProductRecommendation(102L, "SKU-102", "Similar Product 2", 0.87)
        );

        userRecommendations = Arrays.asList(
            new GetProductRecommendationsResult.ProductRecommendation(201L, "SKU-201", "Recommended 1", 0.92),
            new GetProductRecommendationsResult.ProductRecommendation(202L, "SKU-202", "Recommended 2", 0.85)
        );

        homepageRecommendations = Arrays.asList(
            new GetHomepageRecommendationsResult.ProductRecommendation(301L, "SKU-301", "Homepage 1", 0.88),
            new GetHomepageRecommendationsResult.ProductRecommendation(302L, "SKU-302", "Homepage 2", 0.82)
        );
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}/recommendations")
    class GetSimilarProductsTests {

        @Test
        @DisplayName("Should return similar products successfully")
        void shouldReturnSimilarProductsSuccessfully() throws Exception {
            // Arrange
            when(getSimilarProductsUseCase.execute(any(GetSimilarProductsCommand.class)))
                .thenReturn(GetSimilarProductsResult.success(1L, similarProducts));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.recommendationType", is("similar")))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.recommendations", hasSize(2)))
                .andExpect(jsonPath("$.recommendations[0].productId", is(101)))
                .andExpect(jsonPath("$.recommendations[0].sku", is("SKU-101")))
                .andExpect(jsonPath("$.recommendations[0].name", is("Similar Product 1")))
                .andExpect(jsonPath("$.recommendations[0].score", is(0.95)));
        }

        @Test
        @DisplayName("Should return empty list when no similar products")
        void shouldReturnEmptyListWhenNoSimilarProducts() throws Exception {
            // Arrange
            when(getSimilarProductsUseCase.execute(any(GetSimilarProductsCommand.class)))
                .thenReturn(GetSimilarProductsResult.success(1L, Collections.emptyList()));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0)));
        }

        @Test
        @DisplayName("Should apply custom limit")
        void shouldApplyCustomLimit() throws Exception {
            // Arrange
            when(getSimilarProductsUseCase.execute(any(GetSimilarProductsCommand.class)))
                .thenReturn(GetSimilarProductsResult.success(1L, similarProducts));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1/recommendations")
                    .param("limit", "10"))
                .andExpect(status().isOk());

            verify(getSimilarProductsUseCase).execute(argThat(cmd -> 
                cmd.getProductId().equals(1L) && cmd.getLimit() == 10
            ));
        }

        @Test
        @DisplayName("Should use default limit of 6")
        void shouldUseDefaultLimit() throws Exception {
            // Arrange
            when(getSimilarProductsUseCase.execute(any(GetSimilarProductsCommand.class)))
                .thenReturn(GetSimilarProductsResult.success(1L, similarProducts));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1/recommendations"))
                .andExpect(status().isOk());

            verify(getSimilarProductsUseCase).execute(argThat(cmd -> 
                cmd.getLimit() == 6
            ));
        }

        @Test
        @DisplayName("Should return 400 when productId is invalid")
        void shouldReturn400WhenProductIdInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/products/invalid/recommendations"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PARAMETER")));
        }

        @Test
        @DisplayName("Should return 503 when recommendation service unavailable")
        void shouldReturn503WhenServiceUnavailable() throws Exception {
            // Arrange
            when(getSimilarProductsUseCase.execute(any(GetSimilarProductsCommand.class)))
                .thenThrow(new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR", 
                    "Recommendation service is temporarily unavailable"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1/recommendations"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("RECOMMENDATION_SERVICE_ERROR")))
                .andExpect(jsonPath("$.message", is("Recommendation service is temporarily unavailable")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/recommendations/homepage")
    class GetHomepageRecommendationsTests {

        @Test
        @DisplayName("Should return homepage recommendations for anonymous user")
        void shouldReturnHomepageRecommendationsForAnonymous() throws Exception {
            // Arrange
            when(getHomepageRecommendationsUseCase.execute(any(GetHomepageRecommendationsCommand.class)))
                .thenReturn(GetHomepageRecommendationsResult.success(homepageRecommendations, false));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/homepage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationType", is("homepage")))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.recommendations", hasSize(2)))
                .andExpect(jsonPath("$.recommendations[0].productId", is(301)));
        }

        @Test
        @DisplayName("Should return personalized recommendations for logged-in user")
        void shouldReturnPersonalizedRecommendationsForUser() throws Exception {
            // Arrange
            when(getHomepageRecommendationsUseCase.execute(any(GetHomepageRecommendationsCommand.class)))
                .thenReturn(GetHomepageRecommendationsResult.success(homepageRecommendations, true));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/homepage")
                    .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations", hasSize(2)));

            verify(getHomepageRecommendationsUseCase).execute(argThat(cmd -> 
                cmd.getUserId() != null && cmd.getUserId().equals(100L)
            ));
        }

        @Test
        @DisplayName("Should apply custom limit")
        void shouldApplyCustomLimit() throws Exception {
            // Arrange
            when(getHomepageRecommendationsUseCase.execute(any(GetHomepageRecommendationsCommand.class)))
                .thenReturn(GetHomepageRecommendationsResult.success(homepageRecommendations, false));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/homepage")
                    .param("limit", "20"))
                .andExpect(status().isOk());

            verify(getHomepageRecommendationsUseCase).execute(argThat(cmd -> 
                cmd.getLimit() == 20
            ));
        }

        @Test
        @DisplayName("Should use default limit of 12")
        void shouldUseDefaultLimit() throws Exception {
            // Arrange
            when(getHomepageRecommendationsUseCase.execute(any(GetHomepageRecommendationsCommand.class)))
                .thenReturn(GetHomepageRecommendationsResult.success(homepageRecommendations, false));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/homepage"))
                .andExpect(status().isOk());

            verify(getHomepageRecommendationsUseCase).execute(argThat(cmd -> 
                cmd.getLimit() == 12
            ));
        }

        @Test
        @DisplayName("Should return 503 when recommendation service unavailable")
        void shouldReturn503WhenServiceUnavailable() throws Exception {
            // Arrange
            when(getHomepageRecommendationsUseCase.execute(any(GetHomepageRecommendationsCommand.class)))
                .thenThrow(new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR", 
                    "Recommendation service is temporarily unavailable"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/homepage"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("RECOMMENDATION_SERVICE_ERROR")))
                .andExpect(jsonPath("$.message", is("Recommendation service is temporarily unavailable")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/recommendations/user/{userId}")
    class GetUserRecommendationsTests {

        @Test
        @DisplayName("Should return user recommendations successfully")
        void shouldReturnUserRecommendationsSuccessfully() throws Exception {
            // Arrange
            when(getProductRecommendationsUseCase.execute(any(GetProductRecommendationsCommand.class)))
                .thenReturn(GetProductRecommendationsResult.success(userRecommendations));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationType", is("personalized")))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.recommendations", hasSize(2)))
                .andExpect(jsonPath("$.recommendations[0].productId", is(201)))
                .andExpect(jsonPath("$.recommendations[0].sku", is("SKU-201")));
        }

        @Test
        @DisplayName("Should return empty list for new user")
        void shouldReturnEmptyListForNewUser() throws Exception {
            // Arrange
            when(getProductRecommendationsUseCase.execute(any(GetProductRecommendationsCommand.class)))
                .thenReturn(GetProductRecommendationsResult.success(Collections.emptyList()));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0)));
        }

        @Test
        @DisplayName("Should apply custom limit")
        void shouldApplyCustomLimit() throws Exception {
            // Arrange
            when(getProductRecommendationsUseCase.execute(any(GetProductRecommendationsCommand.class)))
                .thenReturn(GetProductRecommendationsResult.success(userRecommendations));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/100")
                    .param("limit", "5"))
                .andExpect(status().isOk());

            verify(getProductRecommendationsUseCase).execute(argThat(cmd -> 
                cmd.getUserId().equals(100L) && cmd.getLimit() == 5
            ));
        }

        @Test
        @DisplayName("Should use default limit of 10")
        void shouldUseDefaultLimit() throws Exception {
            // Arrange
            when(getProductRecommendationsUseCase.execute(any(GetProductRecommendationsCommand.class)))
                .thenReturn(GetProductRecommendationsResult.success(userRecommendations));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/100"))
                .andExpect(status().isOk());

            verify(getProductRecommendationsUseCase).execute(argThat(cmd -> 
                cmd.getLimit() == 10
            ));
        }

        @Test
        @DisplayName("Should return 400 when userId is invalid")
        void shouldReturn400WhenUserIdInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PARAMETER")));
        }

        @Test
        @DisplayName("Should return 503 when recommendation service unavailable")
        void shouldReturn503WhenServiceUnavailable() throws Exception {
            // Arrange
            when(getProductRecommendationsUseCase.execute(any(GetProductRecommendationsCommand.class)))
                .thenThrow(new CustomBusinessException("RECOMMENDATION_SERVICE_ERROR", 
                    "Recommendation service is temporarily unavailable"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/recommendations/user/100"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("RECOMMENDATION_SERVICE_ERROR")))
                .andExpect(jsonPath("$.message", is("Recommendation service is temporarily unavailable")));
        }
    }
}
