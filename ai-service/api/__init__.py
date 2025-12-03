"""API routers package."""

from .ingest import router as ingest_router
from .chat import router as chat_router
from .recommend import router as recommend_router
from .search import router as search_router

__all__ = ["ingest_router", "chat_router", "recommend_router", "search_router"]
