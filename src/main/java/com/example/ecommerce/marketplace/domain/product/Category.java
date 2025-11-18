package com.example.ecommerce.marketplace.domain.product;

import java.time.LocalDateTime;

/**
 * Represents a product category in the e-commerce marketplace.
 * Categories can be hierarchical with parent-child relationships.
 */
public class Category {

    private Long id;
    private String name;
    private String slug; // URL-friendly name
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Category() {
    }

    // Full constructor
    public Category(Long id, String name, String slug,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Generates a URL-friendly slug from the category name.
     * @param name the category name
     * @return URL-friendly slug
     */
    public static String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    /**
     * Updates category information.
     * @param name new category name
     */
    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
            this.slug = generateSlug(name);
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
