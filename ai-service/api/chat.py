"""
Chat API endpoints for AI-powered conversations.
"""

import logging
from typing import List, Optional, Dict, Any

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from services.chat_service import ChatService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize service
chat_service = ChatService()


# ============== Request/Response Models ==============

class ChatMessage(BaseModel):
    """A single chat message."""
    role: str = Field(..., description="Message role: user or assistant")
    content: str = Field(..., description="Message content")


class UserProfile(BaseModel):
    """User profile for context-aware responses."""
    user_id: int
    user_type: str = Field(..., description="retailer or supplier")
    name: Optional[str] = None
    loyalty_tier: Optional[str] = None  # For retailers


class GenerateResponseRequest(BaseModel):
    """Request model for chat generation."""
    query: str = Field(..., description="User's question")
    history: List[ChatMessage] = Field(default=[], description="Previous messages for context")
    user_profile: Optional[UserProfile] = None
    
    class Config:
        json_schema_extra = {
            "example": {
                "query": "What are the tax requirements for importing electronics to Vietnam?",
                "history": [
                    {"role": "user", "content": "Hi, I need help with taxes"},
                    {"role": "assistant", "content": "Hello! I'd be happy to help with tax-related questions. What would you like to know?"}
                ],
                "user_profile": {
                    "user_id": 123,
                    "user_type": "retailer",
                    "name": "ABC Electronics",
                    "loyalty_tier": "Gold"
                }
            }
        }


class GenerateResponseResult(BaseModel):
    """Response model for chat generation."""
    response: str = Field(..., description="AI-generated response")
    sources: List[Dict[str, Any]] = Field(default=[], description="Sources used for the response")
    query_type: str = Field(..., description="Detected query type: product_search, knowledge, general")


# ============== Endpoints ==============

@router.post("/generate", response_model=GenerateResponseResult)
async def generate_response(request: GenerateResponseRequest):
    """
    Generate an AI response to the user's query.
    
    This endpoint:
    1. Classifies the query type
    2. Retrieves relevant context from vector DB
    3. Generates a response using the LLM
    """
    try:
        logger.info(f"Generating response for query: {request.query[:100]}...")
        
        result = await chat_service.generate_response(
            query=request.query,
            history=[msg.model_dump() for msg in request.history],
            user_profile=request.user_profile.model_dump() if request.user_profile else None
        )
        
        logger.info(f"Response generated successfully, type: {result.get('query_type')}")
        return GenerateResponseResult(**result)
        
    except Exception as e:
        logger.error(f"Failed to generate response: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to generate response: {str(e)}"
        )


@router.post("/classify")
async def classify_query(query: str):
    """
    Classify a user query into categories.
    
    Categories:
    - product_search: Looking for products
    - knowledge: Tax, contracts, guides
    - general: General questions about the platform
    - supplier_info: Questions about suppliers
    """
    try:
        query_type = await chat_service.classify_query(query)
        return {"query": query, "type": query_type}
        
    except Exception as e:
        logger.error(f"Failed to classify query: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to classify query: {str(e)}"
        )
