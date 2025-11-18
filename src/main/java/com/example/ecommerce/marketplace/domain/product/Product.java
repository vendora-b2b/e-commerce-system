package com.example.ecommerce.marketplace.domain.product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a product entity in the e-commerce marketplace.
 * Products are offered by suppliers and can be purchased by retailers.
 * This is an aggregate root that manages product information, pricing, and variants.
 */
public class Product {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9-]{5,50}$");
    private static final Double MIN_PRICE = 0.01;
    private static final Integer MIN_MOQ = 1;
    private static final Integer MAX_MOQ = 1000000;

    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private Long supplierId;
    private Double basePrice;
    private Integer minimumOrderQuantity;
    private String unit; // e.g., "piece", "box", "kg", "liter"
    private List<String> images;
    private List<ProductVariant> variants;
    private List<PriceTier> priceTiers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Product() {
        this.images = new ArrayList<>();
        this.variants = new ArrayList<>();
        this.priceTiers = new ArrayList<>();
    }

    // Full constructor
    public Product(Long id, String sku, String name, String description, Long categoryId,
                   Long supplierId, Double basePrice, Integer minimumOrderQuantity, String unit,
                   List<String> images, List<ProductVariant> variants, List<PriceTier> priceTiers,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.unit = unit;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
        this.priceTiers = priceTiers != null ? new ArrayList<>(priceTiers) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Validates the SKU format.
     * SKU must be 5-50 characters, uppercase alphanumeric with hyphens allowed.
     * @return true if SKU is valid, false otherwise
     */
    public boolean validateSku() {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        return SKU_PATTERN.matcher(sku.trim()).matches();
    }

    /**
     * Validates the product name.
     * Name must not be null or empty and should be between 3-200 characters.
     * @return true if name is valid, false otherwise
     */
    public boolean validateName() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        int length = name.trim().length();
        return length >= 3 && length <= 200;
    }

    /**
     * Validates the base price.
     * Price must be positive and greater than minimum allowed price.
     * @return true if price is valid, false otherwise
     */
    public boolean validatePrice() {
        return basePrice != null && basePrice >= MIN_PRICE;
    }

    /**
     * Validates the minimum order quantity.
     * MOQ must be positive and within allowed range.
     * @return true if MOQ is valid, false otherwise
     */
    public boolean validateMinimumOrderQuantity() {
        return minimumOrderQuantity != null && 
               minimumOrderQuantity >= MIN_MOQ && 
               minimumOrderQuantity <= MAX_MOQ;
    }

    /**
     * Validates the supplier ID.
     * Supplier ID must not be null.
     * @return true if supplier ID is valid, false otherwise
     */
    public boolean validateSupplierId() {
        return supplierId != null && supplierId > 0;
    }

    /**
     * Checks if the order quantity meets the minimum requirement.
     * @param quantity the quantity to validate
     * @return true if meets minimum, false otherwise
     */
    public boolean meetsMinimumOrderQuantity(Integer quantity) {
        if (quantity == null || minimumOrderQuantity == null) {
            return false;
        }
        return quantity >= minimumOrderQuantity;
    }

    /**
     * Calculates the price for a given quantity considering price tiers.
     * Falls back to base price if no tiers are defined.
     * @param quantity the quantity to calculate price for
     * @return calculated total price
     */
    public Double calculatePriceForQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!meetsMinimumOrderQuantity(quantity)) {
            throw new IllegalArgumentException(
                "Quantity does not meet minimum order quantity of " + minimumOrderQuantity
            );
        }

        PriceTier applicableTier = getPriceTierForQuantity(quantity);
        
        if (applicableTier != null) {
            return quantity * applicableTier.calculatePricePerUnit(basePrice);
        }

        return quantity * basePrice;
    }

    /**
     * Gets the applicable price tier for a given quantity.
     * Returns the tier with the lowest price that matches the quantity.
     * @param quantity the quantity to check
     * @return the applicable price tier, or null if no tiers apply
     */
    public PriceTier getPriceTierForQuantity(Integer quantity) {
        if (quantity == null || priceTiers == null || priceTiers.isEmpty()) {
            return null;
        }

        return priceTiers.stream()
            .filter(tier -> quantity >= tier.getMinQuantity())
            .filter(tier -> tier.getMaxQuantity() == null || quantity <= tier.getMaxQuantity())
            .min((t1, t2) -> Double.compare(t1.calculatePricePerUnit(basePrice), t2.calculatePricePerUnit(basePrice)))
            .orElse(null);
    }

    /**
     * Calculates the unit price for a given quantity (considering tiers).
     * @param quantity the quantity to check
     * @return the unit price for the given quantity
     */
    public Double getUnitPriceForQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return basePrice;
        }

        PriceTier tier = getPriceTierForQuantity(quantity);
        return tier != null ? tier.calculatePricePerUnit(basePrice) : basePrice;
    }

    /**
     * Checks if the product has price tiers for bulk pricing.
     * @return true if price tiers exist, false otherwise
     */
    public boolean hasPriceTiers() {
        return priceTiers != null && !priceTiers.isEmpty();
    }

    /**
     * Checks if the product has variants.
     * @return true if product has variants, false otherwise
     */
    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    /**
     * Updates product information.
     * @param name product name
     * @param description product description
     * @param categoryId product category ID
     * @param unit unit of measurement
     */
    public void updateProductInfo(String name, String description, Long categoryId, String unit) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        if (unit != null && !unit.trim().isEmpty()) {
            this.unit = unit.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the base price.
     * @param newPrice the new base price
     */
    public void updateBasePrice(Double newPrice) {
        if (newPrice == null || newPrice < MIN_PRICE) {
            throw new IllegalArgumentException("Price must be at least " + MIN_PRICE);
        }
        this.basePrice = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the minimum order quantity.
     * @param newMoq the new minimum order quantity
     */
    public void updateMinimumOrderQuantity(Integer newMoq) {
        if (newMoq == null || newMoq < MIN_MOQ || newMoq > MAX_MOQ) {
            throw new IllegalArgumentException(
                "MOQ must be between " + MIN_MOQ + " and " + MAX_MOQ
            );
        }
        this.minimumOrderQuantity = newMoq;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds an image to the product.
     * @param imageUrl the image URL to add
     */
    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (this.images == null) {
                this.images = new ArrayList<>();
            }
            if (!this.images.contains(imageUrl.trim())) {
                this.images.add(imageUrl.trim());
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Removes an image from the product.
     * @param imageUrl the image URL to remove
     */
    public void removeImage(String imageUrl) {
        if (this.images != null && imageUrl != null) {
            this.images.remove(imageUrl.trim());
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Adds a price tier to the product.
     * @param priceTier the price tier to add
     */
    public void addPriceTier(PriceTier priceTier) {
        if (priceTier == null) {
            throw new IllegalArgumentException("Price tier cannot be null");
        }
        if (this.priceTiers == null) {
            this.priceTiers = new ArrayList<>();
        }
        this.priceTiers.add(priceTier);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a price tier from the product.
     * @param priceTier the price tier to remove
     */
    public void removePriceTier(PriceTier priceTier) {
        if (this.priceTiers != null && priceTier != null) {
            this.priceTiers.remove(priceTier);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Clears all price tiers.
     */
    public void clearPriceTiers() {
        if (this.priceTiers != null) {
            this.priceTiers.clear();
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Adds a variant to the product.
     * @param variant the variant to add
     */
    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        if (this.variants == null) {
            this.variants = new ArrayList<>();
        }
        this.variants.add(variant);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a variant from the product.
     * @param variant the variant to remove
     */
    public void removeVariant(ProductVariant variant) {
        if (this.variants != null && variant != null) {
            this.variants.remove(variant);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Finds a variant by color and size.
     * @param color the color (can be null)
     * @param size the size (can be null)
     * @return the matching variant, or null if not found
     */
    public ProductVariant findVariant(String color, String size) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream()
            .filter(v -> {
                boolean colorMatch = (color == null && v.getColor() == null) ||
                                   (color != null && color.equals(v.getColor()));
                boolean sizeMatch = (size == null && v.getSize() == null) ||
                                  (size != null && size.equals(v.getSize()));
                return colorMatch && sizeMatch;
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets all unique colors from variants.
     * @return list of unique colors
     */
    public List<String> getAvailableColors() {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        return variants.stream()
            .map(ProductVariant::getColor)
            .filter(color -> color != null)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Gets all unique sizes from variants.
     * @return list of unique sizes
     */
    public List<String> getAvailableSizes() {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        return variants.stream()
            .map(ProductVariant::getSize)
            .filter(size -> size != null)
            .distinct()
            .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public List<PriceTier> getPriceTiers() {
        return priceTiers;
    }

    public void setPriceTiers(List<PriceTier> priceTiers) {
        this.priceTiers = priceTiers;
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
