"""
Ingestion API endpoints for products and documents.
"""

import logging
from typing import List, Optional

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from services.qdrant_service import QdrantService
from services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize services
qdrant_service = QdrantService()
embedding_service = EmbeddingService()


# ============== Request/Response Models ==============

class ProductIngestRequest(BaseModel):
    """Request model for product ingestion."""
    sku: str = Field(..., description="Product SKU")
    product_id: int = Field(..., description="Product ID from MySQL")
    name: str = Field(..., description="Product name")
    description: str = Field(..., description="Product description")
    supplier_id: int = Field(..., description="Supplier ID")
    category: Optional[str] = Field(None, description="Product category")
    
    class Config:
        json_schema_extra = {
            "example": {
                "sku": "SHOE-NIKE-001",
                "product_id": 123,
                "name": "Nike Air Max 90",
                "description": "Classic Nike Air Max 90 sneakers with visible Air cushioning",
                "supplier_id": 55,
                "category": "footwear"
            }
        }


class BulkProductIngestRequest(BaseModel):
    """Request model for bulk product ingestion."""
    products: List[ProductIngestRequest]


class DocumentIngestRequest(BaseModel):
    """Request model for document ingestion."""
    text: str = Field(..., description="Document text content")
    doc_type: str = Field(..., description="Document type: contract, tax, guide")
    title: Optional[str] = Field(None, description="Document title")
    region: Optional[str] = Field(None, description="Region/country code (e.g., VN, US)")
    source: Optional[str] = Field(None, description="Source filename or URL")
    
    class Config:
        json_schema_extra = {
            "example": {
                "text": "According to Vietnamese tax law 2024, B2B transactions are subject to...",
                "doc_type": "tax",
                "title": "Vietnam B2B Tax Guide 2024",
                "region": "VN",
                "source": "tax_code_2024.pdf"
            }
        }


class IngestResponse(BaseModel):
    """Response model for ingestion operations."""
    success: bool
    message: str
    ingested_count: int = 0


# ============== Endpoints ==============

@router.post("/product", response_model=IngestResponse)
async def ingest_product(request: ProductIngestRequest):
    """
    Ingest a single product into the vector database.
    
    Called when a supplier creates or updates a product.
    """
    try:
        logger.info(f"Ingesting product: {request.sku}")
        
        # Create text for embedding (combine name and description)
        text_to_embed = f"{request.name}. {request.description}"
        
        # Generate embedding
        embedding = await embedding_service.embed_text(text_to_embed)
        
        # Prepare metadata (basic info only, as per your requirement)
        metadata = {
            "sku": request.sku,
            "product_id": request.product_id,
            "name": request.name,
            "description": request.description,
            "supplier_id": request.supplier_id,
            "category": request.category or "uncategorized"
        }
        
        # Store in Qdrant
        await qdrant_service.upsert_product(
            product_id=request.product_id,
            embedding=embedding,
            metadata=metadata
        )
        
        logger.info(f"Successfully ingested product: {request.sku}")
        return IngestResponse(
            success=True,
            message=f"Product {request.sku} ingested successfully",
            ingested_count=1
        )
        
    except Exception as e:
        logger.error(f"Failed to ingest product {request.sku}: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to ingest product: {str(e)}"
        )


@router.post("/products/bulk", response_model=IngestResponse)
async def ingest_products_bulk(request: BulkProductIngestRequest):
    """
    Bulk ingest multiple products into the vector database.
    
    Used for initial sync or batch updates.
    """
    try:
        logger.info(f"Bulk ingesting {len(request.products)} products")
        
        ingested_count = 0
        for product in request.products:
            try:
                text_to_embed = f"{product.name}. {product.description}"
                embedding = await embedding_service.embed_text(text_to_embed)
                
                metadata = {
                    "sku": product.sku,
                    "product_id": product.product_id,
                    "name": product.name,
                    "description": product.description,
                    "supplier_id": product.supplier_id,
                    "category": product.category or "uncategorized"
                }
                
                await qdrant_service.upsert_product(
                    product_id=product.product_id,
                    embedding=embedding,
                    metadata=metadata
                )
                ingested_count += 1
                
            except Exception as e:
                logger.warning(f"Failed to ingest product {product.sku}: {str(e)}")
                continue
        
        logger.info(f"Bulk ingestion complete: {ingested_count}/{len(request.products)} products")
        return IngestResponse(
            success=True,
            message=f"Bulk ingestion complete",
            ingested_count=ingested_count
        )
        
    except Exception as e:
        logger.error(f"Bulk ingestion failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Bulk ingestion failed: {str(e)}"
        )


@router.post("/document", response_model=IngestResponse)
async def ingest_document(request: DocumentIngestRequest):
    """
    Ingest a document (tax info, contract template, guide) into the knowledge base.
    
    Documents are chunked and stored for RAG retrieval.
    """
    try:
        logger.info(f"Ingesting document: {request.title or 'Untitled'}")
        
        # For now, treat the entire text as one chunk
        # TODO: Implement proper chunking for large documents
        embedding = await embedding_service.embed_text(request.text)
        
        metadata = {
            "doc_type": request.doc_type,
            "title": request.title or "Untitled",
            "region": request.region or "global",
            "source": request.source or "unknown",
            "text": request.text[:1000]  # Store truncated text for reference
        }
        
        await qdrant_service.upsert_document(
            embedding=embedding,
            metadata=metadata
        )
        
        logger.info(f"Successfully ingested document: {request.title}")
        return IngestResponse(
            success=True,
            message=f"Document ingested successfully",
            ingested_count=1
        )
        
    except Exception as e:
        logger.error(f"Failed to ingest document: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to ingest document: {str(e)}"
        )


@router.delete("/product/{product_id}")
async def delete_product(product_id: int):
    """
    Remove a product from the vector database.
    
    Called when a supplier deletes a product.
    Uses product_id (not SKU) since that's the point ID in Qdrant.
    """
    try:
        logger.info(f"Deleting product: {product_id}")
        
        await qdrant_service.delete_product(product_id)
        
        return {"success": True, "message": f"Product {product_id} deleted"}
        
    except Exception as e:
        logger.error(f"Failed to delete product {product_id}: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to delete product: {str(e)}"
        )
