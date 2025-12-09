"""
Search API endpoints for combined product and supplier search.
"""

import logging
from typing import List, Optional

from fastapi import APIRouter, HTTPException, status, Query
from pydantic import BaseModel, Field

from services.qdrant_service import QdrantService
from services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize services
qdrant_service = QdrantService()
embedding_service = EmbeddingService()


# ============== Response Models ==============

class ProductSearchResult(BaseModel):
    """A single product search result."""
    productId: int = Field(..., description="Product ID")
    sku: str = Field(..., description="Product SKU")
    name: str = Field(..., description="Product name")
    description: str = Field("", description="Product description")
    supplierId: int = Field(..., description="Supplier ID")
    category: str = Field("", description="Product category")
    score: float = Field(..., description="Relevance score (0-1)")


class SupplierSearchResult(BaseModel):
    """A single supplier search result."""
    supplierId: int = Field(..., description="Supplier ID")
    name: str = Field(..., description="Supplier name")
    email: str = Field(..., description="Contact email")
    phone: str = Field("", description="Contact phone")
    address: str = Field("", description="Business address")
    businessLicense: str = Field("", description="Business license")
    score: float = Field(..., description="Relevance score (0-1)")


class CombinedSearchResponse(BaseModel):
    """Response model for combined search."""
    products: List[ProductSearchResult] = Field(default_factory=list)
    suppliers: List[SupplierSearchResult] = Field(default_factory=list)
    totalProducts: int = 0
    totalSuppliers: int = 0
    query: str = ""


class ProductOnlySearchResponse(BaseModel):
    """Response model for product-only search."""
    products: List[ProductSearchResult] = Field(default_factory=list)
    total: int = 0
    query: str = ""


class SupplierOnlySearchResponse(BaseModel):
    """Response model for supplier-only search."""
    suppliers: List[SupplierSearchResult] = Field(default_factory=list)
    total: int = 0
    query: str = ""


# ============== Endpoints ==============

@router.get("/combined", response_model=CombinedSearchResponse)
async def search_combined(
    query: str = Query(..., min_length=1, description="Search query"),
    productLimit: int = Query(default=10, ge=1, le=50, alias="productLimit", description="Max products to return"),
    supplierLimit: int = Query(default=5, ge=1, le=20, alias="supplierLimit", description="Max suppliers to return")
):
    """
    Search for both products and suppliers in a single query.
    
    This endpoint is designed for search bar functionality where users
    want to find both products and suppliers that match their query.
    
    Returns products and suppliers sorted by relevance score.
    """
    try:
        logger.info(f"Combined search for: '{query}' (products: {productLimit}, suppliers: {supplierLimit})")
        
        # Generate embedding for the query
        query_embedding = await embedding_service.embed_text(query)
        
        # Search products and suppliers in parallel conceptually
        # (Python async runs them concurrently)
        # Use threshold for search to ensure quality results
        product_results = await qdrant_service.search_products(
            query_vector=query_embedding,
            limit=productLimit,
            score_threshold=0.6  # Higher threshold for search bar results
        )
        
        supplier_results = await qdrant_service.search_suppliers(
            query_vector=query_embedding,
            limit=supplierLimit,
            score_threshold=0.6  # Higher threshold for search bar results
        )
        
        # Transform product results
        products = [
            ProductSearchResult(
                productId=r.get("product_id", r.get("id", 0)),
                sku=r.get("sku", ""),
                name=r.get("name", ""),
                description=r.get("description", ""),
                supplierId=r.get("supplier_id", 0),
                category=r.get("category", ""),
                score=r.get("score", 0.0)
            )
            for r in product_results
        ]
        
        # Transform supplier results
        suppliers = [
            SupplierSearchResult(
                supplierId=r.get("supplier_id", r.get("id", 0)),
                name=r.get("name", ""),
                email=r.get("email", ""),
                phone=r.get("phone", ""),
                address=r.get("address", ""),
                businessLicense=r.get("business_license", ""),
                score=r.get("score", 0.0)
            )
            for r in supplier_results
        ]
        
        logger.info(f"Combined search completed: {len(products)} products, {len(suppliers)} suppliers")
        
        return CombinedSearchResponse(
            products=products,
            suppliers=suppliers,
            totalProducts=len(products),
            totalSuppliers=len(suppliers),
            query=query
        )
        
    except Exception as e:
        logger.error(f"Combined search failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Search failed: {str(e)}"
        )


@router.get("/products", response_model=ProductOnlySearchResponse)
async def search_products(
    query: str = Query(..., min_length=1, description="Search query"),
    limit: int = Query(default=20, ge=1, le=100, description="Max results to return")
):
    """
    Search for products only.
    
    Use this endpoint when you only need product results.
    """
    try:
        logger.info(f"Product search for: '{query}' (limit: {limit})")
        
        # Generate embedding for the query
        query_embedding = await embedding_service.embed_text(query)
        
        # Search products with quality threshold
        results = await qdrant_service.search_products(
            query_vector=query_embedding,
            score_threshold=0.6,  # Higher threshold for search bar results
            limit=limit
        )
        
        # Transform results
        products = [
            ProductSearchResult(
                productId=r.get("product_id", r.get("id", 0)),
                sku=r.get("sku", ""),
                name=r.get("name", ""),
                description=r.get("description", ""),
                supplierId=r.get("supplier_id", 0),
                category=r.get("category", ""),
                score=r.get("score", 0.0)
            )
            for r in results
        ]
        
        logger.info(f"Product search completed: {len(products)} products")
        
        return ProductOnlySearchResponse(
            products=products,
            total=len(products),
            query=query
        )
        
    except Exception as e:
        logger.error(f"Product search failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Search failed: {str(e)}"
        )


@router.get("/suppliers", response_model=SupplierOnlySearchResponse)
async def search_suppliers(
    query: str = Query(..., min_length=1, description="Search query"),
    limit: int = Query(default=10, ge=1, le=50, description="Max results to return")
):
    """
    Search for suppliers only.
    
    Use this endpoint when you only need supplier results.
    """
    try:
        logger.info(f"Supplier search for: '{query}' (limit: {limit})")
        
        # Generate embedding for the query
        query_embedding = await embedding_service.embed_text(query)
        
        # Search suppliers with quality threshold
        results = await qdrant_service.search_suppliers(
            query_vector=query_embedding,
            limit=limit,
            score_threshold=0.6  # Higher threshold for search bar results
        )
        
        # Transform results
        suppliers = [
            SupplierSearchResult(
                supplierId=r.get("supplier_id", r.get("id", 0)),
                name=r.get("name", ""),
                email=r.get("email", ""),
                phone=r.get("phone", ""),
                address=r.get("address", ""),
                businessLicense=r.get("business_license", ""),
                score=r.get("score", 0.0)
            )
            for r in results
        ]
        
        logger.info(f"Supplier search completed: {len(suppliers)} suppliers")
        
        return SupplierOnlySearchResponse(
            suppliers=suppliers,
            total=len(suppliers),
            query=query
        )
        
    except Exception as e:
        logger.error(f"Supplier search failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Search failed: {str(e)}"
        )
