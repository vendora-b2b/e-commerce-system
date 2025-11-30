"""
Embedding Service.

Handles text-to-vector conversions using sentence transformers.
"""

import logging
from typing import List
from functools import lru_cache

from sentence_transformers import SentenceTransformer

from config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class EmbeddingService:
    """Service for generating text embeddings."""
    
    _instance = None
    _model = None
    
    def __new__(cls):
        """Singleton pattern to reuse model instance."""
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance
    
    def __init__(self):
        """Initialize the embedding model."""
        if EmbeddingService._model is None:
            logger.info(f"Loading embedding model: {settings.embedding_model}")
            EmbeddingService._model = SentenceTransformer(settings.embedding_model)
            logger.info("Embedding model loaded successfully")
            
    @property
    def model(self) -> SentenceTransformer:
        """Get the embedding model."""
        return EmbeddingService._model
        
    async def embed_text(self, text: str) -> List[float]:
        """
        Generate embedding for a single text.
        
        Args:
            text: The text to embed
            
        Returns:
            List of floats representing the embedding vector
        """
        try:
            # Clean and prepare text
            text = text.strip()
            if not text:
                raise ValueError("Cannot embed empty text")
                
            # Generate embedding
            embedding = self.model.encode(text, convert_to_numpy=True)
            
            return embedding.tolist()
            
        except Exception as e:
            logger.error(f"Failed to embed text: {str(e)}")
            raise
            
    async def embed_texts(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for multiple texts.
        
        Args:
            texts: List of texts to embed
            
        Returns:
            List of embedding vectors
        """
        try:
            # Clean texts
            cleaned_texts = [t.strip() for t in texts if t.strip()]
            
            if not cleaned_texts:
                raise ValueError("No valid texts to embed")
                
            # Generate embeddings in batch
            embeddings = self.model.encode(cleaned_texts, convert_to_numpy=True)
            
            return [emb.tolist() for emb in embeddings]
            
        except Exception as e:
            logger.error(f"Failed to embed texts: {str(e)}")
            raise
            
    def get_embedding_dimension(self) -> int:
        """Get the dimension of the embedding vectors."""
        return self.model.get_sentence_embedding_dimension()
