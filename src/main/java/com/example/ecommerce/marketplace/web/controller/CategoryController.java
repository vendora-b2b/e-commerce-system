package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.product.Category;
import com.example.ecommerce.marketplace.infrastructure.product.CategoryEntity;
import com.example.ecommerce.marketplace.infrastructure.product.JpaCategoryRepository;
import com.example.ecommerce.marketplace.web.model.product.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Category operations.
 * Handles HTTP requests for category management.
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final JpaCategoryRepository categoryRepository;

    /**
     * Get all available categories.
     *
     * @return List of all categories
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all available product categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.debug("Fetching all categories");

        List<CategoryEntity> categories = categoryRepository.findAll();
        List<CategoryResponse> response = categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.debug("Found {} categories", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a category by ID.
     *
     * @param id Category ID
     * @return Category details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        log.debug("Fetching category with id: {}", id);

        return categoryRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convert CategoryEntity to CategoryResponse DTO.
     */
    private CategoryResponse toResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug()
        );
    }
}
