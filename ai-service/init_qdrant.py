#!/usr/bin/env python3
"""
Qdrant Vector Database Initialization Script

This script initializes the Qdrant vector database with existing products
and suppliers from MySQL. Run this after `docker-compose up` to sync
the vector database with the relational database.

Usage:
    python init_qdrant.py                    # Uses defaults (Docker networking)
    python init_qdrant.py --mysql-host localhost --qdrant-host localhost
    
Note: 
    - This only needs to run once after `docker-compose down -v` (volume reset)
    - If you don't use -v flag, Qdrant data persists and you don't need to re-run
"""

import argparse
import asyncio
import logging
import subprocess
import sys
import json
import time
from typing import List, Dict, Any, Optional

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# Try to import required libraries
try:
    from qdrant_client import QdrantClient
    from qdrant_client.http.models import Distance, VectorParams, PointStruct
    from sentence_transformers import SentenceTransformer
except ImportError as e:
    logger.error(f"Missing required library: {e}")
    logger.error("Install with: pip install qdrant-client sentence-transformers")
    sys.exit(1)


# Configuration
EMBEDDING_MODEL = "all-MiniLM-L6-v2"
EMBEDDING_DIMENSION = 384

COLLECTIONS = {
    "product_catalog": {"distance": Distance.COSINE},
    "supplier_catalog": {"distance": Distance.COSINE},
    "knowledge_base": {"distance": Distance.COSINE},
    "user_vectors": {"distance": Distance.COSINE},
}


