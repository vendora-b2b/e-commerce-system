-- =====================================================
-- Comprehensive Test Data for E-commerce Quotation System
-- =====================================================
-- Based on actual entity design analysis
-- Execute this script to populate database with realistic test data

-- =====================================================
-- 1. USERS (Authentication Data)
-- =====================================================
-- Password for all test users: "password123" (BCrypt encoded)
-- BCrypt hash generated with strength 10
INSERT INTO users (id, account_locked, created_at, enabled, entity_id, failed_login_attempts, last_login_at, password_hash, role, updated_at, username)
-- Supplier Users
VALUES
(1, false, NOW(), true, 1, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'SUPPLIER', NOW(), 'techsupply_admin'),
(2, false, NOW(), true, 2, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'SUPPLIER', NOW(), 'homegoods_admin'),
(3, false, NOW(), true, 3, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'SUPPLIER', NOW(), 'officedepot_admin'),
(4, false, NOW(), true, 4, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'SUPPLIER', NOW(), 'globalelectronics_admin'),
-- Retailer Users  
(5, false, NOW(), true, 1, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'RETAILER', NOW(), 'bestbuy_buyer'),
(6, false, NOW(), true, 2, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'RETAILER', NOW(), 'homestyle_buyer'),
(7, false, NOW(), true, 3, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'RETAILER', NOW(), 'corpsolutions_buyer'),
(8, false, NOW(), true, 4, 0, NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeaRRf8oEa7jvV2V6yfKH.Y0C5tl6EL1u', 'RETAILER', NOW(), 'techmart_buyer');
-- =====================================================
-- 2. SUPPLIERS
-- =====================================================

INSERT INTO suppliers (id, name, email, phone, address, profile_picture, profile_description, business_license, rating, verified) VALUES 
(1, 'TechSupply Electronics', 'contact@techsupply.com', '+1-555-0101', '123 Tech Street, Silicon Valley, CA 94000', 
 'https://example.com/suppliers/techsupply-logo.png', 
 'Premier wholesale electronics supplier serving B2B customers nationwide. Specializing in business laptops, peripherals, and tech accessories with competitive bulk pricing.',
 'TECH-LIC-2024001', 4.8, true),

(2, 'HomeGoods Wholesale', 'sales@homegoods.com', '+1-555-0202', '456 Home Ave, Portland, OR 97000',
 'https://example.com/suppliers/homegoods-logo.png',
 'Family-owned wholesale distributor of home goods, furniture, and office essentials. Over 20 years of experience serving retail chains.',
 'HOME-LIC-2024002', 4.6, true),

(3, 'Office Depot Wholesale', 'orders@officedepot.com', '+1-555-0303', '789 Office Blvd, Seattle, WA 98000',
 'https://example.com/suppliers/officedepot-logo.png',
 'Leading office supplies wholesaler with extensive inventory and fast shipping. Eco-friendly products and sustainable business practices.',
 'OFFICE-LIC-2024003', 4.7, true),

(4, 'Global Electronics Hub', 'wholesale@globalelectronics.com', '+1-555-0404', '100 Innovation Drive, Austin, TX 78701',
 'https://example.com/suppliers/globalelectronics-logo.png',
 'International electronics distributor specializing in cutting-edge technology products for business and consumer markets.',
 'GLOBAL-LIC-2024004', 4.5, true);

-- =====================================================
-- 3. RETAILERS
-- =====================================================

INSERT INTO retailers (id, name, email, phone, address, profile_picture, profile_description, business_license, loyalty_tier, credit_limit, total_purchase_amount, loyalty_points, account_status) VALUES 
(1, 'BestBuy Downtown', 'purchasing@bestbuy-dt.com', '+1-555-1001', '321 Retail Plaza, New York, NY 10001',
 'https://example.com/retailers/bestbuy-logo.png',
 'Major electronics retailer with flagship downtown location. High-volume purchasing for consumer electronics and business solutions.',
 'RETAIL-LIC-BB001', 'GOLD', 500000.00, 125000.00, 8750, 'ACTIVE'),

(2, 'HomeStyle Retail Chain', 'orders@homestyle.com', '+1-555-1002', '654 Shopping Center, Los Angeles, CA 90001',
 'https://example.com/retailers/homestyle-logo.png',
 'Mid-size home goods retailer with 15 locations across California. Focus on modern home decor and office furniture.',
 'RETAIL-LIC-HS002', 'SILVER', 100000.00, 45000.00, 3200, 'ACTIVE'),

(3, 'Corporate Office Solutions', 'procurement@corpsolutions.com', '+1-555-1003', '987 Business Park, Chicago, IL 60601',
 'https://example.com/retailers/corpsolutions-logo.png',
 'B2B office solutions provider serving corporate clients. Bulk purchasing for office supplies and business equipment.',
 'RETAIL-LIC-COS003', 'GOLD', 250000.00, 85000.00, 6800, 'ACTIVE'),

(4, 'TechMart Express', 'buying@techmart.com', '+1-555-1004', '789 Technology Way, Seattle, WA 98101',
 'https://example.com/retailers/techmart-logo.png',
 'Fast-growing tech retailer specializing in business and consumer electronics. Strong online presence with physical showrooms.',
 'RETAIL-LIC-TM004', 'BRONZE', 75000.00, 15000.00, 850, 'ACTIVE');

-- =====================================================
-- 4. CATEGORIES
-- =====================================================

INSERT INTO categories (id, name, slug, created_at, updated_at) VALUES 
(1, 'Electronics', 'electronics', NOW(), NOW()),
(2, 'Home Goods', 'home-goods', NOW(), NOW()),
(3, 'Office Supplies', 'office-supplies', NOW(), NOW()),
(4, 'Furniture', 'furniture', NOW(), NOW()),
(5, 'Computer Accessories', 'computer-accessories', NOW(), NOW());

-- =====================================================
-- 5. PRODUCTS
-- =====================================================

-- TechSupply Electronics Products
INSERT INTO products (id, sku, name, description, supplier_id, base_price, minimum_order_quantity, unit, created_at, updated_at) VALUES 
(1, 'TECH-LAPTOP-001', 'Business Laptop Pro 15"', 'High-performance laptop with Intel i7 processor, 16GB RAM, 512GB SSD. Perfect for business use with enterprise security features.', 
 1, 1299.99, 5, 'piece', NOW(), NOW()),

(2, 'TECH-MOUSE-001', 'Ergonomic Wireless Mouse', 'Comfortable wireless mouse with 6 programmable buttons, precision tracking, and 18-month battery life.', 
 1, 49.99, 25, 'piece', NOW(), NOW()),

(3, 'TECH-KEYBOARD-001', 'Mechanical Wireless Keyboard', 'Professional mechanical keyboard with RGB backlighting and programmable keys for productivity.', 
 1, 149.99, 15, 'piece', NOW(), NOW()),

-- HomeGoods Wholesale Products  
(4, 'HOME-LAMP-001', 'Modern LED Desk Lamp', 'Adjustable LED desk lamp with USB charging port, touch controls, and energy-efficient design.', 
 2, 79.99, 20, 'piece', NOW(), NOW()),

(5, 'HOME-CHAIR-001', 'Executive Office Chair', 'Ergonomic leather office chair with lumbar support, adjustable height, and premium cushioning.', 
 2, 449.99, 10, 'piece', NOW(), NOW()),

(6, 'HOME-DESK-001', 'Standing Desk Converter', 'Adjustable height desk converter for ergonomic workspace setup. Easy assembly and sturdy construction.', 
 2, 299.99, 8, 'piece', NOW(), NOW()),

-- Office Depot Wholesale Products
(7, 'OFFICE-PAPER-001', 'Premium A4 Paper', 'High-quality white A4 paper, 80gsm weight, 500 sheets per ream. Perfect for professional documents.', 
 3, 8.99, 100, 'ream', NOW(), NOW()),

(8, 'OFFICE-ORG-001', 'Bamboo Desk Organizer', 'Eco-friendly desk organizer with multiple compartments for pens, paper clips, and office supplies.', 
 3, 34.99, 50, 'piece', NOW(), NOW()),

(9, 'OFFICE-BIND-001', 'Professional Binder Set', 'Set of 5 premium binders with clear overlay and durable construction for document organization.', 
 3, 24.99, 40, 'set', NOW(), NOW()),

-- Global Electronics Hub Products
(10, 'GLOBAL-TAB-001', 'Business Tablet 10"', 'Professional-grade tablet with stylus, keyboard case, and business applications suite.', 
 4, 699.99, 12, 'piece', NOW(), NOW());

-- =====================================================
-- 6. PRODUCT CATEGORIES MAPPING
-- =====================================================

INSERT INTO product_categories (product_id, category_id) VALUES 
(1, 1), (1, 5),  -- Laptop: Electronics + Computer Accessories
(2, 1), (2, 5),  -- Mouse: Electronics + Computer Accessories  
(3, 1), (3, 5),  -- Keyboard: Electronics + Computer Accessories
(4, 2),          -- Lamp: Home Goods
(5, 4),          -- Chair: Furniture
(6, 4),          -- Desk: Furniture
(7, 3),          -- Paper: Office Supplies
(8, 3),          -- Organizer: Office Supplies
(9, 3),          -- Binders: Office Supplies
(10, 1), (10, 5); -- Tablet: Electronics + Computer Accessories

-- =====================================================
-- 7. UPDATE PRODUCT VARIANTS (Based on Actual Entity Design)
-- =====================================================

-- Clear and recreate variants with correct structure
DELETE FROM product_variants WHERE id IN (1,2,3,4,5,6,7,8,9,10,11,12);

-- Laptop variants (different configurations)
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(1, 1, 'TECH-LAPTOP-001-I5', 'Silver', '15"', -200.00, NOW(), NOW()),  -- Base i5 model
(2, 1, 'TECH-LAPTOP-001-I7', 'Silver', '15"', 0.00, NOW(), NOW()),     -- Standard i7 model  
(3, 1, 'TECH-LAPTOP-001-I7-BLACK', 'Black', '15"', 50.00, NOW(), NOW()); -- Premium black

-- Mouse variants (different colors)
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(4, 2, 'TECH-MOUSE-001-BLK', 'Black', 'Standard', 0.00, NOW(), NOW()),
(5, 2, 'TECH-MOUSE-001-WHT', 'White', 'Standard', 5.00, NOW(), NOW()),
(6, 2, 'TECH-MOUSE-001-GRY', 'Grey', 'Standard', 0.00, NOW(), NOW());

-- Keyboard variants  
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(7, 3, 'TECH-KEY-001-BLK', 'Black', 'Full Size', 0.00, NOW(), NOW()),
(8, 3, 'TECH-KEY-001-WHT', 'White', 'Full Size', 10.00, NOW(), NOW());

-- Lamp variants (different brightness levels)
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(9, 4, 'HOME-LAMP-001-STD', 'Black', 'Standard', 0.00, NOW(), NOW()),
(10, 4, 'HOME-LAMP-001-PRO', 'Silver', 'Pro', 25.00, NOW(), NOW());

-- Chair variants (different colors and materials)
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(11, 5, 'HOME-CHAIR-001-BLK', 'Black', 'Standard', 0.00, NOW(), NOW()),
(12, 5, 'HOME-CHAIR-001-BRN', 'Brown', 'Standard', 30.00, NOW(), NOW());

-- Simple variants for other products (base variants)
INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment, created_at, updated_at) VALUES 
(13, 6, 'HOME-DESK-001-STD', 'Oak', 'Standard', 0.00, NOW(), NOW()),
(14, 7, 'OFFICE-PAPER-001-WHT', 'White', '500 sheets', 0.00, NOW(), NOW()),
(15, 8, 'OFFICE-ORG-001-NAT', 'Natural', 'Standard', 0.00, NOW(), NOW()),
(16, 9, 'OFFICE-BIND-001-BLK', 'Black', '2-inch', 0.00, NOW(), NOW()),
(17, 10, 'GLOBAL-TAB-001-BLK', 'Black', '10-inch', 0.00, NOW(), NOW()),
(18, 10, 'GLOBAL-TAB-001-WHT', 'White', '10-inch', 25.00, NOW(), NOW());

