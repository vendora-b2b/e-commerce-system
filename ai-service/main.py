"""
Vendora AI Service - Main Application Entry Point

This service provides:
- Chatbot functionality with RAG (Retrieval Augmented Generation)
- Product recommendations (item-to-item and user-based)
- Document and product ingestion into vector database
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config import get_settings
from api import ingest_router, chat_router, recommend_router
from services.qdrant_service import QdrantService

# Configure logging
settings = get_settings()
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper()),
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan manager - handles startup and shutdown."""
    # Startup
    logger.info("Starting Vendora AI Service...")
    
    # Initialize Qdrant collections
    qdrant_service = QdrantService()
    await qdrant_service.initialize_collections()
    
    logger.info("AI Service started successfully!")
    
    yield
    
    # Shutdown
    logger.info("Shutting down AI Service...")


# Create FastAPI application
app = FastAPI(
    title="Vendora AI Service",
    description="AI-powered chatbot and recommendation service for Vendora B2B E-Commerce",
    version="1.0.0",
    lifespan=lifespan
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(ingest_router, prefix="/ai/ingest", tags=["Ingestion"])
app.include_router(chat_router, prefix="/ai/chat", tags=["Chat"])
app.include_router(recommend_router, prefix="/ai/recommend", tags=["Recommendations"])


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "service": "vendora-ai-service",
        "version": "1.0.0"
    }


@app.get("/", tags=["Root"])
async def root():
    """Root endpoint with service information."""
    return {
        "service": "Vendora AI Service",
        "version": "1.0.0",
        "endpoints": {
            "health": "/health",
            "docs": "/docs",
            "ingest": "/ai/ingest/*",
            "chat": "/ai/chat/*",
            "recommend": "/ai/recommend/*"
        }
    }