class QdrantInitializer:
    """Handles Qdrant initialization with data from MySQL."""
    
    def __init__(
        self,
        qdrant_host: str = "localhost",
        qdrant_port: int = 6333,
        mysql_host: str = "localhost",
        mysql_port: int = 3306,
        mysql_user: str = "vendora_app",
        mysql_password: str = "vendora_app_pass",
        mysql_database: str = "vendora_db",
        use_docker: bool = True,
        in_container: bool = False
    ):
        self.qdrant_host = qdrant_host
        self.qdrant_port = qdrant_port
        self.mysql_host = mysql_host
        self.mysql_port = mysql_port
        self.mysql_user = mysql_user
        self.mysql_password = mysql_password
        self.mysql_database = mysql_database
        self.use_docker = use_docker
        self.in_container = in_container
        self.mysql_conn = None
        
        # Initialize clients
        logger.info(f"Connecting to Qdrant at {qdrant_host}:{qdrant_port}")
        self.qdrant = QdrantClient(host=qdrant_host, port=qdrant_port)
        
        # If running inside a container, use direct MySQL connection
        if in_container:
            self._init_mysql_connection()
        
        # Load embedding model
        logger.info(f"Loading embedding model: {EMBEDDING_MODEL}")
        self.model = SentenceTransformer(EMBEDDING_MODEL)
        logger.info("Embedding model loaded successfully")
    
    def _init_mysql_connection(self):
        """Initialize direct MySQL connection (for use inside container)."""
        try:
            import pymysql
            self.mysql_conn = pymysql.connect(
                host=self.mysql_host,
                port=self.mysql_port,
                user=self.mysql_user,
                password=self.mysql_password,
                database=self.mysql_database,
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            logger.info(f"Connected to MySQL at {self.mysql_host}:{self.mysql_port}")
        except ImportError:
            logger.error("pymysql not installed. Install with: pip install pymysql")
            sys.exit(1)
        except Exception as e:
            logger.error(f"Failed to connect to MySQL: {e}")
            sys.exit(1)
        
    def run_mysql_query(self, query: str) -> Optional[str]:
        """Execute SQL query and return results as tab-separated string."""
        # If we have a direct connection (running in container), use it
        if self.mysql_conn:
            return self._run_mysql_query_direct(query)
        
        # Otherwise use docker exec
        return self._run_mysql_query_docker(query)
    
    def _run_mysql_query_direct(self, query: str) -> Optional[str]:
        """Execute SQL query using direct pymysql connection."""
        try:
            with self.mysql_conn.cursor() as cursor:
                cursor.execute(query)
                rows = cursor.fetchall()
                if not rows:
                    return ""
                # Convert dict rows to tab-separated string
                result_lines = []
                for row in rows:
                    values = [str(v) if v is not None else '' for v in row.values()]
                    result_lines.append('\t'.join(values))
                return '\n'.join(result_lines)
        except Exception as e:
            logger.error(f"MySQL query error: {e}")
            return None
    
    def _run_mysql_query_docker(self, query: str) -> Optional[str]:
        """Execute SQL query using docker exec."""
        if self.use_docker:
            cmd = [
                'docker', 'exec', '-i', 'vendora-mysql',
                'mysql', '-N', '-B',
                f'-u{self.mysql_user}',
                f'-p{self.mysql_password}',
                self.mysql_database,
                '-e', query
            ]
        else:
            cmd = [
                'mysql', '-N', '-B',
                f'-h{self.mysql_host}',
                f'-P{self.mysql_port}',
                f'-u{self.mysql_user}',
                f'-p{self.mysql_password}',
                self.mysql_database,
                '-e', query
            ]
        
        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                encoding='utf-8',
                errors='replace'
            )
            
            if result.returncode != 0:
                # Ignore password warning
                if 'Warning' not in result.stderr or 'ERROR' in result.stderr:
                    logger.error(f"MySQL Error: {result.stderr}")
                    return None
                    
            return result.stdout
            
        except Exception as e:
            logger.error(f"Failed to run MySQL query: {e}")
            return None
    
    def embed_text(self, text: str) -> List[float]:
        """Generate embedding for text."""
        if not text or not text.strip():
            text = "empty"
        return self.model.encode(text.strip(), convert_to_numpy=True).tolist()
    
    def initialize_collections(self):
        """Create Qdrant collections if they don't exist."""
        logger.info("Initializing Qdrant collections...")
        
        existing = [c.name for c in self.qdrant.get_collections().collections]
        
        for name, config in COLLECTIONS.items():
            if name in existing:
                logger.info(f"  Collection '{name}' already exists")
            else:
                self.qdrant.create_collection(
                    collection_name=name,
                    vectors_config=VectorParams(
                        size=EMBEDDING_DIMENSION,
                        distance=config["distance"]
                    )
                )
                logger.info(f"  Created collection '{name}'")
        
        logger.info("All collections initialized")
    
    def fetch_products(self) -> List[Dict[str, Any]]:
        """Fetch products from MySQL."""
        logger.info("Fetching products from MySQL...")
        
        # Query products with their category (if available via join table)
        query = """
        SELECT 
            p.id,
            p.sku,
            p.name,
            p.description,
            p.supplier_id,
            COALESCE(c.name, 'Uncategorized') as category_name
        FROM products p
        LEFT JOIN product_categories pc ON p.id = pc.product_id
        LEFT JOIN categories c ON pc.category_id = c.id
        ORDER BY p.id;
        """
        
        result = self.run_mysql_query(query)
        if not result:
            logger.warning("No products found or query failed")
            return []
        
        products = []
        for line in result.strip().split('\n'):
            if not line.strip():
                continue
            parts = line.split('\t')
            if len(parts) >= 5:
                products.append({
                    'product_id': int(parts[0]) if parts[0] else 0,
                    'sku': parts[1] if len(parts) > 1 else '',
                    'name': parts[2] if len(parts) > 2 else '',
                    'description': parts[3] if len(parts) > 3 else '',
                    'supplier_id': int(parts[4]) if len(parts) > 4 and parts[4] else None,
                    'category': parts[5] if len(parts) > 5 else 'Uncategorized'
                })
        
        logger.info(f"Fetched {len(products)} products")
        return products
    
    def fetch_suppliers(self) -> List[Dict[str, Any]]:
        """Fetch suppliers from MySQL."""
        logger.info("Fetching suppliers from MySQL...")
        
        query = """
        SELECT 
            id,
            name,
            email,
            phone,
            address,
            business_license,
            profile_description
        FROM suppliers
        ORDER BY id;
        """
        
        result = self.run_mysql_query(query)
        if not result:
            logger.warning("No suppliers found or query failed")
            return []
        
        suppliers = []
        for line in result.strip().split('\n'):
            if not line.strip():
                continue
            parts = line.split('\t')
            if len(parts) >= 2:
                suppliers.append({
                    'supplier_id': int(parts[0]) if parts[0] else 0,
                    'name': parts[1] if len(parts) > 1 else '',
                    'email': parts[2] if len(parts) > 2 else '',
                    'phone': parts[3] if len(parts) > 3 else '',
                    'address': parts[4] if len(parts) > 4 else '',
                    'business_license': parts[5] if len(parts) > 5 else '',
                    'profile_description': parts[6] if len(parts) > 6 else ''
                })
        
        logger.info(f"Fetched {len(suppliers)} suppliers")
        return suppliers
    
    def ingest_products(self, products: List[Dict[str, Any]], batch_size: int = 100):
        """Ingest products into Qdrant."""
        if not products:
            logger.info("No products to ingest")
            return 0
        
        logger.info(f"Ingesting {len(products)} products into Qdrant...")
        
        ingested = 0
        for i in range(0, len(products), batch_size):
            batch = products[i:i + batch_size]
            points = []
            
            for product in batch:
                try:
                    # Create text to embed
                    text = f"{product['name']}. {product['description']}"
                    embedding = self.embed_text(text)
                    
                    # Create point
                    points.append(PointStruct(
                        id=product['product_id'],
                        vector=embedding,
                        payload={
                            'sku': product['sku'],
                            'product_id': product['product_id'],
                            'name': product['name'],
                            'description': product['description'][:1000] if product['description'] else '',
                            'supplier_id': product['supplier_id'],
                            'category': product['category']
                        }
                    ))
                except Exception as e:
                    logger.warning(f"Failed to process product {product.get('product_id')}: {e}")
                    continue
            
            # Upsert batch
            if points:
                try:
                    self.qdrant.upsert(
                        collection_name="product_catalog",
                        points=points
                    )
                    ingested += len(points)
                    logger.info(f"  Ingested batch {i//batch_size + 1}: {len(points)} products (total: {ingested})")
                except Exception as e:
                    logger.error(f"Failed to upsert product batch: {e}")
        
        logger.info(f"Product ingestion complete: {ingested}/{len(products)}")
        return ingested
    
    def ingest_suppliers(self, suppliers: List[Dict[str, Any]], batch_size: int = 100):
        """Ingest suppliers into Qdrant."""
        if not suppliers:
            logger.info("No suppliers to ingest")
            return 0
        
        logger.info(f"Ingesting {len(suppliers)} suppliers into Qdrant...")
        
        ingested = 0
        for i in range(0, len(suppliers), batch_size):
            batch = suppliers[i:i + batch_size]
            points = []
            
            for supplier in batch:
                try:
                    # Create text to embed
                    text = f"{supplier['name']}. {supplier.get('profile_description', '')}. {supplier.get('address', '')}"
                    embedding = self.embed_text(text)
                    
                    # Create point
                    points.append(PointStruct(
                        id=supplier['supplier_id'],
                        vector=embedding,
                        payload={
                            'supplier_id': supplier['supplier_id'],
                            'name': supplier['name'],
                            'email': supplier['email'],
                            'phone': supplier['phone'],
                            'address': supplier['address'],
                            'business_license': supplier['business_license'],
                            'profile_description': supplier.get('profile_description', '')[:1000]
                        }
                    ))
                except Exception as e:
                    logger.warning(f"Failed to process supplier {supplier.get('supplier_id')}: {e}")
                    continue
            
            # Upsert batch
            if points:
                try:
                    self.qdrant.upsert(
                        collection_name="supplier_catalog",
                        points=points
                    )
                    ingested += len(points)
                    logger.info(f"  Ingested batch {i//batch_size + 1}: {len(points)} suppliers (total: {ingested})")
                except Exception as e:
                    logger.error(f"Failed to upsert supplier batch: {e}")
        
        logger.info(f"Supplier ingestion complete: {ingested}/{len(suppliers)}")
        return ingested
    
    def get_collection_stats(self) -> Dict[str, int]:
        """Get point counts for all collections."""
        stats = {}
        for name in COLLECTIONS.keys():
            try:
                info = self.qdrant.get_collection(name)
                stats[name] = info.points_count
            except Exception as e:
                stats[name] = -1
                logger.warning(f"Failed to get stats for {name}: {e}")
        return stats
    
    def run(self):
        """Run the full initialization process."""
        start_time = time.time()
        
        print("\n" + "="*60)
        print("  QDRANT VECTOR DATABASE INITIALIZATION")
        print("="*60 + "\n")
        
        # Step 1: Initialize collections
        self.initialize_collections()
        print()
        
        # Step 2: Fetch data from MySQL
        products = self.fetch_products()
        suppliers = self.fetch_suppliers()
        print()
        
        # Step 3: Ingest data
        products_ingested = self.ingest_products(products)
        suppliers_ingested = self.ingest_suppliers(suppliers)
        print()
        
        # Step 4: Show stats
        stats = self.get_collection_stats()
        
        elapsed = time.time() - start_time
        
        print("\n" + "="*60)
        print("  INITIALIZATION COMPLETE")
        print("="*60)
        print(f"\n  Time elapsed: {elapsed:.2f} seconds")
        print("\n  Collection Statistics:")
        for name, count in stats.items():
            print(f"    - {name}: {count} points")
        print("\n  Summary:")
        print(f"    - Products ingested: {products_ingested}")
        print(f"    - Suppliers ingested: {suppliers_ingested}")
        print("="*60 + "\n")
        
        return products_ingested, suppliers_ingested