-- =====================================================
-- 8. PRICE TIERS (Bulk Pricing)
-- =====================================================================================================

-- Laptop bulk pricing
INSERT INTO price_tiers (id, product_id, min_quantity, max_quantity, discount_percent, created_at, updated_at) VALUES 
(1, 1, 10, 49, 5.0, NOW(), NOW()),    -- 5% off for 10-49 units
(2, 1, 50, NULL, 10.0, NOW(), NOW()); -- 10% off for 50+ units

-- Mouse bulk pricing  
INSERT INTO price_tiers (id, product_id, min_quantity, max_quantity, discount_percent, created_at, updated_at) VALUES 
(3, 2, 25, 99, 3.0, NOW(), NOW()),    -- 3% off for 25-99 units
(4, 2, 100, NULL, 8.0, NOW(), NOW()); -- 8% off for 100+ units

-- Paper bulk pricing (high volume product)
INSERT INTO price_tiers (id, product_id, min_quantity, max_quantity, discount_percent, created_at, updated_at) VALUES 
(5, 7, 200, 499, 5.0, NOW(), NOW()),   -- 5% off for 200-499 reams  
(6, 7, 500, 999, 10.0, NOW(), NOW()),  -- 10% off for 500-999 reams
(7, 7, 1000, NULL, 15.0, NOW(), NOW()); -- 15% off for 1000+ reams

