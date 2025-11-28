"""
Chat Service.

Handles AI-powered chat generation using RAG (Retrieval Augmented Generation).
"""

import logging
from typing import List, Dict, Any, Optional

import google.generativeai as genai

from config import get_settings
from services.qdrant_service import QdrantService
from services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
settings = get_settings()


class ChatService:
    """Service for AI chat generation with RAG."""
    
    def __init__(self):
        """Initialize chat service with LLM and vector search."""
        self.qdrant_service = QdrantService()
        self.embedding_service = EmbeddingService()
        
        # Configure Gemini
        if settings.google_api_key:
            genai.configure(api_key=settings.google_api_key)
            self.model = genai.GenerativeModel('gemini-1.5-flash')
            self.llm_provider = "gemini"
            logger.info("Chat service initialized with Gemini")
        else:
            self.model = None
            self.llm_provider = None
            logger.warning("No LLM API key configured - chat will use fallback responses")
            
    async def classify_query(self, query: str) -> str:
        """
        Classify the user's query into categories.
        
        Categories:
        - product_search: Looking for products
        - knowledge: Tax, contracts, guides
        - general: General questions about the platform
        - supplier_info: Questions about suppliers
        """
        query_lower = query.lower()
        
        # Simple keyword-based classification
        # In production, use LLM for better classification
        product_keywords = ['product', 'buy', 'purchase', 'price', 'shoe', 'clothing', 
                           'find', 'search', 'looking for', 'need', 'want']
        knowledge_keywords = ['tax', 'contract', 'law', 'regulation', 'guide', 'how to',
                             'policy', 'legal', 'template', 'document']
        supplier_keywords = ['supplier', 'vendor', 'seller', 'who sells', 'contact']
        
        if any(kw in query_lower for kw in knowledge_keywords):
            return "knowledge"
        elif any(kw in query_lower for kw in supplier_keywords):
            return "supplier_info"
        elif any(kw in query_lower for kw in product_keywords):
            return "product_search"
        else:
            return "general"
            
    async def generate_response(
        self,
        query: str,
        history: List[Dict[str, str]] = None,
        user_profile: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Generate a response to the user's query using RAG.
        
        Steps:
        1. Classify the query
        2. Retrieve relevant context from vector DB
        3. Generate response with LLM
        """
        history = history or []
        
        # Step 1: Classify query
        query_type = await self.classify_query(query)
        logger.info(f"Query classified as: {query_type}")
        
        # Step 2: Retrieve context
        context_docs = []
        sources = []
        
        query_embedding = await self.embedding_service.embed_text(query)
        
        if query_type == "product_search":
            # Search product catalog
            results = await self.qdrant_service.search_products(
                query_vector=query_embedding,
                limit=5
            )
            context_docs = [
                f"Product: {r['name']} (SKU: {r['sku']}) - {r.get('description', 'No description')}"
                for r in results
            ]
            sources = [{"type": "product", "id": r["product_id"], "name": r["name"]} for r in results]
            
        elif query_type == "knowledge":
            # Search knowledge base
            results = await self.qdrant_service.search_knowledge_base(
                query_vector=query_embedding,
                limit=3
            )
            context_docs = [
                f"[{r.get('doc_type', 'document').upper()}] {r.get('title', 'Untitled')}: {r.get('text', '')[:500]}"
                for r in results
            ]
            sources = [{"type": r.get("doc_type"), "title": r.get("title"), "source": r.get("source")} for r in results]
            
        elif query_type == "supplier_info":
            # For supplier info, we'd call Spring Boot API via MCP
            # For now, search products to find supplier info
            results = await self.qdrant_service.search_products(
                query_vector=query_embedding,
                limit=3
            )
            context_docs = [
                f"Supplier ID {r['supplier_id']} sells: {r['name']}"
                for r in results
            ]
            sources = [{"type": "supplier", "supplier_id": r["supplier_id"]} for r in results]
        
        # Step 3: Generate response
        response_text = await self._generate_llm_response(
            query=query,
            context=context_docs,
            history=history,
            user_profile=user_profile,
            query_type=query_type
        )
        
        return {
            "response": response_text,
            "sources": sources,
            "query_type": query_type
        }
        
    async def _generate_llm_response(
        self,
        query: str,
        context: List[str],
        history: List[Dict[str, str]],
        user_profile: Optional[Dict[str, Any]],
        query_type: str
    ) -> str:
        """Generate response using LLM."""
        
        if not self.model:
            # Fallback response when no LLM is configured
            return self._generate_fallback_response(query, context, query_type)
            
        try:
            # Build prompt
            system_prompt = self._build_system_prompt(user_profile)
            context_text = "\n".join(context) if context else "No specific context available."
            
            # Format conversation history
            history_text = ""
            if history:
                for msg in history[-settings.chat_context_limit:]:
                    role = "User" if msg["role"] == "user" else "Assistant"
                    history_text += f"{role}: {msg['content']}\n"
            
            full_prompt = f"""{system_prompt}

### Relevant Context:
{context_text}

### Conversation History:
{history_text}

### Current Question:
User: {query}

### Response:
Please provide a helpful response based on the context above. If the context doesn't contain relevant information, say so politely and offer to help in other ways."""

            # Generate response
            response = self.model.generate_content(full_prompt)
            return response.text
            
        except Exception as e:
            logger.error(f"LLM generation failed: {str(e)}")
            return self._generate_fallback_response(query, context, query_type)
            
    def _build_system_prompt(self, user_profile: Optional[Dict[str, Any]]) -> str:
        """Build the system prompt based on user profile."""
        base_prompt = """You are a helpful AI assistant for Vendora, a B2B e-commerce marketplace platform.
You help retailers and suppliers with:
- Finding products
- Understanding tax regulations and contracts
- Using the platform features
- Connecting with suppliers

Be professional, concise, and helpful. If you're unsure about something, say so."""

        if user_profile:
            user_type = user_profile.get("user_type", "user")
            name = user_profile.get("name", "")
            loyalty_tier = user_profile.get("loyalty_tier", "")
            
            if user_type == "retailer" and loyalty_tier:
                base_prompt += f"\n\nYou are speaking with a {loyalty_tier} tier retailer"
                if name:
                    base_prompt += f" ({name})"
                base_prompt += ". Provide personalized service appropriate to their tier level."
            elif user_type == "supplier":
                base_prompt += f"\n\nYou are speaking with a supplier"
                if name:
                    base_prompt += f" ({name})"
                base_prompt += ". Help them manage their products and orders."
                
        return base_prompt
        
    def _generate_fallback_response(
        self,
        query: str,
        context: List[str],
        query_type: str
    ) -> str:
        """Generate a fallback response when LLM is unavailable."""
        
        if not context:
            return "I apologize, but I couldn't find relevant information for your query. Please try rephrasing your question or contact our support team for assistance."
            
        if query_type == "product_search":
            return f"Based on your search, here are some relevant products:\n\n" + "\n".join(f"• {c}" for c in context)
        elif query_type == "knowledge":
            return f"Here's what I found in our knowledge base:\n\n" + "\n".join(context)
        else:
            return f"Here's some information that might help:\n\n" + "\n".join(context)
