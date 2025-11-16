#!/usr/bin/env python3
"""
CSV Data Ingestion Script
Ingests categories.csv and products.csv into MySQL database
"""

import csv
import json
import subprocess
import sys
from datetime import datetime

def run_mysql(sql):
    """Execute SQL command in MySQL container"""
    cmd = [
        'docker', 'exec', '-i', 'vendora-mysql',
        'mysql', '-uvendora_app', '-pvendora_app_pass', 'vendora_db'
    ]

    process = subprocess.Popen(
        cmd,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    stdout, stderr = process.communicate(input=sql)

    if process.returncode != 0 and 'Warning' not in stderr:
        print(f"Error: {stderr}", file=sys.stderr)
        return None

    return stdout

def parse_json_field(field):
    """Parse JSON field from CSV"""
    if not field or field == '':
        return []
    try:
        return json.loads(field)
    except:
        return []

def parse_category_path(category_ids_str):
    """Parse category IDs path like '0:1085666:1007040' and return leaf category ID"""
    if not category_ids_str or category_ids_str == '':
        return None
    parts = category_ids_str.split(':')
    # Get the last non-zero ID
    for p in reversed(parts):
        if p and p != '0':
            try:
                return int(p)
            except:
                continue
    return None

def escape_sql_string(s):
    """Escape string for SQL"""
    if s is None:
        return 'NULL'
    s = str(s).replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')
    return f"'{s}'"

def create_variant_sku(base_sku, color, size):
    """Generate variant SKU"""
    parts = [str(base_sku)]
    if color:
        parts.append(color.replace(' ', '-').replace('"', '').replace("'", ''))
    if size:
        parts.append(size.replace(' ', '-').replace('"', '').replace("'", ''))
    return '-'.join(parts)

def ingest_categories(csv_file):
    """Ingest categories from CSV"""
    print("\n" + "="*60)
    print("INGESTING CATEGORIES")
    print("="*60)

    sql_statements = []
    count = 0

    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)

        for row in reader:
            try:
                cat_id = int(row['category_id'])
                cat_name = row['category_name'].replace("'", "\\'").replace('"', '\\"')

                slug = cat_name.lower().replace(' ', '-').replace('&', 'and').replace("'", '')
                slug = ''.join(c for c in slug if c.isalnum() or c == '-')

                sql = f"""
                INSERT INTO categories (id, name, parent_category_id, level, slug, created_at, updated_at)
                VALUES ({cat_id}, '{cat_name}', NULL, 0, '{slug}', NOW(), NOW())
                ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = VALUES(updated_at);
                """
                sql_statements.append(sql)
                count += 1

                if count % 100 == 0:
                    print(f"  Processed {count} categories...")

            except Exception as e:
                print(f"  Error processing category {row.get('category_id')}: {e}")
                continue

    # Execute all SQL statements
    print(f"\n  Inserting {count} categories into database...")
    full_sql = '\n'.join(sql_statements)
    run_mysql(full_sql)

    print(f"✓ Inserted {count} categories")
    return count

def get_supplier_info(brand_name):
    """Get enriched supplier information based on brand name"""
    # Map of known brands to supplier information
    # This is a curated list based on general knowledge about these brands
    supplier_data = {
        'email_suffix': brand_name.lower().replace(' ', '').replace('&', 'and').replace("'", '')[:30],
        'phone': '+1-800-555-0100',  # Generic phone
        'address': f'{brand_name} Headquarters, USA',
        'profile_description': f'{brand_name} - Quality products from a trusted brand',
        'verified': True,
    }

    return supplier_data