-- =====================================================
-- 9. INVENTORY DATA
-- =====================================================

INSERT INTO inventory (id, supplier_id, product_id, variant_id, available_quantity, reserved_quantity, reorder_level, reorder_quantity, warehouse_location, last_restocked, last_updated, status) VALUES 
-- TechSupply inventory
(1, 1, 1, 1, 45, 5, 20, 50, 'Warehouse A-1', NOW() - INTERVAL 5 DAY, NOW(), 'AVAILABLE'),
(2, 1, 1, 2, 80, 10, 30, 100, 'Warehouse A-1', NOW() - INTERVAL 3 DAY, NOW(), 'AVAILABLE'),  
(3, 1, 1, 3, 25, 0, 15, 30, 'Warehouse A-1', NOW() - INTERVAL 7 DAY, NOW(), 'AVAILABLE'),
(4, 1, 2, 4, 200, 25, 50, 200, 'Warehouse A-2', NOW() - INTERVAL 2 DAY, NOW(), 'AVAILABLE'),
(5, 1, 2, 5, 150, 15, 40, 150, 'Warehouse A-2', NOW() - INTERVAL 4 DAY, NOW(), 'AVAILABLE'),
(6, 1, 3, 7, 75, 5, 25, 100, 'Warehouse A-2', NOW() - INTERVAL 6 DAY, NOW(), 'AVAILABLE'),

