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
            logger.info(f"🔍 TRACKING INTERACTION: user={user_id}, product={product_id}, sku={sku}, action={action}")
            
            # Get the product vector
            product_vector = None
            
            if product_id:
                logger.info(f"🔍 Looking up product vector: product_id={product_id}")
                product_vector = await self.qdrant_service.get_product_vector(product_id)
                
            if product_vector is None and sku:
                # Try to find by SKU
                logger.info(f"🔍 Product not found by ID, trying SKU: {sku}")
                results = await self.qdrant_service.search_products(
                    query_vector=[0.0] * settings.embedding_dimension,  # Dummy vector
                    limit=1,
                    filters={"sku": sku}
                )
                if results:
                    product_id = results[0]["id"]
                    logger.info(f"✅ Found product by SKU: product_id={product_id}")
                    product_vector = await self.qdrant_service.get_product_vector(product_id)
                    
            if product_vector is None:
                logger.error(f"❌ PRODUCT NOT IN QDRANT: product_id={product_id}, sku={sku} - Cannot track interaction!")
                logger.error(f"💡 Solution: Product must be ingested to Qdrant first via /ai/ingest/product")
                return
            
            logger.info(f"✅ Product vector found: dimension={len(product_vector)}")
                
            # Get or initialize user vector
            logger.info(f"🔍 Looking up user vector: user_id={user_id}")
            user_vector = await self.qdrant_service.get_user_vector(user_id)
            
            if user_vector is None:
                # Initialize with the product vector (first interaction)
                logger.info(f"🆕 COLD START: Initializing user vector from product vector")
                user_vector = product_vector
                logger.info(f"✅ User vector initialized: dimension={len(user_vector)}")
            else:
                # Update using weighted decay formula
                logger.info(f"🔄 UPDATING existing user vector")
                weight = self.weights.get(action, 1.0)
                update_factor = (1 - self.decay) * weight  # Removed 0.05 scaling for more noticeable updates

                user_vector = np.array(user_vector)
                product_vector = np.array(product_vector)

                # Log old vector for debugging
                old_vector_sample = user_vector[:5].tolist()
                old_norm = np.linalg.norm(user_vector)

                # Weighted update: blend old preferences with new product
                user_vector = user_vector * self.decay + product_vector * update_factor

                # Calculate norm before normalization
                norm_before_normalization = np.linalg.norm(user_vector)

                # Normalize the vector (required for cosine similarity in Qdrant)
                if norm_before_normalization > 0:
                    user_vector = user_vector / norm_before_normalization

                new_norm = np.linalg.norm(user_vector)
                new_vector_sample = user_vector[:5].tolist()
                user_vector = user_vector.tolist()

                logger.info(f"✅ User vector updated: old_norm={old_norm:.4f}, new_norm={new_norm:.4f}, weight={weight}, decay={self.decay}, update_factor={update_factor:.4f}")
                logger.info(f"   Old vector sample: {old_vector_sample}")
                logger.info(f"   New vector sample: {new_vector_sample}")
                
            # Save updated user vector
            logger.info(f"💾 Saving user vector to Qdrant: user_id={user_id}")
            await self.qdrant_service.upsert_user_vector(user_id, user_vector)
            
            logger.info(f"✅✅✅ TRACKING COMPLETE: {action} interaction for user {user_id} on product {product_id}")
            
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
                limit=limit,
                score_threshold=0.0  # No threshold for homepage recommendations
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
            # No threshold for similar products - we want recommendations even if not perfect match
            results = await self.qdrant_service.search_products(
                query_vector=product_vector,
                limit=limit + 1,
                score_threshold=0.0  # No threshold for recommendations
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
            # Get user vector from Qdrant and print it
            user_vector = await self.qdrant_service.get_user_vector(user_id)
            
            if user_vector is not None:
                print("=" * 80)
                print(f"🎯 HOMEPAGE RECOMMENDATION REQUEST")
                print(f"User ID: {user_id}")
                print(f"User Vector Dimension: {len(user_vector)}")
                print(f"User Vector (first 10 values): {user_vector[:10]}")
                print(f"User Vector (last 10 values): {user_vector[-10:]}")
                print(f"Vector Norm: {np.linalg.norm(user_vector):.4f}")
                print("=" * 80)
            else:
                print("=" * 80)
                print(f"🆕 HOMEPAGE RECOMMENDATION REQUEST (NEW USER)")
                print(f"User ID: {user_id}")
                print(f"User Vector: NOT FOUND (cold start)")
                print("=" * 80)
            
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
                limit=limit,
                score_threshold=0.0  # No threshold for default recommendations
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
