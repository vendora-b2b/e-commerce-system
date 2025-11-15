# Quick Start Guide

Get the e-commerce system up and running in 5 minutes!

## Prerequisites

- Docker & Docker Compose
- Python 3
- Java 17+ (for running the application)
- Gradle (for running the application)

## Setup Steps

### 1. Start Database

```bash
docker-compose up -d
```

Wait for MySQL to be ready (~10 seconds):
```bash
docker exec vendora-mysql mysqladmin ping -h localhost -uroot -pvendora_root_2025
# Should output: mysqld is alive
```

### 2. Create Schema

```bash
docker exec -i vendora-mysql mysql -uroot -pvendora_root_2025 < init-db/02-create-schema.sql
```

### 3. Load Data from CSV Files

**Option A: Load ALL data from CSV files (Recommended)**

```bash
python3 ingest_csv_to_db.py
```

This loads:
- **475 categories** from `categories.csv`
- **992 products** from `products.csv`
- **96,365 product variants** (auto-generated from colors × sizes)
- **96,437 inventory records** (with random stock levels)

Takes approximately 2-3 minutes.

**Option B: Load limited data for testing**

```bash
# Load only 50 products
python3 ingest_csv_to_db.py --limit 50
```

**Option C: Load minimal sample data**

```bash
# Load 3 hand-crafted sample products
docker exec -i vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db < ingest_simple.sql
```

### 4. Verify Data

```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT COUNT(*) as products FROM products; SELECT COUNT(*) as variants FROM product_variants;"
```

Expected output:
```
products: 3
variants: 6
```

### 5. Start Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## Sample Data Overview

**Product 1: Laura Mercier Eye Shadow**
- 2 color variants (Sugar Frost, Tuxedo)
- In stock: 45 and 60 units

**Product 2: Exultantex Curtains**
- 4 variants (Black/Blue/Gray/Pink in different sizes)
- Stock levels: 20, 0 (out of stock), 35, 50

**Product 3: Cotton Duvet Set**
- No variants
- In stock: 100 units

## Test Queries

### Get all products with categories
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT p.name, c.name as category, p.base_price
      FROM products p
      LEFT JOIN categories c ON p.category_id = c.id;"
```

### Get product variants with inventory
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT p.name, pv.color, pv.size, i.available_quantity, i.status
      FROM products p
      LEFT JOIN product_variants pv ON p.id = pv.product_id
      LEFT JOIN inventory i ON pv.id = i.variant_id
      WHERE p.id = 2;"
```

### Find low/out of stock items
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db \
  -e "SELECT p.name, pv.color, pv.size, i.available_quantity, i.status
      FROM inventory i
      JOIN products p ON i.product_id = p.id
      LEFT JOIN product_variants pv ON i.variant_id = pv.id
      WHERE i.status = 'OUT_OF_STOCK' OR i.available_quantity < 30;"
```

## Reset Everything

```bash
# Stop and remove containers
docker-compose down -v

# Start fresh
docker-compose up -d
docker exec -i vendora-mysql mysql -uroot -pvendora_root_2025 < init-db/02-create-schema.sql
docker exec -i vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db < ingest_simple.sql
```

## More Information

- **Full Data Ingestion Guide**: See [DATA_INGESTION_GUIDE.md](DATA_INGESTION_GUIDE.md)
- **Database Schema**: See `init-db/02-create-schema.sql`
- **Sample Data**: See `ingest_simple.sql`

## Troubleshooting

**Container won't start:**
```bash
docker logs vendora-mysql
```

**Connection refused:**
Make sure MySQL is ready:
```bash
docker exec vendora-mysql mysqladmin ping -h localhost -uroot -pvendora_root_2025
```

**Data already exists:**
Clear tables first:
```bash
docker exec vendora-mysql mysql -uvendora_app -pvendora_app_pass vendora_db -e "
  SET FOREIGN_KEY_CHECKS=0;
  TRUNCATE TABLE inventory;
  TRUNCATE TABLE product_variants;
  TRUNCATE TABLE products;
  TRUNCATE TABLE categories;
  SET FOREIGN_KEY_CHECKS=1;
"
```