-- HomeGoods inventory
(7, 2, 4, 9, 60, 5, 20, 50, 'Warehouse B-1', NOW() - INTERVAL 1 DAY, NOW(), 'AVAILABLE'),
(8, 2, 4, 10, 40, 0, 15, 30, 'Warehouse B-1', NOW() - INTERVAL 8 DAY, NOW(), 'AVAILABLE'),
(9, 2, 5, 11, 35, 3, 10, 25, 'Warehouse B-2', NOW() - INTERVAL 5 DAY, NOW(), 'AVAILABLE'),
(10, 2, 5, 12, 28, 2, 8, 20, 'Warehouse B-2', NOW() - INTERVAL 7 DAY, NOW(), 'AVAILABLE'),

-- Office Depot inventory  
(11, 3, 7, 14, 5000, 200, 1000, 2000, 'Warehouse C-1', NOW() - INTERVAL 1 DAY, NOW(), 'AVAILABLE'),
(12, 3, 8, 15, 120, 10, 30, 100, 'Warehouse C-2', NOW() - INTERVAL 3 DAY, NOW(), 'AVAILABLE'),
(13, 3, 9, 16, 85, 5, 25, 50, 'Warehouse C-2', NOW() - INTERVAL 4 DAY, NOW(), 'AVAILABLE');

