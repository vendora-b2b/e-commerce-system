# Data Ingestion Guide

This guide explains how to ingest the sample product and category data into the MySQL database.

## Prerequisites

- Docker and Docker Compose installed
- MySQL client (optional, for manual queries)
- CSV data files: `products.csv` and `categories.csv`

## Quick Start

### Step 1: Start MySQL Database

```bash
docker-compose up -d
```

This will start the MySQL container with the database `vendora_db`.

**Verify MySQL is running:**
```bash
docker exec vendora-mysql mysqladmin ping -h localhost -uroot -pvendora_root_2025
```

You should see: `mysqld is alive`

### Step 2: Create Database Schema

The database schema is automatically created from the SQL migration script:

```bash
docker exec -i vendora-mysql mysql -uroot -pvendora_root_2025 < init-db/02-create-schema.sql
```

This creates all necessary tables:
- `categories` - Product categories with hierarchy support
- `products` - Main product table
- `product_variants` - Product variants (color, size combinations)
- `inventory` - Stock tracking at variant level
- `order_items` - Order items with variant support
- Supporting tables for images and price tiers

### Step 3: Ingest Sample Data

We provide a sample SQL script with 3 products to test the system:

```bash
docker exec -i vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db < ingest_simple.sql
```

**Sample data includes:**
- 6 categories
- 3 products with different variant configurations:
  1. Eye shadow (2 color variants)
  2. Curtains (4 color+size variants)
  3. Duvet set (no variants)

## Verify Data Ingestion

### Check Categories
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT id, name FROM categories LIMIT 10;"
```

### Check Products
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT p.id, p.sku, p.name, c.name as category, p.base_price
      FROM products p
      LEFT JOIN categories c ON p.category_id = c.id;"
```

### Check Product Variants
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT pv.id, p.name as product, pv.color, pv.size, pv.sku
      FROM product_variants pv
      JOIN products p ON pv.product_id = p.id;"
```

### Check Inventory Levels
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT p.name, pv.color, pv.size, i.available_quantity, i.status
      FROM inventory i
      JOIN products p ON i.product_id = p.id
      LEFT JOIN product_variants pv ON i.variant_id = pv.id;"
```

## Database Schema Overview

### Key Tables and Relationships

```
categories
├── id (PK)
├── name
├── parent_category_id (FK to categories)
├── level
└── slug

products
├── id (PK)
├── sku (UNIQUE)
├── name
├── description
├── category_id (FK to categories)
├── supplier_id
├── base_price
├── minimum_order_quantity
├── unit
└── status

product_variants
├── id (PK)
├── product_id (FK to products)
├── sku (UNIQUE)
├── color
├── size
└── price_adjustment

inventory
├── id (PK)
├── supplier_id
├── product_id (FK to products)
├── variant_id (FK to product_variants) -- NULL for products without variants
├── available_quantity
├── reserved_quantity
└── status
```

### Variant Handling

**Products can have:**
1. **No variants** - Simple products (e.g., Duvet Set)
   - Inventory tracked at product level (`variant_id = NULL`)

2. **Color variants only** - One dimension (e.g., Eye Shadow)
   - Creates one variant per color
   - Inventory tracked per variant

3. **Color + Size variants** - Two dimensions (e.g., Curtains)
   - Creates cartesian product (all combinations)
   - Inventory tracked per variant combination

## Ingesting Full CSV Data

To ingest the full `products.csv` and `categories.csv` files, you'll need to:

### Option 1: Using MySQL LOAD DATA (Categories Only)

```sql
-- Copy CSV file to container
docker cp categories.csv vendora-mysql:/tmp/categories.csv

-- Run import
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db -e "
  LOAD DATA INFILE '/tmp/categories.csv'
  INTO TABLE categories
  FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
  LINES TERMINATED BY '\n'
  IGNORE 1 ROWS
  (name, id)
  SET
    parent_category_id = NULL,
    level = 0,
    slug = LOWER(REPLACE(REPLACE(name, ' ', '-'), '\\'', '')),
    created_at = NOW(),
    updated_at = NOW();
"
```

### Option 2: Custom Script for Products (Recommended)

Products require complex processing to handle variants. Create a custom script that:

1. Parses `colors` and `sizes` JSON arrays from CSV
2. Generates variant combinations (cartesian product)
3. Creates unique variant SKUs
4. Inserts products, variants, and inventory records

**Example variant generation:**
```
Product: T-Shirt
Colors: ["Red", "Blue"]
Sizes: ["Small", "Large"]

Generated Variants:
1. Red-Small   (SKU: TSHIRT-Red-Small)
2. Red-Large   (SKU: TSHIRT-Red-Large)
3. Blue-Small  (SKU: TSHIRT-Blue-Small)
4. Blue-Large  (SKU: TSHIRT-Blue-Large)
```

## Reset Database

To start fresh:

```bash
# Clear all data (keeps structure)
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db -e "
  SET FOREIGN_KEY_CHECKS=0;
  TRUNCATE TABLE inventory;
  TRUNCATE TABLE product_variant_images;
  TRUNCATE TABLE product_variants;
  TRUNCATE TABLE product_images;
  TRUNCATE TABLE price_tiers;
  TRUNCATE TABLE products;
  TRUNCATE TABLE categories;
  SET FOREIGN_KEY_CHECKS=1;
"

# Drop and recreate schema
docker exec -i vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db -e "
  SET FOREIGN_KEY_CHECKS=0;
  DROP TABLE IF EXISTS inventory, order_items, product_variant_images,
                       product_variants, product_images, price_tiers,
                       products, categories;
  SET FOREIGN_KEY_CHECKS=1;
"

docker exec -i vendora-mysql mysql -uroot -pvendora_root_2025 < init-db/02-create-schema.sql
```

## Useful Queries

### Find products with low stock
```sql
SELECT p.name, pv.color, pv.size, i.available_quantity, i.status
FROM inventory i
JOIN products p ON i.product_id = p.id
LEFT JOIN product_variants pv ON i.variant_id = pv.id
WHERE i.status = 'OUT_OF_STOCK' OR i.available_quantity < 30;
```

### Get all variants for a specific product
```sql
SELECT pv.id, pv.sku, pv.color, pv.size, pv.price_adjustment
FROM product_variants pv
WHERE pv.product_id = 2;
```

### Check inventory by product
```sql
SELECT
  p.name,
  COALESCE(pv.color, 'N/A') as color,
  COALESCE(pv.size, 'N/A') as size,
  i.available_quantity,
  i.reserved_quantity,
  i.status
FROM inventory i
JOIN products p ON i.product_id = p.id
LEFT JOIN product_variants pv ON i.variant_id = pv.id
ORDER BY p.name;
```

## Troubleshooting

### MySQL Container Not Starting
```bash
# Check container status
docker ps -a | grep vendora-mysql

# Check logs
docker logs vendora-mysql

# Restart container
docker-compose restart mysql
```

### Permission Denied Errors
Make sure you're using the correct user:
- Root user: `vendora_root_2025`
- App user: `vendora_app` / `vendora_app_pass`

### Foreign Key Constraint Errors
Always disable foreign key checks when truncating:
```sql
SET FOREIGN_KEY_CHECKS=0;
-- your operations
SET FOREIGN_KEY_CHECKS=1;
```

### Duplicate Key Errors
If you see duplicate SKU errors, the data may already exist. Either:
1. Clear the table first (see Reset Database section)
2. Use `ON DUPLICATE KEY UPDATE` in your INSERT statements

## Connection Information

**For Application (application.properties):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vendora_db
spring.datasource.username=vendora_app
spring.datasource.password=vendora_app_pass
```

**For Manual Connection:**
```bash
# Using Docker
docker exec -it vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db

# Using MySQL client on host
mysql -h127.0.0.1 -P3306 -uvendora_app -pvendora_app_pass vendora_db
```

## Next Steps

After successful data ingestion:

1. **Verify Data**: Run the verification queries above
2. **Start Application**: `./gradlew bootRun`
3. **Test APIs**: Access endpoints to retrieve products with variants
4. **Check Logs**: Monitor application logs for any data-related issues

## Support

If you encounter issues:
1. Check Docker logs: `docker logs vendora-mysql`
2. Verify schema: `SHOW TABLES;` and `DESCRIBE <table_name>;`
3. Check data: Run SELECT queries on each table
4. Review foreign key constraints: `SHOW CREATE TABLE <table_name>;`
