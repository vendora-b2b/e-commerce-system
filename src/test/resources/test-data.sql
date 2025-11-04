-- Test data for Order integration tests
-- Creates a test supplier and retailer to satisfy foreign key constraints

INSERT INTO suppliers (id, name, email, phone, address, profile_description, business_license, rating, verified)
VALUES (999, 'Test Supplier Inc', 'test-supplier@test.com', '555-0100', '123 Supplier St', 'Test supplier', 'LIC-TEST-999', NULL, false);

INSERT INTO retailers (id, name, email, phone, address, profile_description, business_license, loyalty_tier, credit_limit, total_purchase_amount, loyalty_points, account_status)
VALUES (999, 'Test Retailer Corp', 'test-retailer@test.com', '555-0200', '456 Retailer Ave', 'Test retailer', 'BUS-TEST-999', 'BRONZE', 0.0, 0.0, 0, 'ACTIVE');

-- Create test products for order items
INSERT INTO products (id, sku, name, description, category, supplier_id, base_price, minimum_order_quantity, unit, status, created_at, updated_at)
VALUES 
    (100, 'TEST-PROD-100', 'Test Product A', 'Test product for integration tests', 'Test Category', 999, 29.99, 1, 'PIECE', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (101, 'TEST-PROD-101', 'Test Product B', 'Another test product', 'Test Category', 999, 49.99, 1, 'PIECE', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
