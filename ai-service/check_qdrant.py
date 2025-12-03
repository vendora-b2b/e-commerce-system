"""
Qdrant Vector Database Inspection Script

This script connects to the Qdrant vector database and displays sample data
from all collections to verify the ingestion is working correctly.

Usage:
    python check_qdrant.py

Requirements:
    pip install qdrant-client
"""

import sys
from qdrant_client import QdrantClient
from qdrant_client.http import models


def get_client(host: str = "localhost", port: int = 6333) -> QdrantClient:
    """Create Qdrant client connection."""
    try:
        client = QdrantClient(host=host, port=port)
        # Test connection
        client.get_collections()
        print(f"✓ Connected to Qdrant at {host}:{port}")
        return client
    except Exception as e:
        print(f"✗ Failed to connect to Qdrant: {e}")
        sys.exit(1)


def list_collections(client: QdrantClient) -> list:
    """List all collections in Qdrant."""
    collections = client.get_collections()
    names = [c.name for c in collections.collections]
    print(f"\n📦 Collections found: {len(names)}")
    for name in names:
        print(f"   - {name}")
    return names


def get_collection_info(client: QdrantClient, collection_name: str):
    """Get detailed info about a collection."""
    try:
        info = client.get_collection(collection_name)
        print(f"\n📊 Collection: {collection_name}")
        print(f"   Points count: {info.points_count}")
        print(f"   Vectors count: {info.vectors_count}")
        print(f"   Status: {info.status}")
        if info.config.params.vectors:
            if hasattr(info.config.params.vectors, 'size'):
                print(f"   Vector size: {info.config.params.vectors.size}")
                print(f"   Distance: {info.config.params.vectors.distance}")
        return info
    except Exception as e:
        print(f"   ✗ Error getting collection info: {e}")
        return None


def get_sample_points(client: QdrantClient, collection_name: str, limit: int = 5):
    """Get sample points from a collection."""
    try:
        # Scroll through points (no vector needed)
        points, _ = client.scroll(
            collection_name=collection_name,
            limit=limit,
            with_payload=True,
            with_vectors=False  # Don't fetch vectors to save bandwidth
        )
        
        if not points:
            print(f"   No points found in {collection_name}")
            return []
            
        print(f"\n   📋 Sample points ({len(points)} of {limit} requested):")
        print("   " + "=" * 60)
        
        for i, point in enumerate(points, 1):
            print(f"\n   [{i}] ID: {point.id}")
            if point.payload:
                for key, value in point.payload.items():
                    # Truncate long values
                    str_value = str(value)
                    if len(str_value) > 80:
                        str_value = str_value[:77] + "..."
                    print(f"       {key}: {str_value}")
        
        return points
        
    except Exception as e:
        print(f"   ✗ Error getting sample points: {e}")
        return []


def check_product_catalog(client: QdrantClient):
    """Check the product_catalog collection."""
    collection_name = "product_catalog"
    print("\n" + "=" * 70)
    print("🛍️  PRODUCT CATALOG")
    print("=" * 70)
    
    info = get_collection_info(client, collection_name)
    if info and info.points_count > 0:
        get_sample_points(client, collection_name, limit=5)
    else:
        print("   ⚠️  No products ingested yet")
        print("   To ingest products, create/update a product via the Spring Boot API")


def check_supplier_catalog(client: QdrantClient):
    """Check the supplier_catalog collection."""
    collection_name = "supplier_catalog"
    print("\n" + "=" * 70)
    print("🏢 SUPPLIER CATALOG")
    print("=" * 70)
    
    info = get_collection_info(client, collection_name)
    if info and info.points_count > 0:
        get_sample_points(client, collection_name, limit=5)
    else:
        print("   ⚠️  No suppliers ingested yet")
        print("   To ingest suppliers, update a supplier profile via the Spring Boot API")


def check_knowledge_base(client: QdrantClient):
    """Check the knowledge_base collection."""
    collection_name = "knowledge_base"
    print("\n" + "=" * 70)
    print("📚 KNOWLEDGE BASE")
    print("=" * 70)
    
    info = get_collection_info(client, collection_name)
    if info and info.points_count > 0:
        get_sample_points(client, collection_name, limit=3)
    else:
        print("   ⚠️  No documents ingested yet")
        print("   To ingest documents, use POST /ai/ingest/document")


def check_user_vectors(client: QdrantClient):
    """Check the user_vectors collection."""
    collection_name = "user_vectors"
    print("\n" + "=" * 70)
    print("👤 USER VECTORS (Recommendation Preferences)")
    print("=" * 70)
    
    info = get_collection_info(client, collection_name)
    if info and info.points_count > 0:
        get_sample_points(client, collection_name, limit=3)
    else:
        print("   ⚠️  No user vectors yet")
        print("   User vectors are created when users interact with products (view, add to cart, order)")


def test_search(client: QdrantClient, collection_name: str, query_text: str):
    """Test semantic search on a collection."""
    try:
        # We need embeddings for search, but this script doesn't have the embedding model
        # So we'll just verify the collection is searchable
        print(f"\n🔍 Search capability check for {collection_name}:")
        
        info = client.get_collection(collection_name)
        if info.points_count > 0:
            print(f"   ✓ Collection has {info.points_count} points, ready for semantic search")
        else:
            print(f"   ⚠️  Collection is empty, search won't return results")
            
    except Exception as e:
        print(f"   ✗ Search check failed: {e}")


def main():
    print("=" * 70)
    print("🔍 QDRANT VECTOR DATABASE INSPECTION")
    print("=" * 70)
    
    # Parse command line arguments
    host = "localhost"
    port = 6333
    
    if len(sys.argv) > 1:
        host = sys.argv[1]
    if len(sys.argv) > 2:
        port = int(sys.argv[2])
    
    # Connect to Qdrant
    client = get_client(host, port)
    
    # List all collections
    collections = list_collections(client)
    
    # Check each expected collection
    expected_collections = ["product_catalog", "supplier_catalog", "knowledge_base", "user_vectors"]
    
    missing = [c for c in expected_collections if c not in collections]
    if missing:
        print(f"\n⚠️  Missing expected collections: {missing}")
        print("   Start the AI service to create them: docker-compose up ai-service")
    
    # Check each collection
    if "product_catalog" in collections:
        check_product_catalog(client)
        
    if "supplier_catalog" in collections:
        check_supplier_catalog(client)
        
    if "knowledge_base" in collections:
        check_knowledge_base(client)
        
    if "user_vectors" in collections:
        check_user_vectors(client)
    
    # Summary
    print("\n" + "=" * 70)
    print("📊 SUMMARY")
    print("=" * 70)
    
    total_points = 0
    for name in expected_collections:
        if name in collections:
            try:
                info = client.get_collection(name)
                count = info.points_count
                total_points += count
                status = "✓" if count > 0 else "○"
                print(f"   {status} {name}: {count} points")
            except:
                print(f"   ✗ {name}: error")
        else:
            print(f"   ✗ {name}: not created")
    
    print(f"\n   Total vectors stored: {total_points}")
    
    if total_points == 0:
        print("\n💡 To populate the database:")
        print("   1. Start all services: docker-compose up -d")
        print("   2. Create products via POST /api/products")
        print("   3. Update supplier profiles via PUT /api/suppliers/{id}")
        print("   4. View products to create user vectors")


if __name__ == "__main__":
    main()
