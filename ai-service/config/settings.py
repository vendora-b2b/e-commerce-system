"""
Application settings and configuration.
Uses pydantic-settings for environment variable management.
"""

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""
    
    # Qdrant Configuration
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    
    # Spring Boot Backend
    spring_boot_url: str = "http://localhost:8080"
    
    # LLM API Keys
    google_api_key: str = ""
    openai_api_key: str = ""
    
    # Embedding Configuration
    embedding_model: str = "all-MiniLM-L6-v2"
    embedding_dimension: int = 384  # Dimension for all-MiniLM-L6-v2
    
    # Collection Names
    knowledge_base_collection: str = "knowledge_base"
    product_catalog_collection: str = "product_catalog"
    supplier_catalog_collection: str = "supplier_catalog"
    user_vectors_collection: str = "user_vectors"
    
    # Recommendation Settings
    user_vector_decay: float = 0.6  # Lower decay = more influence from new products (was 0.95)
    view_weight: float = 2.0  # Increased from 1.0 to make views more impactful
    add_to_cart_weight: float = 4.0  # Increased from 2.0
    order_weight: float = 8.0  # Increased from 5.0
    
    # Chat Settings
    chat_context_limit: int = 10  # Number of previous messages to include
    
    # Logging
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance."""
    return Settings()
