"""
Recommendation API endpoints.
"""

import logging
from typing import List, Optional
from enum import Enum

from fastapi import APIRouter, HTTPException, status, Query
from pydantic import BaseModel, Field

from services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize service
recommendation_service = RecommendationService()


# ============== Enums and Models ==============

class InteractionType(str, Enum):
    """Types of user interactions for analytics."""
    VIEW = "VIEW"
    ADD_TO_CART = "ADD_TO_CART"
    ORDER = "ORDER"


class TrackInteractionRequest(BaseModel):
    """Request model for tracking user interactions."""
    user_id: int = Field(..., description="User ID", alias="userId")
    product_id: Optional[int] = Field(None, description="Product ID", alias="productId")
    variant_id: Optional[int] = Field(None, description="Variant ID", alias="variantId")
    sku: Optional[str] = Field(None, description="Product SKU")
    action: InteractionType = Field(..., description="Type of interaction")
    
    class Config:
        populate_by_name = True  # Accept both snake_case and camelCase
        json_schema_extra = {
            "example": {
                "user_id": 123,
                "product_id": 456,
                "sku": "SHOE-NIKE-001",
                "action": "VIEW"
            }
        }


class ProductRecommendation(BaseModel):
    """A single product recommendation."""
    product_id: int = Field(..., alias="productId")
    sku: str
    name: str
    score: float = Field(..., description="Relevance score (0-1)")
    
    class Config:
        populate_by_name = True  # Accept both snake_case and camelCase
        by_alias = True  # Serialize using aliases (camelCase)


class RecommendationResponse(BaseModel):
    """Response model for recommendations."""
    recommendations: List[ProductRecommendation]
    total: int


# ============== Endpoints ==============

@router.post("/analytics/track")
async def track_interaction(request: TrackInteractionRequest):
    """
    Track a user interaction for recommendation learning.
    
    This updates the user's preference vector based on their actions.
    """
    try:
        logger.info(f"📥 RECEIVED TRACKING REQUEST: user={request.user_id}, product={request.product_id}, action={request.action}")
        
        # Need at least one identifier
        if not any([request.product_id, request.variant_id, request.sku]):
            logger.error(f"❌ Missing product identifier: user={request.user_id}")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="At least one of product_id, variant_id, or sku is required"
            )
        
        await recommendation_service.track_interaction(
            user_id=request.user_id,
            product_id=request.product_id,
            variant_id=request.variant_id,
            sku=request.sku,
            action=request.action.value
        )
        
        logger.info(f"✅ TRACKING SUCCESSFUL: user={request.user_id}, product={request.product_id}")
        return {"success": True, "message": "Interaction tracked successfully"}
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Failed to track interaction: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to track interaction: {str(e)}"
        )


@router.get("/user/{user_id}", response_model=RecommendationResponse)
async def get_user_recommendations(
    user_id: int,
    limit: int = Query(default=20, ge=1, le=50, description="Number of recommendations")
):
    """
    Get personalized product recommendations for a user.
    
    Based on the user's interaction history and preference vector.
    """
    try:
        logger.info(f"Getting recommendations for user: {user_id}")
        
        recommendations = await recommendation_service.get_user_recommendations(
            user_id=user_id,
            limit=limit
        )
        
        return RecommendationResponse(
            recommendations=recommendations,
            total=len(recommendations)
        )
        
    except Exception as e:
        logger.error(f"Failed to get user recommendations: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to get recommendations: {str(e)}"
        )


@router.get("/similar/{product_id}", response_model=RecommendationResponse)
async def get_similar_products(
    product_id: int,
    limit: int = Query(default=10, ge=1, le=30, description="Number of similar products")
):
    """
    Get products similar to a given product.
    
    Item-to-item recommendations based on vector similarity.
    """
    try:
        logger.info(f"Getting similar products for product: {product_id}")
        
        recommendations = await recommendation_service.get_similar_products(
            product_id=product_id,
            limit=limit
        )
        
        return RecommendationResponse(
            recommendations=recommendations,
            total=len(recommendations)
        )
        
    except Exception as e:
        logger.error(f"Failed to get similar products: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to get similar products: {str(e)}"
        )


@router.get("/homepage/{user_id}", response_model=RecommendationResponse)
async def get_homepage_recommendations(
    user_id: int,
    limit: int = Query(default=20, ge=1, le=50, description="Number of recommendations")
):
    """
    Get homepage recommendations for a user.
    
    Combines user preferences with trending/popular items.
    Anonymous users (user_id=0) get default recommendations.
    """
    try:
        # Treat userId=0 as anonymous/new user
        effective_user_id = user_id if user_id > 0 else None
        logger.info(f"Getting homepage recommendations for user: {user_id} (effective: {effective_user_id})")
        
        # For anonymous users, return default recommendations
        if effective_user_id is None:
            recommendations = await recommendation_service._get_default_recommendations(limit)
        else:
            recommendations = await recommendation_service.get_homepage_recommendations(
                user_id=effective_user_id,
                limit=limit
            )
        
        # Log top 5 recommendations
        print("=" * 80)
        print("🎯 TOP 5 HOMEPAGE RECOMMENDATIONS")
        print(f"User ID: {user_id}")
        print("-" * 80)
        for i, rec in enumerate(recommendations[:5], 1):
            print(f"{i}. Product ID: {rec.get('product_id', 'N/A')}")
            print(f"   SKU: {rec.get('sku', 'N/A')}")
            print(f"   Name: {rec.get('name', 'N/A')}")
            print(f"   Score: {rec.get('score', 0):.4f}")
            print(f"   Reason: {rec.get('reason', 'N/A')}")
            print()
        print("=" * 80)
        
        return RecommendationResponse(
            recommendations=recommendations,
            total=len(recommendations)
        )
        
    except Exception as e:
        logger.error(f"Failed to get homepage recommendations: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to get homepage recommendations: {str(e)}"
        )