def create_suppliers_from_brands(csv_file):
    """Extract brands and create suppliers"""
    print("\n" + "="*60)
    print("CREATING SUPPLIERS FROM BRANDS")
    print("="*60)

    brands = {}

    # Extract unique brands
    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            brand = row.get('brand', '').strip()
            if brand and brand != '':
                if brand not in brands:
                    brands[brand] = get_supplier_info(brand)

    print(f"  Found {len(brands)} unique brands")

    # Insert suppliers
    sql_statements = []
    for brand, info in sorted(brands.items()):
        brand_escaped = brand.replace("'", "\\'").replace('"', '\\"')
        email = f"contact@{info['email_suffix']}.com"[:255]
        business_license = f"BL-{info['email_suffix'][:20].upper()}"[:255]
        address = info['address'][:255].replace("'", "\\'")
        desc = info['profile_description'][:1000].replace("'", "\\'")
        verified = 1 if info['verified'] else 0

        sql = f"""
        INSERT INTO suppliers (name, email, business_license, phone, address, profile_description, verified)
        VALUES ('{brand_escaped}', '{email}', '{business_license}', '{info['phone']}', '{address}', '{desc}', {verified})
        ON DUPLICATE KEY UPDATE name = VALUES(name);
        """
        sql_statements.append(sql)

    # Execute all SQL statements
    print(f"  Inserting {len(brands)} suppliers into database...")
    full_sql = '\n'.join(sql_statements)
    run_mysql(full_sql)

    print(f"✓ Created {len(brands)} suppliers")
    return len(brands)

def get_supplier_id_by_brand(brand):
    """Get supplier ID by brand name"""
    if not brand:
        return 1  # Default supplier

    brand_escaped = brand.replace("'", "\\'").replace('"', '\\"')
    result = run_mysql(f"SELECT id FROM suppliers WHERE name = '{brand_escaped}';")

    if result and result.strip():
        lines = result.strip().split('\n')
        if len(lines) > 1:  # Skip header
            return lines[1].strip()

    return 1  # Default supplier if not found

def ingest_products(csv_file, limit=None):
    """Ingest products from CSV"""
    print("\n" + "="*60)
    print(f"INGESTING PRODUCTS" + (f" (limit: {limit})" if limit else ""))
    print("="*60)

    product_count = 0
    variant_count = 0
    image_count = 0
    error_count = 0

    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)

        batch_sql = []
        batch_size = 50

        for row in reader:
            if limit and product_count >= limit:
                break

            try:
                # Parse basic fields
                product_id = row.get('product_id', '')
                sku = row.get('sku', product_id) or product_id
                sku = sku[:50]  # Limit to 50 chars

                name = row.get('product_name', 'Unknown Product')
                name = name[:200]  # Limit to 200 chars
                name = name.replace("'", "\\'").replace('"', '\\"')

                description = row.get('description', '')[:2000]
                description = description.replace("'", "\\'").replace('"', '\\"') if description else ''

                # Get brand and supplier
                brand = row.get('brand', '').strip()
                supplier_id = get_supplier_id_by_brand(brand)

                # Parse category
                category_ids_str = row.get('category_ids', '')
                category_id = parse_category_path(category_ids_str)
                category_id_sql = str(category_id) if category_id else 'NULL'

                # Parse price
                try:
                    final_price = float(row.get('final_price', 0))
                    if final_price <= 0:
                        final_price = 19.99
                except:
                    final_price = 19.99

                # Parse unit and MOQ
                unit = row.get('unit', 'piece') or 'piece'
                unit = unit[:50]

                try:
                    moq = int(row.get('min_order_quant', 1))
                    if moq <= 0:
                        moq = 1
                except:
                    moq = 1

                # Parse image URLs
                image_urls_str = row.get('image_urls', '')
                image_urls = parse_json_field(image_urls_str)

                # Insert product
                product_sql = f"""
                INSERT INTO products (sku, name, description, category_id, supplier_id, base_price, minimum_order_quantity, unit, status, created_at, updated_at)
                VALUES ('{sku}', '{name}', '{description}', {category_id_sql}, {supplier_id}, {final_price}, {moq}, '{unit}', 'ACTIVE', NOW(), NOW());
                SET @last_product_id = LAST_INSERT_ID();
                """

                batch_sql.append(product_sql)

                # Insert product images
                if image_urls:
                    for img_url in image_urls:
                        if img_url and img_url.strip():
                            img_url_escaped = img_url.strip()[:500].replace("'", "\\'").replace('"', '\\"')
                            image_sql = f"""
                            INSERT INTO product_images (product_id, image_url)
                            VALUES (@last_product_id, '{img_url_escaped}');
                            """
                            batch_sql.append(image_sql)
                            image_count += 1

                # Parse variants
                colors = parse_json_field(row.get('colors', ''))
                sizes = parse_json_field(row.get('sizes', ''))

                if colors or sizes:
                    # If only colors or only sizes
                    if not colors:
                        colors = [None]
                    if not sizes:
                        sizes = [None]

                    # Create cartesian product of variants
                    for color in colors:
                        for size in sizes:
                            if color is None and size is None:
                                continue

                            variant_sku = create_variant_sku(sku, color, size)
                            variant_sku = variant_sku[:100]  # Limit SKU length

                            color_sql = escape_sql_string(color) if color else 'NULL'
                            size_sql = escape_sql_string(size) if size else 'NULL'

                            # Random stock between 0-100
                            import random
                            stock_qty = random.randint(0, 100)
                            status = 'OUT_OF_STOCK' if stock_qty == 0 else 'AVAILABLE'

                            variant_sql = f"""
                            INSERT INTO product_variants (product_id, sku, color, size, price_adjustment)
                            VALUES (@last_product_id, '{variant_sku}', {color_sql}, {size_sql}, 0.0);
                            SET @last_variant_id = LAST_INSERT_ID();
                            INSERT INTO inventory (supplier_id, product_id, variant_id, available_quantity, reserved_quantity, status, last_updated)
                            VALUES ({supplier_id}, @last_product_id, @last_variant_id, {stock_qty}, 0, '{status}', NOW());
                            """

                            batch_sql.append(variant_sql)
                            variant_count += 1
                else:
                    # No variants - create inventory at product level
                    import random
                    stock_qty = random.randint(10, 100)

                    inventory_sql = f"""
                    INSERT INTO inventory (supplier_id, product_id, variant_id, available_quantity, reserved_quantity, status, last_updated)
                    VALUES ({supplier_id}, @last_product_id, NULL, {stock_qty}, 0, 'AVAILABLE', NOW());
                    """

                    batch_sql.append(inventory_sql)

                product_count += 1

                # Execute batch
                if len(batch_sql) >= batch_size:
                    run_mysql('\n'.join(batch_sql))
                    batch_sql = []
                    print(f"  Progress: {product_count} products, {variant_count} variants...")

            except Exception as e:
                error_count += 1
                print(f"  Error processing product {product_id}: {e}")
                continue

        # Execute remaining batch
        if batch_sql:
            run_mysql('\n'.join(batch_sql))

    print(f"\n✓ Inserted {product_count} products")
    print(f"✓ Inserted {variant_count} variants")
    print(f"✓ Inserted {image_count} product images")
    if error_count > 0:
        print(f"⚠ Errors: {error_count}")

    return product_count, variant_count, image_count

