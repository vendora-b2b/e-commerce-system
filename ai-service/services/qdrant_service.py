"""
Qdrant Vector Database Service.

Handles all interactions with Qdrant for storing and retrieving vectors.
"""

import logging
import uuid
from typing import List, Dict, Any, Optional

from qdrant_client import QdrantClient
from qdrant_client.http import models
from qdrant_client.http.models import (
    Distance,
    VectorParams,
    PointStruct,
    Filter,
    FieldCondition,
    MatchValue,
)

from config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class QdrantService:
    """Service for interacting with Qdrant vector database."""
    
    def __init__(self):
        """Initialize Qdrant client."""
        self.client = QdrantClient(
            host=settings.qdrant_host,
            port=settings.qdrant_port
        )
        self.embedding_dimension = settings.embedding_dimension
        
    async def initialize_collections(self):
        """Initialize all required collections in Qdrant."""
        collections = [
            (settings.product_catalog_collection, self._get_product_schema()),
            (settings.knowledge_base_collection, self._get_knowledge_schema()),
            (settings.user_vectors_collection, self._get_user_schema()),
        ]
        
        for collection_name, schema in collections:
            await self._create_collection_if_not_exists(collection_name, schema)
            
    async def _create_collection_if_not_exists(
        self, 
        collection_name: str, 
        payload_schema: Optional[Dict] = None
    ):
        """Create a collection if it doesn't exist."""
        try:
            collections = self.client.get_collections()
            existing_names = [c.name for c in collections.collections]
            
            if collection_name not in existing_names:
                self.client.create_collection(
                    collection_name=collection_name,
                    vectors_config=VectorParams(
                        size=self.embedding_dimension,
                        distance=Distance.COSINE
                    )
                )
                logger.info(f"Created collection: {collection_name}")
            else:
                logger.info(f"Collection already exists: {collection_name}")
                
        except Exception as e:
            logger.error(f"Failed to create collection {collection_name}: {str(e)}")
            raise
            
    def _get_product_schema(self) -> Dict:
        """Get schema for product catalog collection."""
        return {
            "sku": "keyword",
            "product_id": "integer",
            "name": "text",
            "description": "text",
            "supplier_id": "integer",
            "category": "keyword"
        }
        
    def _get_knowledge_schema(self) -> Dict:
        """Get schema for knowledge base collection."""
        return {
            "doc_type": "keyword",
            "title": "text",
            "region": "keyword",
            "source": "keyword",
            "text": "text"
        }
        
    def _get_user_schema(self) -> Dict:
        """Get schema for user vectors collection."""
        return {
            "user_id": "integer",
            "last_updated": "datetime"
        }
        
    # ============== Product Operations ==============
    
    async def upsert_product(
        self,
        product_id: int,
        embedding: List[float],
        metadata: Dict[str, Any]
    ):
        """Insert or update a product in the catalog."""
        try:
            self.client.upsert(
                collection_name=settings.product_catalog_collection,
                points=[
                    PointStruct(
                        id=product_id,
                        vector=embedding,
                        payload=metadata
                    )
                ]
            )
            logger.debug(f"Upserted product {product_id}")
            
        except Exception as e:
            logger.error(f"Failed to upsert product {product_id}: {str(e)}")
            raise
            
    async def delete_product(self, product_id: int):
        """Delete a product by product_id (the point ID in Qdrant)."""
        try:
            self.client.delete(
                collection_name=settings.product_catalog_collection,
                points_selector=models.PointIdsList(
                    points=[product_id]
                )
            )
            logger.info(f"Deleted product with ID: {product_id}")
            
        except Exception as e:
            logger.error(f"Failed to delete product {product_id}: {str(e)}")
            raise
            
    async def search_products(
        self,
        query_vector: List[float],
        limit: int = 10,
        filters: Optional[Dict[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        """Search for products by vector similarity."""
        try:
            filter_conditions = None
            if filters:
                must_conditions = []
                for key, value in filters.items():
                    must_conditions.append(
                        FieldCondition(key=key, match=MatchValue(value=value))
                    )
                filter_conditions = Filter(must=must_conditions)
            
            results = self.client.search(
                collection_name=settings.product_catalog_collection,
                query_vector=query_vector,
                limit=limit,
                query_filter=filter_conditions
            )
            
            return [
                {
                    "id": hit.id,
                    "score": hit.score,
                    **hit.payload
                }
                for hit in results
            ]
            
        except Exception as e:
            logger.error(f"Product search failed: {str(e)}")
            raise
            
    async def get_product_vector(self, product_id: int) -> Optional[List[float]]:
        """Get the vector for a specific product."""
        try:
            results = self.client.retrieve(
                collection_name=settings.product_catalog_collection,
                ids=[product_id],
                with_vectors=True
            )
            
            if results:
                return results[0].vector
            return None
            
        except Exception as e:
            logger.error(f"Failed to get product vector {product_id}: {str(e)}")
            return None
            
    # ============== Knowledge Base Operations ==============
    
    async def upsert_document(
        self,
        embedding: List[float],
        metadata: Dict[str, Any]
    ):
        """Insert a document into the knowledge base."""
        try:
            doc_id = str(uuid.uuid4())
            
            self.client.upsert(
                collection_name=settings.knowledge_base_collection,
                points=[
                    PointStruct(
                        id=doc_id,
                        vector=embedding,
                        payload=metadata
                    )
                ]
            )
            logger.debug(f"Upserted document {doc_id}")
            
        except Exception as e:
            logger.error(f"Failed to upsert document: {str(e)}")
            raise
            
    async def search_knowledge_base(
        self,
        query_vector: List[float],
        limit: int = 5,
        doc_type: Optional[str] = None,
        region: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        """Search the knowledge base."""
        try:
            must_conditions = []
            if doc_type:
                must_conditions.append(
                    FieldCondition(key="doc_type", match=MatchValue(value=doc_type))
                )
            if region:
                must_conditions.append(
                    FieldCondition(key="region", match=MatchValue(value=region))
                )
                
            filter_conditions = Filter(must=must_conditions) if must_conditions else None
            
            results = self.client.search(
                collection_name=settings.knowledge_base_collection,
                query_vector=query_vector,
                limit=limit,
                query_filter=filter_conditions
            )
            
            return [
                {
                    "id": hit.id,
                    "score": hit.score,
                    **hit.payload
                }
                for hit in results
            ]
            
        except Exception as e:
            logger.error(f"Knowledge base search failed: {str(e)}")
            raise
            
    # ============== User Vector Operations ==============
    
    async def get_user_vector(self, user_id: int) -> Optional[List[float]]:
        """Get a user's preference vector."""
        try:
            results = self.client.retrieve(
                collection_name=settings.user_vectors_collection,
                ids=[user_id],
                with_vectors=True
            )
            
            if results:
                return results[0].vector
            return None
            
        except Exception as e:
            logger.error(f"Failed to get user vector {user_id}: {str(e)}")
            return None
            
    async def upsert_user_vector(
        self,
        user_id: int,
        vector: List[float]
    ):
        """Update a user's preference vector."""
        try:
            from datetime import datetime
            
            self.client.upsert(
                collection_name=settings.user_vectors_collection,
                points=[
                    PointStruct(
                        id=user_id,
                        vector=vector,
                        payload={
                            "user_id": user_id,
                            "last_updated": datetime.utcnow().isoformat()
                        }
                    )
                ]
            )
            logger.debug(f"Updated user vector for {user_id}")
            
        except Exception as e:
            logger.error(f"Failed to upsert user vector {user_id}: {str(e)}")
            raise
            
    async def search_by_vector(
        self,
        collection_name: str,
        query_vector: List[float],
        limit: int = 10
    ) -> List[Dict[str, Any]]:
        """Generic vector search for any collection."""
        try:
            results = self.client.search(
                collection_name=collection_name,
                query_vector=query_vector,
                limit=limit
            )
            
            return [
                {
                    "id": hit.id,
                    "score": hit.score,
                    **hit.payload
                }
                for hit in results
            ]
            
        except Exception as e:
            logger.error(f"Vector search failed: {str(e)}")
            raise
