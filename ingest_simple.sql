-- Simple SQL script to ingest sample data for testing
USE vendora_db;

-- Insert sample categories (from categories.csv)
INSERT INTO categories (id, name, parent_category_id, level, slug, created_at, updated_at)
VALUES
(7896251, 'Eye Shadow Stick', NULL, 0, 'eye-shadow-stick', NOW(), NOW()),
(1331481, 'Blackout Curtains', NULL, 0, 'blackout-curtains', NOW(), NOW()),
(136318, 'Plus Size Tops', NULL, 0, 'plus-size-tops', NOW(), NOW()),
(1085666, 'Beauty', NULL, 0, 'beauty', NOW(), NOW()),
(5438, 'Clothing', NULL, 0, 'clothing', NOW(), NOW()),
(4044, 'Home', NULL, 0, 'home', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert sample product 1 (Laura Mercier Eye Shadow)
INSERT INTO products (sku, name, description, category_id, supplier_id, base_price, minimum_order_quantity, unit, status, created_at, updated_at)
VALUES
('173530386', 'Laura Mercier Caviar Stick Eye Color', 'Cream eye shadow stick with 12-hour wear', 7896251, 1, 22.90, 50, 'gram/ounce', 'ACTIVE', NOW(), NOW());

SET @product1_id = LAST_INSERT_ID();

-- Insert variants for product 1 (colors)
INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES
(@product1_id, '173530386-Sugar-Frost', 'Sugar Frost', NULL, 0.00);

SET @var1_1 = LAST_INSERT_ID();

INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES
(@product1_id, '173530386-Tuxedo', 'Tuxedo', NULL, 0.00);

SET @var1_2 = LAST_INSERT_ID();

-- Insert inventory for product 1 variants
INSERT INTO inventory (supplier_id, product_id, variant_id, available_quantity, reserved_quantity, status, last_updated)
VALUES
(1, @product1_id, @var1_1, 45, 0, 'AVAILABLE', NOW()),
(1, @product1_id, @var1_2, 60, 0, 'AVAILABLE', NOW());

-- Insert sample product 2 (Blackout Curtains)
INSERT INTO products (sku, name, description, category_id, supplier_id, base_price, minimum_order_quantity, unit, status, created_at, updated_at)
VALUES
('430528189', 'Exultantex Grey Blackout Curtains', 'Pom Pom Thermal Window Curtains 50W x 95L 2 Panels', 1331481, 1, 47.88, 90, 'pair', 'ACTIVE', NOW(), NOW());

SET @product2_id = LAST_INSERT_ID();

-- Insert variants for product 2 (sizes and colors)
INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES (@product2_id, '430528189-Black-50x54', 'Black', '50" x 54"', 0.00);
SET @var2_1 = LAST_INSERT_ID();

INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES (@product2_id, '430528189-Blue-50x63', 'Blue', '50" x 63"', 0.00);
SET @var2_2 = LAST_INSERT_ID();

INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES (@product2_id, '430528189-Gray-50x84', 'Gray', '50" x 84"', 0.00);
SET @var2_3 = LAST_INSERT_ID();

INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
VALUES (@product2_id, '430528189-Pink-50x95', 'Pink', '50" x 95"', 0.00);
SET @var2_4 = LAST_INSERT_ID();

-- Insert inventory for product 2 variants
INSERT INTO inventory (supplier_id, product_id, variant_id, available_quantity, reserved_quantity, status, last_updated)
VALUES
(1, @product2_id, @var2_1, 20, 0, 'AVAILABLE', NOW()),
(1, @product2_id, @var2_2, 0, 0, 'OUT_OF_STOCK', NOW()),
(1, @product2_id, @var2_3, 35, 0, 'AVAILABLE', NOW()),
(1, @product2_id, @var2_4, 50, 0, 'AVAILABLE', NOW());

-- Insert sample product 3 (No variants - simple product)
INSERT INTO products (sku, name, description, category_id, supplier_id, base_price, minimum_order_quantity, unit, status, created_at, updated_at)
VALUES
('161657830', '100% Cotton King Duvet Set', 'Simply Put Distress Tile Print 3 Piece', 4044, 1, 49.99, 70, 'set', 'ACTIVE', NOW(), NOW());

SET @product3_id = LAST_INSERT_ID();

-- Insert inventory for product 3 (no variants)
INSERT INTO inventory (supplier_id, product_id, variant_id, available_quantity, reserved_quantity, status, last_updated)
VALUES
(1, @product3_id, NULL, 100, 0, 'AVAILABLE', NOW());

-- Show results
SELECT 'Categories:' as '';
SELECT COUNT(*) as total_categories FROM categories;

SELECT 'Products:' as '';
SELECT id, sku, name, base_price, unit FROM products;

SELECT 'Variants:' as '';
SELECT pv.id, p.name as product_name, pv.color, pv.size, pv.sku
FROM product_variants pv
JOIN products p ON pv.product_id = p.id;

SELECT 'Inventory:' as '';
SELECT i.id, p.name as product_name, pv.color, pv.size, i.available_quantity, i.status
FROM inventory i
JOIN products p ON i.product_id = p.id
LEFT JOIN product_variants pv ON i.variant_id = pv.id;