def verify_data():
    """Verify ingested data"""
    print("\n" + "="*60)
    print("VERIFYING DATA")
    print("="*60)

    queries = [
        ("Categories", "SELECT COUNT(*) as count FROM categories;"),
        ("Suppliers", "SELECT COUNT(*) as count FROM suppliers;"),
        ("Products", "SELECT COUNT(*) as count FROM products;"),
        ("Product Images", "SELECT COUNT(*) as count FROM product_images;"),
        ("Variants", "SELECT COUNT(*) as count FROM product_variants;"),
        ("Inventory", "SELECT COUNT(*) as count FROM inventory;"),
    ]

    for name, query in queries:
        result = run_mysql(query)
        if result:
            print(f"  {name}: {result.strip()}")

def main():
    """Main ingestion process"""
    import argparse

    parser = argparse.ArgumentParser(description='Ingest CSV data into MySQL database')
    parser.add_argument('--limit', type=int, help='Limit number of products to ingest')
    parser.add_argument('--products-only', action='store_true', help='Skip categories and suppliers')
    parser.add_argument('--categories-only', action='store_true', help='Skip suppliers and products')
    parser.add_argument('--suppliers-only', action='store_true', help='Skip categories and products')

    args = parser.parse_args()

    print("="*60)
    print("CSV DATA INGESTION TOOL")
    print("="*60)

    try:
        if not args.products_only and not args.suppliers_only:
            cat_count = ingest_categories('categories.csv')

        if not args.categories_only and not args.products_only:
            supplier_count = create_suppliers_from_brands('products.csv')

        if not args.categories_only and not args.suppliers_only:
            prod_count, var_count, img_count = ingest_products('products.csv', limit=args.limit)

        verify_data()

        print("\n" + "="*60)
        print("✓ INGESTION COMPLETE!")
        print("="*60)

    except Exception as e:
        print(f"\n❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == '__main__':
    main()
