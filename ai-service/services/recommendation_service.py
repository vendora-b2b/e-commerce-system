"""
Recommendation Service.

Handles user preference tracking and product recommendations.
"""

import logging
from typing import List, Dict, Any, Optional
import numpy as np

from config import get_settings
from services.qdrant_service import QdrantService
from services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
settings = get_settings()


class RecommendationService:
    """Service for generating product recommendations."""
    
    def __init__(self):
        """Initialize recommendation service."""
        self.qdrant_service = QdrantService()
        self.embedding_service = EmbeddingService()
        
        # Weights for different interaction types
        self.weights = {
            "VIEW": settings.view_weight,
            "ADD_TO_CART": settings.add_to_cart_weight,
            "ORDER": settings.order_weight
        }
        
        # Decay factor for user vector updates
        self.decay = settings.user_vector_decay
        
    async def track_interaction(
        self,
        user_id: int,
        product_id: Optional[int],
        variant_id: Optional[int],
        sku: Optional[str],
        action: str
    ):
        """
        Track a user interaction and update their preference vector.
        
        Uses the formula: V_new = V_old × decay + V_product × (1-decay) × weight
        """
        try:
            # Get the product vector
            product_vector = None
            
            if product_id:
                product_vector = await self.qdrant_service.get_product_vector(product_id)
                
            if product_vector is None and sku:
                # Try to find by SKU
                results = await self.qdrant_service.search_products(
                    query_vector=[0.0] * settings.embedding_dimension,  # Dummy vector
                    limit=1,
                    filters={"sku": sku}
                )
                if results:
                    product_id = results[0]["id"]
                    product_vector = await self.qdrant_service.get_product_vector(product_id)
                    
            if product_vector is None:
                logger.warning(f"Could not find product vector for tracking: product_id={product_id}, sku={sku}")
                return
                
            # Get or initialize user vector
            user_vector = await self.qdrant_service.get_user_vector(user_id)
            
            if user_vector is None:
                # Initialize with the product vector (first interaction)
                user_vector = product_vector
            else:
                # Update using weighted decay formula
                weight = self.weights.get(action, 1.0)
                update_factor = (1 - self.decay) * weight * 0.05  # Scale down the update
                
                user_vector = np.array(user_vector)
                product_vector = np.array(product_vector)
                
                user_vector = user_vector * self.decay + product_vector * update_factor
                
                # Normalize the vector
                norm = np.linalg.norm(user_vector)
                if norm > 0:
                    user_vector = user_vector / norm
                    
                user_vector = user_vector.tolist()
                
            # Save updated user vector
            await self.qdrant_service.upsert_user_vector(user_id, user_vector)
            
            logger.info(f"Tracked {action} interaction for user {user_id}")
            
        except Exception as e:
            logger.error(f"Failed to track interaction: {str(e)}")
            raise
            
    async def get_user_recommendations(
        self,
        user_id: int,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        Get personalized recommendations for a user based on their preference vector.
        """
        try:
            # Get user vector
            user_vector = await self.qdrant_service.get_user_vector(user_id)
            
            if user_vector is None:
                # Cold start: return popular/default products
                logger.info(f"No user vector for {user_id}, returning default recommendations")
                return await self._get_default_recommendations(limit)
                
            # Search for similar products
            results = await self.qdrant_service.search_products(
                query_vector=user_vector,
                limit=limit
            )
            
            return [
                {
                    "product_id": r["product_id"],
                    "sku": r["sku"],
                    "name": r["name"],
                    "score": r["score"]
                }
                for r in results
            ]
            
        except Exception as e:
            logger.error(f"Failed to get user recommendations: {str(e)}")
            raise
            
    async def get_similar_products(
        self,
        product_id: int,
        limit: int = 10
    ) -> List[Dict[str, Any]]:
        """
        Get products similar to a given product (item-to-item recommendations).
        """
        try:
            # Get product vector
            product_vector = await self.qdrant_service.get_product_vector(product_id)
            
            if product_vector is None:
                logger.warning(f"Product vector not found for {product_id}")
                return []
                
            # Search for similar products (limit + 1 to exclude self)
            results = await self.qdrant_service.search_products(
                query_vector=product_vector,
                limit=limit + 1
            )
            
            # Filter out the query product itself
            recommendations = [
                {
                    "product_id": r["product_id"],
                    "sku": r["sku"],
                    "name": r["name"],
                    "score": r["score"]
                }
                for r in results
                if r["product_id"] != product_id
            ][:limit]
            
            return recommendations
            
        except Exception as e:
            logger.error(f"Failed to get similar products: {str(e)}")
            raise
            
    async def get_homepage_recommendations(
        self,
        user_id: int,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        Get homepage recommendations combining user preferences with diversity.
        """
        try:
            # Get user-based recommendations
            user_recommendations = await self.get_user_recommendations(user_id, limit=limit)
            
            # If we have enough recommendations, return them
            if len(user_recommendations) >= limit:
                return user_recommendations[:limit]
                
            # Otherwise, supplement with default recommendations
            needed = limit - len(user_recommendations)
            default_recs = await self._get_default_recommendations(needed)
            
            # Merge, avoiding duplicates
            seen_ids = {r["product_id"] for r in user_recommendations}
            for rec in default_recs:
                if rec["product_id"] not in seen_ids:
                    user_recommendations.append(rec)
                    seen_ids.add(rec["product_id"])
                    
            return user_recommendations[:limit]
            
        except Exception as e:
            logger.error(f"Failed to get homepage recommendations: {str(e)}")
            raise
            
    async def _get_default_recommendations(
        self,
        limit: int
    ) -> List[Dict[str, Any]]:
        """
        Get default recommendations for users without preference history.
        
        In production, this could return trending/popular products.
        For now, returns a random sample from the catalog.
        """
        try:
            # Create a neutral query vector (zeros)
            # This will return somewhat random results from the catalog
            neutral_vector = [0.0] * settings.embedding_dimension
            
            results = await self.qdrant_service.search_products(
                query_vector=neutral_vector,
                limit=limit
            )
            
            return [
                {
                    "product_id": r["product_id"],
                    "sku": r["sku"],
                    "name": r["name"],
                    "score": 0.5  # Default score for non-personalized
                }
                for r in results
            ]
            
        except Exception as e:
            logger.error(f"Failed to get default recommendations: {str(e)}")
            return []