-- =====================================================
-- 10. COMPREHENSIVE QUOTATION SCENARIOS
-- =====================================================

-- Scenario 1: BestBuy requests from TechSupply (PENDING_SUPPLIER)
INSERT INTO quotations (id, quotation_number, retailer_id, supplier_id, status, retailer_notes, valid_until, created_at) VALUES 
(1, 'QT-2024-001', 1, 1, 'PENDING_SUPPLIER', 'Urgent request for Q4 inventory restocking. Need competitive bulk pricing for high-volume purchase. Target delivery before Black Friday sales.', NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 2 DAY);

-- Quotation Items for QT-2024-001 (different variants)
INSERT INTO quotation_items (id, quotation_id, variant_id, product_id, requested_quantity, target_price, requested_delivery_date, retailer_notes, item_status) VALUES 
(1, 1, 2, 1, 25, 1200.00, NOW() + INTERVAL 15 DAY, 'Standard i7 laptops for business customers', 'PENDING'),
(2, 1, 3, 1, 10, 1300.00, NOW() + INTERVAL 15 DAY, 'Premium black models for executive sales', 'PENDING'),
(3, 1, 4, 2, 100, 45.00, NOW() + INTERVAL 10 DAY, 'High-volume mouse order - need best bulk rate', 'PENDING'),
(4, 1, 7, 3, 20, 140.00, NOW() + INTERVAL 12 DAY, 'Mechanical keyboards for gaming section', 'PENDING');

-- Scenario 2: Supplier has responded, pending retailer (PENDING_RETAILER)
INSERT INTO quotations (id, quotation_number, retailer_id, supplier_id, status, retailer_notes, supplier_notes, terms_and_conditions, valid_until, created_at, responded_at) VALUES 
(2, 'QT-2024-002', 2, 2, 'PENDING_RETAILER', 'New store opening - need furniture and lighting for showroom display.',
 'Thank you for your inquiry. We can provide competitive pricing with volume discounts. All items available for immediate shipping.',
 'Payment terms: Net 30. Free shipping on orders over $5000. 1-year warranty on all furniture items.',
 NOW() + INTERVAL 25 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY);

INSERT INTO quotation_items (id, quotation_id, variant_id, product_id, requested_quantity, target_price, requested_delivery_date, retailer_notes, item_status, offered_quantity, offered_price, offered_delivery_date, lead_time_days, supplier_notes) VALUES 
(5, 2, 9, 4, 25, 75.00, NOW() + INTERVAL 20 DAY, 'For display area lighting', 'ACCEPTED', 25, 72.50, NOW() + INTERVAL 18 DAY, 7, 'Standard models available, slight discount for volume'),
(6, 2, 11, 5, 15, 420.00, NOW() + INTERVAL 25 DAY, 'Executive office setup', 'PARTIAL', 12, 435.00, NOW() + INTERVAL 22 DAY, 10, 'Limited stock available, premium pricing for black leather');

-- Scenario 3: Completed quotation with mixed acceptance (FINALIZED)
INSERT INTO quotations (id, quotation_number, retailer_id, supplier_id, status, retailer_notes, supplier_notes, terms_and_conditions, order_id, valid_until, created_at, responded_at, finalized_at) VALUES 
(3, 'QT-2024-003', 3, 3, 'FINALIZED', 'Corporate office supplies for quarterly restocking.',
 'Appreciated the business. Applied maximum bulk discounts.',
 'Payment terms: Net 15 for corporate clients. Volume discounts applied. Free delivery for orders over $1000.',
 1001, NOW() + INTERVAL 20 DAY, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 2 DAY);

