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
    private String category;
    private Long supplierId;
    private Double basePrice;
    private Integer minimumOrderQuantity;
    private String unit; // e.g., "piece", "box", "kg", "liter"
    private List<String> images;
    private List<ProductVariant> variants;
    private List<PriceTier> priceTiers;
    private String status; // ACTIVE, INACTIVE, DISCONTINUED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Product() {
        this.images = new ArrayList<>();
        this.variants = new ArrayList<>();
        this.priceTiers = new ArrayList<>();
    }

    // Full constructor
    public Product(Long id, String sku, String name, String description, String category,
                   Long supplierId, Double basePrice, Integer minimumOrderQuantity, String unit,
                   List<String> images, List<ProductVariant> variants, List<PriceTier> priceTiers,
                   String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.supplierId = supplierId;
        this.basePrice = basePrice;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.unit = unit;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
        this.priceTiers = priceTiers != null ? new ArrayList<>(priceTiers) : new ArrayList<>();
        this.status = status;
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
            return quantity * applicableTier.getPricePerUnit();
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
            .min((t1, t2) -> Double.compare(t1.getPricePerUnit(), t2.getPricePerUnit()))
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
        return tier != null ? tier.getPricePerUnit() : basePrice;
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
     * Checks if the product is active.
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Checks if the product is discontinued.
     * @return true if status is DISCONTINUED, false otherwise
     */
    public boolean isDiscontinued() {
        return "DISCONTINUED".equals(status);
    }

    /**
     * Activates the product (sets status to ACTIVE).
     * Updates the timestamp.
     */
    public void activate() {
        if (!"DISCONTINUED".equals(this.status)) {
            this.status = "ACTIVE";
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot activate a discontinued product");
        }
    }

    /**
     * Deactivates the product (sets status to INACTIVE).
     * Updates the timestamp.
     */
    public void deactivate() {
        if (!"DISCONTINUED".equals(this.status)) {
            this.status = "INACTIVE";
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot deactivate a discontinued product");
        }
    }

    /**
     * Marks the product as discontinued.
     * This is typically irreversible.
     */
    public void discontinue() {
        this.status = "DISCONTINUED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates product information.
     * @param name product name
     * @param description product description
     * @param category product category
     * @param unit unit of measurement
     */
    public void updateProductInfo(String name, String description, String category, String unit) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (category != null && !category.trim().isEmpty()) {
            this.category = category.trim();
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
     * Finds a variant by name and value.
     * @param variantName the variant name
     * @param variantValue the variant value
     * @return the matching variant, or null if not found
     */
    public ProductVariant findVariant(String variantName, String variantValue) {
        if (variants == null || variantName == null || variantValue == null) {
            return null;
        }
        return variants.stream()
            .filter(v -> variantName.equals(v.getVariantName()) && 
                        variantValue.equals(v.getVariantValue()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets all variant names.
     * @return list of unique variant names
     */
    public List<String> getVariantNames() {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        return variants.stream()
            .map(ProductVariant::getVariantName)
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    /**
     * Inner class representing a product variant.
     * A variant is a distinct version of a product that shares the same base model but differs in one or more attributes
     * (e.g., color, size, style, or price).
     */
    public static class ProductVariant {
        private Long id;
        private String variantName;  // e.g., "Color", "Size"
        private String variantValue; // e.g., "Red", "Large"
        private Double priceAdjustment; // Additional cost or discount
        private List<String> images;

        public ProductVariant() {
            this.images = new ArrayList<>();
        }

        public ProductVariant(Long id, String variantName, String variantValue, 
                            Double priceAdjustment, List<String> images) {
            this.id = id;
            this.variantName = variantName;
            this.variantValue = variantValue;
            this.priceAdjustment = priceAdjustment;
            this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        }

        /**
         * Calculates the final price with variant adjustment.
         * @param basePrice the base product price
         * @return the adjusted price
         */
        public Double calculateAdjustedPrice(Double basePrice) {
            if (basePrice == null) {
                return null;
            }
            if (priceAdjustment == null) {
                return basePrice;
            }
            return basePrice + priceAdjustment;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public String getVariantValue() {
            return variantValue;
        }

        public void setVariantValue(String variantValue) {
            this.variantValue = variantValue;
        }

        public Double getPriceAdjustment() {
            return priceAdjustment;
        }

        public void setPriceAdjustment(Double priceAdjustment) {
            this.priceAdjustment = priceAdjustment;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }

    /**
     * Inner class representing a price tier for bulk pricing.
     * Defines discounted pricing based on quantity ranges.
     */
    public static class PriceTier {
        private Long id;
        private Integer minQuantity;
        private Integer maxQuantity; // null for unlimited
        private Double pricePerUnit;
        private Double discountPercent;

        public PriceTier() {
        }

        public PriceTier(Long id, Integer minQuantity, Integer maxQuantity, 
                        Double pricePerUnit, Double discountPercent) {
            this.id = id;
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.pricePerUnit = pricePerUnit;
            this.discountPercent = discountPercent;
        }

        /**
         * Checks if a quantity falls within this tier's range.
         * @param quantity the quantity to check
         * @return true if quantity is in range, false otherwise
         */
        public boolean isApplicableForQuantity(Integer quantity) {
            if (quantity == null || minQuantity == null) {
                return false;
            }
            boolean meetsMin = quantity >= minQuantity;
            boolean meetsMax = maxQuantity == null || quantity <= maxQuantity;
            return meetsMin && meetsMax;
        }

        /**
         * Calculates total price for a quantity at this tier.
         * @param quantity the quantity
         * @return total price
         */
        public Double calculateTotalPrice(Integer quantity) {
            if (quantity == null || pricePerUnit == null) {
                return null;
            }
            return quantity * pricePerUnit;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getMinQuantity() {
            return minQuantity;
        }

        public void setMinQuantity(Integer minQuantity) {
            this.minQuantity = minQuantity;
        }

        public Integer getMaxQuantity() {
            return maxQuantity;
        }

        public void setMaxQuantity(Integer maxQuantity) {
            this.maxQuantity = maxQuantity;
        }

        public Double getPricePerUnit() {
            return pricePerUnit;
        }

        public void setPricePerUnit(Double pricePerUnit) {
            this.pricePerUnit = pricePerUnit;
        }

        public Double getDiscountPercent() {
            return discountPercent;
        }

        public void setDiscountPercent(Double discountPercent) {
            this.discountPercent = discountPercent;
        }
    }
}