def wait_for_services(qdrant_host: str, qdrant_port: int, mysql_host: str = None, mysql_port: int = 3306, in_container: bool = False, max_retries: int = 30):
    """Wait for Qdrant and MySQL to be ready."""
    import socket
    
    logger.info("Waiting for services to be ready...")
    
    # Wait for Qdrant
    for i in range(max_retries):
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(2)
            result = sock.connect_ex((qdrant_host, qdrant_port))
            sock.close()
            if result == 0:
                logger.info(f"Qdrant is ready at {qdrant_host}:{qdrant_port}")
                break
        except Exception:
            pass
        
        if i < max_retries - 1:
            logger.info(f"Waiting for Qdrant... ({i+1}/{max_retries})")
            time.sleep(2)
    else:
        logger.error("Qdrant did not become ready in time")
        sys.exit(1)
    
    # Wait for MySQL
    if in_container and mysql_host:
        # When running inside a container, check MySQL via socket
        for i in range(max_retries):
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.settimeout(2)
                result = sock.connect_ex((mysql_host, mysql_port))
                sock.close()
                if result == 0:
                    logger.info(f"MySQL is ready at {mysql_host}:{mysql_port}")
                    break
            except Exception:
                pass
            
            if i < max_retries - 1:
                logger.info(f"Waiting for MySQL... ({i+1}/{max_retries})")
                time.sleep(2)
        else:
            logger.error("MySQL did not become ready in time")
            sys.exit(1)
    else:
        # When running on host, use docker exec
        for i in range(max_retries):
            try:
                result = subprocess.run(
                    ['docker', 'exec', 'vendora-mysql', 'mysqladmin', 'ping', '-h', 'localhost', '-u', 'root', '-pvendora_root_2025'],
                    capture_output=True,
                    text=True
                )
                if result.returncode == 0:
                    logger.info("MySQL is ready")
                    break
            except Exception:
                pass
            
            if i < max_retries - 1:
                logger.info(f"Waiting for MySQL... ({i+1}/{max_retries})")
                time.sleep(2)
        else:
            logger.error("MySQL did not become ready in time")
            sys.exit(1)