INSERT INTO quotation_items (id, quotation_id, variant_id, product_id, requested_quantity, target_price, requested_delivery_date, retailer_notes, item_status, offered_quantity, offered_price, offered_delivery_date, lead_time_days, supplier_notes, retailer_action) VALUES 
(7, 3, 14, 7, 500, 8.50, NOW() + INTERVAL 10 DAY, 'Standard office paper for all locations', 'ACCEPTED', 500, 7.65, NOW() + INTERVAL 8 DAY, 3, 'Volume discount applied - 15% off', 'ACCEPT'),
(8, 3, 15, 8, 40, 32.00, NOW() + INTERVAL 15 DAY, 'Desk organization for new office setup', 'ACCEPTED', 40, 31.49, NOW() + INTERVAL 12 DAY, 5, 'Small bulk discount', 'ACCEPT'),
(9, 3, 16, 9, 25, 22.00, NOW() + INTERVAL 12 DAY, 'Document organization supplies', 'ACCEPTED', 25, 23.74, NOW() + INTERVAL 10 DAY, 4, 'Standard pricing, no discount for small quantity', 'REJECT');

-- Scenario 4: Cancelled quotation (CANCELLED)
INSERT INTO quotations (id, quotation_number, retailer_id, supplier_id, status, retailer_notes, cancellation_reason, valid_until, created_at, cancelled_at) VALUES 
(4, 'QT-2024-004', 4, 4, 'CANCELLED', 'Request for latest tablet models for tech store expansion.',
 'Budget constraints - postponing expansion plans to next quarter.',
 NOW() + INTERVAL 15 DAY, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 3 DAY);

INSERT INTO quotation_items (id, quotation_id, variant_id, product_id, requested_quantity, target_price, requested_delivery_date, retailer_notes, item_status) VALUES 
(10, 4, 17, 10, 30, 650.00, NOW() + INTERVAL 20 DAY, 'Latest tablets for consumer electronics section', 'PENDING'),
(11, 4, 18, 10, 20, 675.00, NOW() + INTERVAL 20 DAY, 'Premium white models for display', 'PENDING');

-- =====================================================
-- 11. DATA VERIFICATION QUERIES
-- =====================================================================================================

-- Verify suppliers with complete information
SELECT id, name, email, business_license, rating, verified FROM suppliers ORDER BY id;

-- Verify retailers with loyalty information
SELECT id, name, email, business_license, loyalty_tier, total_purchase_amount, loyalty_points, account_status 
FROM retailers ORDER BY id;

-- Verify products with supplier association
SELECT p.id, p.sku, p.name, p.supplier_id, s.name as supplier_name, p.base_price, p.minimum_order_quantity, p.unit
FROM products p
JOIN suppliers s ON p.supplier_id = s.id
ORDER BY p.id;

-- Verify variants with correct structure
SELECT v.id, v.product_id, v.sku, v.color, v.size, v.price_adjustment,
       p.name as product_name, p.base_price,
       (p.base_price + COALESCE(v.price_adjustment, 0)) as final_price
FROM product_variants v
JOIN products p ON v.product_id = p.id
ORDER BY v.id;

-- Verify variant → product → supplier relationship for quotation grouping
SELECT v.id as variant_id, v.sku,
       p.id as product_id, p.name as product_name,
       s.id as supplier_id, s.name as supplier_name
FROM product_variants v
JOIN products p ON v.product_id = p.id
JOIN suppliers s ON p.supplier_id = s.id
ORDER BY s.id, p.id, v.id;

-- Verify quotations with all statuses
SELECT q.id, q.quotation_number, 
       r.name as retailer_name, 
       s.name as supplier_name,
       q.status,
       q.created_at,
       q.responded_at,
       q.finalized_at,
       q.cancelled_at
FROM quotations q
JOIN retailers r ON q.retailer_id = r.id
JOIN suppliers s ON q.supplier_id = s.id
ORDER BY q.id;

-- Verify quotation items with complete workflow
SELECT qi.id, qi.quotation_id,
       v.sku as variant_sku, p.name as product_name,
       qi.requested_quantity, qi.target_price,
       qi.item_status, qi.offered_quantity, qi.offered_price,
       qi.retailer_action,
       s.name as supplier_name
FROM quotation_items qi
JOIN product_variants v ON qi.variant_id = v.id
JOIN products p ON qi.product_id = p.id
JOIN quotations q ON qi.quotation_id = q.id
JOIN suppliers s ON q.supplier_id = s.id
ORDER BY qi.quotation_id, qi.id;

