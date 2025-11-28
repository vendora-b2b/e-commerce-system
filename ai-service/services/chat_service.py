"""
Chat Service - Agentic RAG Implementation.

Uses LLM-based intent classification and routing for multi-domain chat.
Architecture:
  1. LLM Router (Gemini Flash) - Classifies intent, extracts filters
  2. Parallel Retrieval - Fetches from multiple sources if needed
  3. LLM Generator (Gemini Pro) - Generates final response with context
"""

import json
import logging
import asyncio
from typing import List, Dict, Any, Optional
from dataclasses import dataclass

import google.generativeai as genai

from config import get_settings
from services.qdrant_service import QdrantService
from services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
settings = get_settings()


@dataclass
class QueryIntent:
    """Structured output from LLM router."""
    intents: List[str]  # e.g., ["product_search", "tax_question"]
    product_filters: Optional[Dict[str, Any]] = None  # e.g., {"category": "electronics"}
    knowledge_filters: Optional[Dict[str, Any]] = None  # e.g., {"doc_type": "tax", "region": "VN"}
    requires_realtime_data: bool = False  # If true, needs MCP call to Spring Boot
    confidence: float = 1.0


class ChatService:
    """Service for AI chat generation with Agentic RAG."""
    
    # Intent classification prompt for the router LLM
    ROUTER_PROMPT = """You are a query router for a B2B e-commerce platform (Vendora).
Analyze the user's query and determine what information sources are needed.

Available intents:
- product_search: User wants to find, buy, or learn about products
- tax_question: Questions about taxes, duties, import/export regulations
- contract_help: Questions about contracts, agreements, legal templates
- supplier_info: Questions about suppliers, vendors, seller information
- platform_help: Questions about how to use the platform
- general: General conversation or unclear intent

Instructions:
1. A query can have MULTIPLE intents (e.g., "Find laptops and what's the import tax?" = product_search + tax_question)
2. Extract any filters mentioned (category, region, price range, etc.)
3. Set requires_realtime_data=true if the query needs live data (inventory, order status, user account info)

Respond ONLY with valid JSON in this exact format:
{
  "intents": ["intent1", "intent2"],
  "product_filters": {"category": "value", "price_max": 1000} or null,
  "knowledge_filters": {"doc_type": "tax", "region": "VN"} or null,
  "requires_realtime_data": false,
  "confidence": 0.95
}

User query: """

    def __init__(self):
        """Initialize chat service with LLM and vector search."""
        self.qdrant_service = QdrantService()
        self.embedding_service = EmbeddingService()
        
        # Configure Gemini
        if settings.google_api_key:
            genai.configure(api_key=settings.google_api_key)
            # Use Flash for routing (fast, cheap), Pro for generation (better quality)
            self.router_model = genai.GenerativeModel('gemini-1.5-flash')
            self.generator_model = genai.GenerativeModel('gemini-1.5-flash')  # Can upgrade to gemini-1.5-pro
            self.llm_available = True
            logger.info("Chat service initialized with Gemini (Agentic RAG mode)")
        else:
            self.router_model = None
            self.generator_model = None
            self.llm_available = False
            logger.warning("No LLM API key configured - using fallback keyword classification")
    
    async def classify_query_with_llm(self, query: str) -> QueryIntent:
        """
        Use LLM to classify query intent and extract filters.
        
        This is the "Agent" part of Agentic RAG - the LLM decides:
        - What sources to query
        - What filters to apply
        - Whether real-time data is needed
        """
        if not self.llm_available:
            return self._fallback_classification(query)
        
        try:
            prompt = self.ROUTER_PROMPT + query
            
            response = self.router_model.generate_content(
                prompt,
                generation_config=genai.GenerationConfig(
                    response_mime_type="application/json",
                    temperature=0.1  # Low temperature for consistent classification
                )
            )
            
            # Parse JSON response
            result = json.loads(response.text)
            
            intent = QueryIntent(
                intents=result.get("intents", ["general"]),
                product_filters=result.get("product_filters"),
                knowledge_filters=result.get("knowledge_filters"),
                requires_realtime_data=result.get("requires_realtime_data", False),
                confidence=result.get("confidence", 0.8)
            )
            
            logger.info(f"LLM classified query: intents={intent.intents}, confidence={intent.confidence}")
            return intent
            
        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse LLM router response: {e}")
            return self._fallback_classification(query)
        except Exception as e:
            logger.error(f"LLM router failed: {e}")
            return self._fallback_classification(query)
    
    def _fallback_classification(self, query: str) -> QueryIntent:
        """Fallback to keyword-based classification when LLM is unavailable."""
        query_lower = query.lower()
        intents = []
        
        # Keyword matching as fallback
        if any(kw in query_lower for kw in ['product', 'buy', 'purchase', 'price', 'find', 'search', 'looking for']):
            intents.append("product_search")
        if any(kw in query_lower for kw in ['tax', 'duty', 'import', 'export', 'regulation']):
            intents.append("tax_question")
        if any(kw in query_lower for kw in ['contract', 'agreement', 'legal', 'template']):
            intents.append("contract_help")
        if any(kw in query_lower for kw in ['supplier', 'vendor', 'seller', 'who sells']):
            intents.append("supplier_info")
        if any(kw in query_lower for kw in ['how to', 'how do i', 'help me', 'guide']):
            intents.append("platform_help")
        
        if not intents:
            intents = ["general"]
            
        return QueryIntent(intents=intents, confidence=0.6)
    
    async def retrieve_context(
        self,
        query: str,
        intent: QueryIntent
    ) -> Dict[str, List[Dict[str, Any]]]:
        """
        Retrieve context from multiple sources in parallel based on intent.
        
        Returns a dict with results from each source.
        """
        query_embedding = await self.embedding_service.embed_text(query)
        
        # Build retrieval tasks based on intents
        tasks = {}
        
        if "product_search" in intent.intents:
            tasks["products"] = self._search_products(query_embedding, intent.product_filters)
            
        if "tax_question" in intent.intents:
            filters = intent.knowledge_filters or {}
            filters["doc_type"] = "tax"
            tasks["tax_docs"] = self._search_knowledge(query_embedding, filters)
            
        if "contract_help" in intent.intents:
            filters = intent.knowledge_filters or {}
            filters["doc_type"] = "contract"
            tasks["contract_docs"] = self._search_knowledge(query_embedding, filters)
            
        if "supplier_info" in intent.intents:
            tasks["suppliers"] = self._search_products(query_embedding, intent.product_filters)
            
        if "platform_help" in intent.intents:
            filters = {"doc_type": "guide"}
            tasks["guides"] = self._search_knowledge(query_embedding, filters)
        
        # Execute all retrieval tasks in parallel
        if tasks:
            results = await asyncio.gather(*tasks.values(), return_exceptions=True)
            context = {}
            for key, result in zip(tasks.keys(), results):
                if isinstance(result, Exception):
                    logger.error(f"Retrieval failed for {key}: {result}")
                    context[key] = []
                else:
                    context[key] = result
            return context
        
        return {}
    
    async def _search_products(
        self,
        query_embedding: List[float],
        filters: Optional[Dict[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        """Search product catalog."""
        try:
            # Convert intent filters to Qdrant filters
            qdrant_filters = {}
            if filters:
                if "category" in filters:
                    qdrant_filters["category"] = filters["category"]
                if "supplier_id" in filters:
                    qdrant_filters["supplier_id"] = filters["supplier_id"]
            
            results = await self.qdrant_service.search_products(
                query_vector=query_embedding,
                limit=5,
                filters=qdrant_filters if qdrant_filters else None
            )
            return results
        except Exception as e:
            logger.error(f"Product search failed: {e}")
            return []
    
    async def _search_knowledge(
        self,
        query_embedding: List[float],
        filters: Optional[Dict[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        """Search knowledge base."""
        try:
            qdrant_filters = {}
            if filters:
                if "doc_type" in filters:
                    qdrant_filters["doc_type"] = filters["doc_type"]
                if "region" in filters:
                    qdrant_filters["region"] = filters["region"]
            
            results = await self.qdrant_service.search_knowledge_base(
                query_vector=query_embedding,
                limit=3,
                filters=qdrant_filters if qdrant_filters else None
            )
            return results
        except Exception as e:
            logger.error(f"Knowledge search failed: {e}")
            return []

    async def generate_response(
        self,
        query: str,
        history: List[Dict[str, str]] = None,
        user_profile: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Generate a response using Agentic RAG.
        
        Pipeline:
        1. LLM Router classifies intent and extracts filters
        2. Parallel retrieval from relevant sources
        3. LLM Generator produces final response with context
        """
        history = history or []
        
        # Step 1: Agentic Classification
        intent = await self.classify_query_with_llm(query)
        logger.info(f"Query intents: {intent.intents} (confidence: {intent.confidence})")
        
        # Step 2: Parallel Retrieval
        context = await self.retrieve_context(query, intent)
        
        # Build sources list for response
        sources = self._extract_sources(context)
        
        # Step 3: Generate Response
        response_text = await self._generate_llm_response(
            query=query,
            context=context,
            history=history,
            user_profile=user_profile,
            intent=intent
        )
        
        return {
            "response": response_text,
            "sources": sources,
            "intents": intent.intents,
            "confidence": intent.confidence
        }
    
    def _extract_sources(self, context: Dict[str, List[Dict]]) -> List[Dict[str, Any]]:
        """Extract source references from retrieval results."""
        sources = []
        
        if "products" in context:
            for p in context["products"]:
                sources.append({
                    "type": "product",
                    "id": p.get("product_id"),
                    "name": p.get("name"),
                    "sku": p.get("sku")
                })
        
        for doc_type in ["tax_docs", "contract_docs", "guides"]:
            if doc_type in context:
                for d in context[doc_type]:
                    sources.append({
                        "type": d.get("doc_type", "document"),
                        "title": d.get("title"),
                        "source": d.get("source"),
                        "region": d.get("region")
                    })
        
        if "suppliers" in context:
            seen_suppliers = set()
            for p in context["suppliers"]:
                sid = p.get("supplier_id")
                if sid and sid not in seen_suppliers:
                    sources.append({
                        "type": "supplier",
                        "supplier_id": sid
                    })
                    seen_suppliers.add(sid)
        
        return sources
    
    def _format_context_for_prompt(self, context: Dict[str, List[Dict]]) -> str:
        """Format retrieved context into a prompt-friendly string."""
        sections = []
        
        if context.get("products"):
            product_text = "### Products Found:\n"
            for p in context["products"]:
                product_text += f"- {p.get('name')} (SKU: {p.get('sku')}): {p.get('description', 'No description')[:200]}\n"
            sections.append(product_text)
        
        if context.get("tax_docs"):
            tax_text = "### Tax & Regulation Information:\n"
            for d in context["tax_docs"]:
                tax_text += f"[{d.get('title', 'Tax Document')}] ({d.get('region', 'Global')}): {d.get('text', '')[:400]}\n\n"
            sections.append(tax_text)
        
        if context.get("contract_docs"):
            contract_text = "### Contract & Legal Information:\n"
            for d in context["contract_docs"]:
                contract_text += f"[{d.get('title', 'Contract Document')}]: {d.get('text', '')[:400]}\n\n"
            sections.append(contract_text)
        
        if context.get("guides"):
            guide_text = "### Platform Guides:\n"
            for d in context["guides"]:
                guide_text += f"[{d.get('title', 'Guide')}]: {d.get('text', '')[:400]}\n\n"
            sections.append(guide_text)
        
        if context.get("suppliers"):
            supplier_text = "### Supplier Information:\n"
            seen = set()
            for p in context["suppliers"]:
                sid = p.get("supplier_id")
                if sid and sid not in seen:
                    supplier_text += f"- Supplier {sid} sells: {p.get('name')}\n"
                    seen.add(sid)
            sections.append(supplier_text)
        
        return "\n".join(sections) if sections else "No specific context available."

    async def _generate_llm_response(
        self,
        query: str,
        context: Dict[str, List[Dict]],
        history: List[Dict[str, str]],
        user_profile: Optional[Dict[str, Any]],
        intent: QueryIntent
    ) -> str:
        """Generate response using LLM with retrieved context."""
        
        if not self.llm_available:
            return self._generate_fallback_response(query, context, intent)
            
        try:
            system_prompt = self._build_system_prompt(user_profile)
            context_text = self._format_context_for_prompt(context)
            
            # Format conversation history
            history_text = ""
            if history:
                for msg in history[-settings.chat_context_limit:]:
                    role = "User" if msg["role"] == "user" else "Assistant"
                    history_text += f"{role}: {msg['content']}\n"
            
            # Include intent info for better responses
            intent_hint = f"User's query involves: {', '.join(intent.intents)}"
            
            full_prompt = f"""{system_prompt}

{intent_hint}

### Retrieved Context:
{context_text}

### Conversation History:
{history_text}

### Current Question:
User: {query}

### Instructions:
1. Answer based on the retrieved context above
2. If multiple topics are covered (e.g., products AND taxes), address each clearly
3. If context is insufficient, acknowledge what you don't know
4. Be concise but thorough
5. For products, mention key details (name, SKU, supplier)
6. For tax/legal questions, cite the source document if available

### Response:"""

            response = self.generator_model.generate_content(
                full_prompt,
                generation_config=genai.GenerationConfig(
                    temperature=0.7,
                    max_output_tokens=1024
                )
            )
            return response.text
            
        except Exception as e:
            logger.error(f"LLM generation failed: {str(e)}")
            return self._generate_fallback_response(query, context, intent)
            
    def _build_system_prompt(self, user_profile: Optional[Dict[str, Any]]) -> str:
        """Build the system prompt based on user profile."""
        base_prompt = """You are a helpful AI assistant for Vendora, a B2B e-commerce marketplace platform.
You help retailers and suppliers with:
- Finding products from various suppliers
- Understanding tax regulations, import/export duties
- Contract templates and legal documents
- Platform usage and features

Be professional, accurate, and helpful. Cite sources when available."""

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
                base_prompt += ". Help them manage their products and understand marketplace policies."
                
        return base_prompt
        
    def _generate_fallback_response(
        self,
        query: str,
        context: Dict[str, List[Dict]],
        intent: QueryIntent
    ) -> str:
        """Generate a fallback response when LLM is unavailable."""
        
        if not any(context.values()):
            return "I apologize, but I couldn't find relevant information for your query. Please try rephrasing your question or contact our support team for assistance."
        
        response_parts = []
        
        if context.get("products"):
            response_parts.append("**Products Found:**\n" + "\n".join(
                f"• {p.get('name')} (SKU: {p.get('sku')})" for p in context["products"]
            ))
        
        if context.get("tax_docs"):
            response_parts.append("**Tax Information:**\n" + "\n".join(
                f"• {d.get('title', 'Document')}: {d.get('text', '')[:200]}..." for d in context["tax_docs"]
            ))
        
        if context.get("contract_docs"):
            response_parts.append("**Contract Information:**\n" + "\n".join(
                f"• {d.get('title', 'Document')}: {d.get('text', '')[:200]}..." for d in context["contract_docs"]
            ))
        
        if context.get("guides"):
            response_parts.append("**Guides:**\n" + "\n".join(
                f"• {d.get('title', 'Guide')}: {d.get('text', '')[:200]}..." for d in context["guides"]
            ))
        
        return "\n\n".join(response_parts) if response_parts else "I found some information but couldn't format it properly. Please contact support."