def main():
    parser = argparse.ArgumentParser(
        description="Initialize Qdrant vector database with MySQL data"
    )
    parser.add_argument(
        "--qdrant-host",
        default="localhost",
        help="Qdrant host (default: localhost)"
    )
    parser.add_argument(
        "--qdrant-port",
        type=int,
        default=6333,
        help="Qdrant port (default: 6333)"
    )
    parser.add_argument(
        "--mysql-host",
        default="localhost",
        help="MySQL host (default: localhost, uses docker exec)"
    )
    parser.add_argument(
        "--mysql-port",
        type=int,
        default=3306,
        help="MySQL port (default: 3306)"
    )
    parser.add_argument(
        "--no-docker",
        action="store_true",
        help="Connect to MySQL directly instead of using docker exec"
    )
    parser.add_argument(
        "--in-container",
        action="store_true",
        help="Running inside a Docker container (uses direct MySQL connection via pymysql)"
    )
    parser.add_argument(
        "--wait",
        action="store_true",
        help="Wait for services to be ready before starting"
    )
    parser.add_argument(
        "--skip-if-populated",
        action="store_true",
        help="Skip initialization if collections already have data"
    )
    
    args = parser.parse_args()
    
    in_container = args.in_container
    
    if args.wait:
        wait_for_services(
            args.qdrant_host, 
            args.qdrant_port, 
            mysql_host=args.mysql_host if in_container else None,
            mysql_port=args.mysql_port,
            in_container=in_container
        )
    
    # Check if already populated
    if args.skip_if_populated:
        try:
            client = QdrantClient(host=args.qdrant_host, port=args.qdrant_port)
            info = client.get_collection("product_catalog")
            if info.points_count > 0:
                logger.info(f"product_catalog already has {info.points_count} points, skipping initialization")
                print("\n✓ Qdrant already populated, skipping initialization\n")
                return
        except Exception:
            pass  # Collection doesn't exist, proceed with initialization
    
    initializer = QdrantInitializer(
        qdrant_host=args.qdrant_host,
        qdrant_port=args.qdrant_port,
        mysql_host=args.mysql_host,
        mysql_port=args.mysql_port,
        use_docker=not args.no_docker and not in_container,
        in_container=in_container
    )
    
    initializer.run()


if __name__ == "__main__":
    main()