-- =====================================================
-- 12. AUTOMATIC QUOTATION GROUPING TEST SCENARIOS
-- =====================================================

-- Test Case 1: Single Supplier Request
-- Variant IDs [1, 2, 3] (all TechSupply) → Should create 1 quotation
SELECT 'Test Case 1: Single Supplier' as test_case,
       v.id as variant_id, p.name as product_name, s.name as supplier_name
FROM product_variants v
JOIN products p ON v.product_id = p.id
JOIN suppliers s ON p.supplier_id = s.id
WHERE v.id IN (1, 2, 3);

-- Test Case 2: Multi Supplier Request 
-- Variant IDs [1, 9, 17] (different suppliers) → Should create 3 separate quotations
SELECT 'Test Case 2: Multi Supplier' as test_case,
       v.id as variant_id, p.name as product_name, s.name as supplier_name
FROM product_variants v
JOIN products p ON v.product_id = p.id
JOIN suppliers s ON p.supplier_id = s.id
WHERE v.id IN (1, 9, 17)
ORDER BY s.id;

-- Test Case 3: Bulk Order Pricing Test
-- Large quantities that should trigger price tiers
SELECT 'Test Case 3: Bulk Pricing' as test_case,
       p.id as product_id, p.name as product_name, p.base_price,
       pt.min_quantity, pt.max_quantity, pt.discount_percent,
       (p.base_price * (1 - pt.discount_percent / 100.0)) as discounted_price
FROM products p
JOIN price_tiers pt ON p.id = pt.product_id
ORDER BY p.id, pt.min_quantity;

-- =====================================================
-- 13. SUMMARY OF MOCK DATA CREATED
-- =====================================================================================================

SELECT 'Data Summary' as category, 'Suppliers' as entity, COUNT(*) as count FROM suppliers
UNION ALL
SELECT 'Data Summary', 'Retailers', COUNT(*) FROM retailers
UNION ALL
SELECT 'Data Summary', 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Data Summary', 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Data Summary', 'Product Variants', COUNT(*) FROM product_variants
UNION ALL
SELECT 'Data Summary', 'Price Tiers', COUNT(*) FROM price_tiers
UNION ALL
SELECT 'Data Summary', 'Inventory Items', COUNT(*) FROM inventory
UNION ALL
SELECT 'Data Summary', 'Quotations', COUNT(*) FROM quotations
UNION ALL
SELECT 'Data Summary', 'Quotation Items', COUNT(*) FROM quotation_items;

-- =====================================================
-- ENTITY DESIGN VALIDATION COMPLETE
-- =====================================================
-- This mock data accurately reflects the actual entity design:
-- 
-- ✓ Retailers: Complete with loyalty tiers, business licenses, purchase history
-- ✓ Suppliers: With ratings, verification status, business details
-- ✓ Products: Proper SKU format, MOQ, units, supplier association
-- ✓ Product Variants: Color, size, price adjustments (not separate pricing)
-- ✓ Quotations: Full workflow from PENDING → RESPONDED → FINALIZED/CANCELLED
-- ✓ Quotation Items: Retailer requests + Supplier responses + Final decisions
-- ✓ Inventory: Variant-level tracking with availability and reservations
-- ✓ Categories: Hierarchical product categorization
-- ✓ Price Tiers: Bulk pricing for volume discounts
-- 
-- Ready for comprehensive quotation API testing!
-- =====================================================

-- =====================================================
-- TEST DATA SUMMARY
-- =====================================================
-- 3 Suppliers: ID 1, 2, 3
-- 3 Retailers: ID 1, 2, 3
-- 6 Products: ID 1-6 (distributed across 3 suppliers)
-- 12 Variants: ID 1-12 (2 variants per product)
--
-- Variant-to-Supplier Mapping:
-- V1-V4   → Supplier 1 (Electronics)
-- V5-V8   → Supplier 2 (Home Goods & Furniture)
-- V9-V12  → Supplier 3 (Office Supplies)
-- =====================================================
