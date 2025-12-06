"""
Chat Service - Agentic RAG Implementation.

Uses LLM-based intent classification and routing for multi-domain chat.
Architecture:
  1. LLM Router (Gemini Flash) - Classifies intent, extracts filters
  2. Parallel Retrieval - Fetches from multiple sources if needed
  3. Real-time Data - Fetches live data from Spring Boot when needed
  4. LLM Generator (Gemini Pro) - Generates final response with context
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
from services.spring_boot_client import SpringBootClient

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
1. Determine PRIMARY intent(s):
   - If asking about a supplier → ONLY "supplier_info" (not product_search)
   - If asking about a product → ONLY "product_search" (not supplier_info)
   - Multiple intents only for combined queries (e.g., "Find laptops and tax info" = product_search + tax_question)

2. Extract filters mentioned:
   - supplier_id: Numeric ID when user says "supplier with id X", "supplier X", "supplier id X"
   - product_id: Numeric ID when user says "product with id X", "product X", "product id X"
   - sku: Product SKU when user mentions "sku X" (need to convert SKU to product_id)
   - category: Category name/slug (e.g., "electronics", "beauty", "serum")
   - price_min/price_max: Price range in dollars (e.g., "under $15", "price < 20")
   - check_inventory: true if asking about stock/availability/inventory

3. Set requires_realtime_data=true if ANY of these conditions apply:
   - User asks for SPECIFIC ID (e.g., "supplier with id 132", "product id 50") → ALWAYS TRUE
   - User mentions PRICE filters (e.g., "under $15", "price < 20", "cheap products")
   - User mentions CATEGORY filters (e.g., "serum products", "laptops in electronics category")
   - User asks about INVENTORY/STOCK (e.g., "in stock", "availability", "how many available")
   - User asks about live data (order status, user account info)

Examples:
- "find the supplier with id 132" → {"intents": ["supplier_info"], "product_filters": {"supplier_id": 132}, "requires_realtime_data": true}
- "show me product 50" → {"intents": ["product_search"], "product_filters": {"product_id": 50}, "requires_realtime_data": true}
- "serum under $15" → {"intents": ["product_search"], "product_filters": {"category": "serum", "price_max": 15}, "requires_realtime_data": true}
- "find serum product with price < 2 dollar" → {"intents": ["product_search"], "product_filters": {"category": "serum", "price_max": 2}, "requires_realtime_data": true}
- "laptops over $500" → {"intents": ["product_search"], "product_filters": {"category": "laptops", "price_min": 500}, "requires_realtime_data": true}
- "check inventory status of sku 435835263" → {"intents": ["product_search"], "product_filters": {"sku": "435835263", "check_inventory": true}, "requires_realtime_data": true}

Respond ONLY with valid JSON in this exact format:
{
  "intents": ["intent1", "intent2"],
  "product_filters": {"category": "beauty", "price_max": 15, "price_min": 10, "product_id": 123, "supplier_id": 132, "check_inventory": true} or null,
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
            self.router_model = genai.GenerativeModel('gemini-2.0-flash')
            self.generator_model = genai.GenerativeModel('gemini-2.0-flash')  # Can upgrade to gemini-2.5-pro
            self.llm_available = True
            logger.info("✅ Chat service initialized with Gemini (Agentic RAG mode)")
            logger.info(f"   API Key configured: {settings.google_api_key[:20]}...")
        else:
            self.router_model = None
            self.generator_model = None
            self.llm_available = False
            logger.warning("⚠️  NO LLM API KEY CONFIGURED - using fallback keyword classification")
            logger.warning("   Set GOOGLE_API_KEY environment variable to enable LLM Router")
        
        # Initialize Spring Boot client for real-time data
        self.spring_boot_client = SpringBootClient()
        logger.info("Spring Boot client initialized for real-time data fetch")
    
    async def classify_query_with_llm(self, query: str) -> QueryIntent:
        """
        Use LLM to classify query intent and extract filters.
        
        This is the "Agent" part of Agentic RAG - the LLM decides:
        - What sources to query
        - What filters to apply
        - Whether real-time data is needed
        """
        if not self.llm_available:
            logger.info("🔄 Using FALLBACK classification (LLM not available)")
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
            
            logger.info(f"🤖 LLM Router classified: intents={intent.intents}, filters={intent.product_filters}, confidence={intent.confidence}")
            return intent
            
        except json.JSONDecodeError as e:
            logger.warning(f"⚠️  Failed to parse LLM router response: {e}")
            logger.warning(f"   Response text: {response.text if 'response' in locals() else 'N/A'}")
            logger.info("🔄 Falling back to keyword classification")
            return self._fallback_classification(query)
        except Exception as e:
            logger.error(f"❌ LLM router failed: {e}")
            logger.info("🔄 Falling back to keyword classification")
            return self._fallback_classification(query)
    
    def _fallback_classification(self, query: str) -> QueryIntent:
        """Fallback to keyword-based classification when LLM is unavailable."""
        logger.info(f"📝 Fallback classification for: '{query}'")
        query_lower = query.lower()
        intents = []
        filters = {}
        
        # Extract IDs and filters using regex
        import re
        supplier_id_match = re.search(r'supplier\s+(?:with\s+)?id\s+(\d+)', query_lower)
        product_id_match = re.search(r'product\s+(?:with\s+)?id\s+(\d+)', query_lower)
        
        # Extract SKU - patterns: "sku 12345", "sku: 12345", "of sku 12345"
        sku_match = re.search(r'sku[:\s]+(\d+)', query_lower)
        
        # Check for inventory/stock keywords
        inventory_check = any(kw in query_lower for kw in ['inventory', 'stock', 'availability', 'available', 'in stock', 'how many'])
        
        # Extract price filters
        # Patterns: "price < 2", "under $15", "less than 20 dollars", "below 10"
        price_max_match = re.search(r'(?:price|cost)\s*[<]\s*(\d+(?:\.\d+)?)|(?:under|below|less than)\s*(?:\$|usd)?\s*(\d+(?:\.\d+)?)', query_lower)
        # Patterns: "price > 100", "over $50", "more than 20 dollars", "above 30"
        price_min_match = re.search(r'(?:price|cost)\s*[>]\s*(\d+(?:\.\d+)?)|(?:over|above|more than)\s*(?:\$|usd)?\s*(\d+(?:\.\d+)?)', query_lower)
        
        # Extract category from common patterns
        category_match = re.search(r'(\w+)\s+product', query_lower)
        
        # Keyword matching as fallback (order matters - check specific ones first)
        if any(kw in query_lower for kw in ['supplier', 'vendor', 'seller', 'who sells']):
            intents.append("supplier_info")
            if supplier_id_match:
                filters["supplier_id"] = int(supplier_id_match.group(1))
        elif any(kw in query_lower for kw in ['tax', 'duty', 'import', 'export', 'regulation']):
            intents.append("tax_question")
        elif any(kw in query_lower for kw in ['contract', 'agreement', 'legal', 'template']):
            intents.append("contract_help")
        elif any(kw in query_lower for kw in ['how to', 'how do i', 'help me', 'guide']):
            intents.append("platform_help")
        elif any(kw in query_lower for kw in ['inventory', 'stock', 'availability', 'sku', 'product', 'buy', 'purchase', 'price', 'find', 'search', 'looking for']):
            intents.append("product_search")
            
            # Add SKU if detected
            if sku_match:
                filters["sku"] = sku_match.group(1)
            
            # Add product ID if detected
            if product_id_match:
                filters["product_id"] = int(product_id_match.group(1))
            
            # Add inventory check flag
            if inventory_check:
                filters["check_inventory"] = True
            
            # Add price filters if detected
            if price_max_match:
                price_val = price_max_match.group(1) or price_max_match.group(2)
                filters["price_max"] = float(price_val)
            if price_min_match:
                price_val = price_min_match.group(1) or price_min_match.group(2)
                filters["price_min"] = float(price_val)
            
            # Add category if detected
            if category_match:
                filters["category"] = category_match.group(1)
        
        if not intents:
            intents = ["general"]
        
        # Set requires_realtime_data if IDs or filters are present
        requires_realtime = bool(
            supplier_id_match or 
            product_id_match or 
            sku_match or 
            inventory_check or 
            price_max_match or 
            price_min_match or 
            category_match
        )
        
        logger.info(f"   → Extracted: intents={intents}, filters={filters}, realtime={requires_realtime}")
            
        return QueryIntent(
            intents=intents,
            product_filters=filters if filters else None,
            requires_realtime_data=requires_realtime,
            confidence=0.6
        )
    
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
            tasks["products"] = self._search_products(query_embedding, intent.product_filters, query)
            
        if "tax_question" in intent.intents:
            filters = intent.knowledge_filters or {}
            filters["doc_type"] = "tax"
            tasks["tax_docs"] = self._search_knowledge(query_embedding, filters)
            
        if "contract_help" in intent.intents:
            filters = intent.knowledge_filters or {}
            filters["doc_type"] = "contract"
            tasks["contract_docs"] = self._search_knowledge(query_embedding, filters)
            
        if "supplier_info" in intent.intents:
            tasks["suppliers"] = self._search_suppliers(query_embedding, intent.product_filters)
            
        if "platform_help" in intent.intents:
            filters = {"doc_type": "guide"}
            tasks["guides"] = self._search_knowledge(query_embedding, filters)
        
        # Execute all retrieval tasks in parallel
        context = {}
        if tasks:
            results = await asyncio.gather(*tasks.values(), return_exceptions=True)
            for key, result in zip(tasks.keys(), results):
                if isinstance(result, Exception):
                    logger.error(f"Retrieval failed for {key}: {result}")
                    context[key] = []
                else:
                    context[key] = result
        
        # Fetch real-time data from Spring Boot if needed
        if intent.requires_realtime_data:
            realtime_data = await self._fetch_realtime_data(context, intent)
            context["realtime_data"] = realtime_data
        
        return context
    
    async def _fetch_realtime_data(
        self,
        context: Dict[str, List[Dict[str, Any]]],
        intent: QueryIntent
    ) -> Dict[str, Any]:
        """
        Fetch real-time data from Spring Boot backend.
        
        This is called when the LLM router determines that live data is needed
        (e.g., inventory levels, current prices, order status).
        """
        realtime_data = {}
        
        try:
            # Get product IDs from vector search results
            product_ids = []
            if "products" in context:
                for p in context["products"]:
                    pid = p.get("product_id")
                    if pid:
                        product_ids.append(int(pid))
            
            # Fetch real-time product details (includes latest prices)
            if product_ids:
                products = await self.spring_boot_client.get_products_by_ids(product_ids[:10])
                if products:
                    realtime_data["products"] = [
                        {
                            "id": p.id,
                            "name": p.name,
                            "sku": p.sku,
                            "base_price": p.base_price,
                            "minimum_order_quantity": p.minimum_order_quantity,
                            "supplier_id": p.supplier_id
                        }
                        for p in products
                    ]
                    
                    # Fetch inventory status for these products
                    inventory_data = []
                    for pid in product_ids[:5]:  # Limit to 5 to avoid too many calls
                        inv = await self.spring_boot_client.get_inventory_status(pid)
                        if inv:
                            inventory_data.append({
                                "product_id": inv.product_id,
                                "available_quantity": inv.available_quantity,
                                "in_stock": inv.in_stock,
                                "reserved_quantity": inv.reserved_quantity
                            })
                    if inventory_data:
                        realtime_data["inventory"] = inventory_data
                    
                    # Fetch supplier info for relevant suppliers
                    supplier_ids = set(p.supplier_id for p in products if p.supplier_id)
                    supplier_data = []
                    for sid in list(supplier_ids)[:3]:  # Limit to 3 suppliers
                        supplier = await self.spring_boot_client.get_supplier_info(sid)
                        if supplier:
                            supplier_data.append({
                                "id": supplier.id,
                                "name": supplier.name,
                                "rating": supplier.rating,
                                "verified": supplier.verified
                            })
                    if supplier_data:
                        realtime_data["suppliers"] = supplier_data
            
            # If check_inventory is requested, get inventory details for context products
            if intent.product_filters and intent.product_filters.get("check_inventory"):
                if "products" in context:
                    inventory_details = []
                    for p in context["products"][:5]:  # Limit to first 5
                        pid = p.get("product_id") or p.get("id")
                        if pid:
                            inv = await self.spring_boot_client.get_inventory_status(pid)
                            if inv:
                                inventory_details.append({
                                    "product_id": inv.product_id,
                                    "product_name": p.get("name"),
                                    "available_quantity": inv.available_quantity,
                                    "in_stock": inv.in_stock,
                                    "reserved_quantity": inv.reserved_quantity
                                })
                    if inventory_details:
                        realtime_data["inventory_details"] = inventory_details
            
            # If filters specify a category or supplier, search for more products
            if intent.product_filters:
                category = intent.product_filters.get("category")
                supplier_id = intent.product_filters.get("supplier_id")
                price_min = intent.product_filters.get("price_min")
                price_max = intent.product_filters.get("price_max")
                
                if category or supplier_id or price_min or price_max:
                    search_results = await self.spring_boot_client.search_products(
                        category=category,
                        supplier_id=supplier_id,
                        min_price=price_min,
                        max_price=price_max,
                        limit=5
                    )
                    if search_results:
                        realtime_data["search_results"] = [
                            {
                                "id": p.id,
                                "name": p.name,
                                "sku": p.sku,
                                "base_price": p.base_price,
                                "in_stock": p.in_stock
                            }
                            for p in search_results
                        ]
            
            logger.info(f"Fetched real-time data: {list(realtime_data.keys())}")
            
        except Exception as e:
            logger.error(f"Failed to fetch real-time data: {e}")
        
        return realtime_data
    
    async def _search_products(
        self,
        query_embedding: List[float],
        filters: Optional[Dict[str, Any]] = None,
        query_text: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        """Search product catalog."""
        try:
            logger.info(f"_search_products called with filters: {filters}, query: {query_text}")
            
            # If SKU is provided, search by SKU using Internal API
            if filters and "sku" in filters:
                sku = filters["sku"]
                logger.info(f"Searching for product with SKU {sku} via Internal API")
                search_results = await self.spring_boot_client.search_products(
                    query=sku,  # Search by SKU
                    limit=1
                )
                if search_results and len(search_results) > 0:
                    p = search_results[0]
                    return [{
                        "id": p.id,
                        "product_id": p.id,
                        "name": p.name,
                        "sku": p.sku,
                        "description": p.description,
                        "base_price": p.base_price,
                        "supplier_id": p.supplier_id,
                        "in_stock": p.in_stock,
                        "score": 1.0  # Perfect match since it's by SKU
                    }]
                else:
                    logger.warning(f"Product with SKU {sku} not found in database")
                    return []
            
            # If a specific product_id is requested, fetch from Internal API by ID
            if filters and "product_id" in filters:
                product_id = filters["product_id"]
                logger.info(f"Fetching specific product {product_id} from Internal API")
                products = await self.spring_boot_client.get_products_by_ids([product_id])
                if products and len(products) > 0:
                    p = products[0]
                    return [{
                        "id": p.id,
                        "product_id": p.id,
                        "name": p.name,
                        "sku": p.sku,
                        "description": p.description,
                        "base_price": p.base_price,
                        "supplier_id": p.supplier_id,
                        "categories": [cat.get("name", "") for cat in p.categories],
                        "score": 1.0  # Perfect match since it's by ID
                    }]
                else:
                    logger.warning(f"Product {product_id} not found in database")
                    return []
            
            # If price filters, category filters, or supplier_id exists, use Internal API search
            # This allows database-level filtering which is more accurate than vector search
            if filters and any(key in filters for key in ["price_min", "price_max", "category", "supplier_id"]):
                logger.info(f"Using Internal API search with filters: {filters}")
                search_results = await self.spring_boot_client.search_products(
                    query=query_text,
                    category=filters.get("category"),
                    supplier_id=filters.get("supplier_id"),
                    min_price=filters.get("price_min"),
                    max_price=filters.get("price_max"),
                    limit=10
                )
                
                if search_results:
                    return [{
                        "id": p.id,
                        "product_id": p.id,
                        "name": p.name,
                        "sku": p.sku,
                        "description": p.description,
                        "base_price": p.base_price,
                        "supplier_id": p.supplier_id,
                        "in_stock": p.in_stock,
                        "score": 0.95  # High score since it matches filters
                    } for p in search_results]
                else:
                    logger.info("No products found with filters in Internal API")
                    return []
            
            # Otherwise, use vector search for semantic similarity
            # Convert intent filters to Qdrant filters for vector search
            qdrant_filters = {}
            if filters:
                if "category" in filters:
                    qdrant_filters["category"] = filters["category"]
                if "supplier_id" in filters:
                    qdrant_filters["supplier_id"] = filters["supplier_id"]
            
            results = await self.qdrant_service.search_products(
                query_vector=query_embedding,
                limit=5,
                filters=qdrant_filters if qdrant_filters else None,
                score_threshold=0.7  # Only return products with >70% similarity
            )
            logger.info(f"Vector search returned {len(results)} products above threshold")
            return results
        except Exception as e:
            logger.error(f"Product search failed: {e}")
            return []
    
    async def _search_suppliers(
        self,
        query_embedding: List[float],
        filters: Optional[Dict[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        """Search supplier catalog."""
        try:
            # If a specific supplier_id is requested, fetch from Internal API
            if filters and "supplier_id" in filters:
                supplier_id = filters["supplier_id"]
                logger.info(f"Fetching specific supplier {supplier_id} from Internal API")
                supplier = await self.spring_boot_client.get_supplier_info(supplier_id)
                if supplier:
                    return [{
                        "id": supplier.id,
                        "supplier_id": supplier.id,
                        "name": supplier.name,
                        "email": supplier.email,
                        "phone": supplier.phone,
                        "address": supplier.address,
                        "profile_description": supplier.profile_description,
                        "rating": supplier.rating,
                        "verified": supplier.verified,
                        "score": 1.0  # Perfect match since it's by ID
                    }]
                else:
                    logger.warning(f"Supplier {supplier_id} not found in database")
                    return []
            
            # Otherwise, do vector search
            results = await self.qdrant_service.search_suppliers(
                query_vector=query_embedding,
                limit=5,
                filters=None,
                score_threshold=0.7  # Only return suppliers with >70% similarity
            )
            logger.info(f"Vector search returned {len(results)} suppliers above threshold")
            return results
        except Exception as e:
            logger.error(f"Supplier search failed: {e}")
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
                doc_type=qdrant_filters.get("doc_type") if qdrant_filters else None,
                region=qdrant_filters.get("region") if qdrant_filters else None,
                score_threshold=0.6  # Lower threshold for knowledge base (more permissive)
            )
            logger.info(f"Knowledge base search returned {len(results)} results above threshold")
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
        
        # Determine primary query_type from intents for API response
        query_type = intent.intents[0] if intent.intents else "general"
        
        return {
            "response": response_text,
            "sources": sources,
            "query_type": query_type,
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
            for s in context["suppliers"]:
                # Handle both supplier_id and id fields
                sid = s.get("supplier_id") or s.get("id")
                if sid and sid not in seen_suppliers:
                    sources.append({
                        "type": "supplier",
                        "supplier_id": sid,
                        "name": s.get("name"),
                        "email": s.get("email"),
                        "phone": s.get("phone"),
                        "address": s.get("address"),
                        "rating": s.get("rating"),
                        "verified": s.get("verified")
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
        
        if context.get("suppliers"):
            supplier_text = "### Suppliers Found:\n"
            for s in context["suppliers"]:
                supplier_text += f"- {s.get('name', 'Unknown')} (ID: {s.get('supplier_id') or s.get('id')})\n"
                supplier_text += f"  Email: {s.get('email', 'N/A')}, Phone: {s.get('phone', 'N/A')}\n"
                if s.get('address'):
                    supplier_text += f"  Address: {s.get('address')}\n"
                if s.get('profile_description'):
                    supplier_text += f"  Description: {s.get('profile_description')[:150]}\n"
                if s.get('rating') is not None:
                    supplier_text += f"  Rating: {s.get('rating')}/5.0, Verified: {s.get('verified', False)}\n"
            sections.append(supplier_text)
        
        if context.get("realtime_data", {}).get("inventory_details"):
            inventory_text = "### Inventory Status:\n"
            for inv in context["realtime_data"]["inventory_details"]:
                inventory_text += f"- {inv.get('product_name', 'Product')} (ID: {inv.get('product_id')})\n"
                inventory_text += f"  Available: {inv.get('available_quantity')} units\n"
                inventory_text += f"  In Stock: {'Yes' if inv.get('in_stock') else 'No'}\n"
                inventory_text += f"  Reserved: {inv.get('reserved_quantity')} units\n"
            sections.append(inventory_text)
        
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
        
        # Include real-time data from Spring Boot
        if context.get("realtime_data"):
            rt = context["realtime_data"]
            
            if rt.get("products"):
                rt_product_text = "### Real-Time Product Data (Live from Database):\n"
                for p in rt["products"]:
                    rt_product_text += f"- {p.get('name')} (SKU: {p.get('sku')}): Price ${p.get('base_price', 'N/A')}, MOQ: {p.get('minimum_order_quantity', 1)}\n"
                sections.append(rt_product_text)
            
            if rt.get("inventory"):
                inv_text = "### Current Inventory Status:\n"
                for inv in rt["inventory"]:
                    status = "In Stock" if inv.get("in_stock") else "Out of Stock"
                    inv_text += f"- Product {inv.get('product_id')}: {inv.get('available_quantity', 0)} available ({status})\n"
                sections.append(inv_text)
            
            if rt.get("suppliers"):
                sup_text = "### Verified Supplier Details:\n"
                for s in rt["suppliers"]:
                    verified = "✓ Verified" if s.get("verified") else "Unverified"
                    rating = s.get("rating", 0)
                    sup_text += f"- {s.get('name')}: Rating {rating:.1f}/5 ({verified})\n"
                sections.append(sup_text)
            
            if rt.get("search_results"):
                search_text = "### Additional Products Found:\n"
                for p in rt["search_results"]:
                    stock = "In Stock" if p.get("in_stock") else "Out of Stock"
                    search_text += f"- {p.get('name')} (SKU: {p.get('sku')}): ${p.get('base_price', 'N/A')} - {stock}\n"
                sections.append(search_text)
        
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
            
            # Special handling for general conversational queries (greetings, small talk)
            if "general" in intent.intents and not any(context.values()):
                # No context retrieved - handle as natural conversation
                full_prompt = f"""{system_prompt}

### Conversation History:
{history_text}

### Current Message:
User: {query}

### Instructions:
This is a general conversational message (greeting or small talk). Respond naturally and warmly.
- If it's a greeting, welcome them and offer to help with products, orders, tax questions, or platform guidance
- If it's a question in general, respond politely but indicate you are here to assist with the e-commerce platform
- Be friendly and professional
- Keep it brief and conversational
- Don't apologize for lack of information - this is normal conversation

### Response:"""
            else:
                # Normal query with context or specific intent
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
4. Be concise but thorough, and be warm with user. Do not use "**" for header display.
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
        
        # Handle general conversational queries (greetings, small talk)
        if "general" in intent.intents and not any(context.values()):
            greetings = [
                "Hello! Welcome to our e-commerce platform. How can I assist you today? I can help you find products, answer questions about orders, explain tax policies, or guide you through our platform features.",
                "Hi there! I'm here to help you with product searches, order information, tax questions, and platform guidance. What can I do for you?",
                "Greetings! Feel free to ask me about products, orders, taxes, supplier information, or any platform features you need help with.",
                "Hey! Welcome! I'm your AI assistant for this marketplace. I can help you discover products, understand policies, or answer any questions you have."
            ]
            # Simple hash-based selection for consistency
            index = sum(ord(c) for c in query) % len(greetings)
            return greetings[index]
        
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
        
        if context.get("suppliers"):
            supplier_lines = []
            for s in context["suppliers"]:
                line = f"• {s.get('name', 'Unknown')} (ID: {s.get('supplier_id') or s.get('id')})\n"
                line += f"  Email: {s.get('email', 'N/A')}\n"
                line += f"  Phone: {s.get('phone', 'N/A')}\n"
                line += f"  Address: {s.get('address', 'N/A')}\n"
                if s.get('profile_description'):
                    line += f"  Description: {s.get('profile_description')[:150]}\n"
                if s.get('rating') is not None:
                    line += f"  Rating: {s.get('rating')}/5.0, Verified: {'Yes' if s.get('verified') else 'No'}"
                supplier_lines.append(line)
            response_parts.append("**Suppliers Found:**\n" + "\n".join(supplier_lines))
        
        return "\n\n".join(response_parts) if response_parts else "I found some information but couldn't format it properly. Please contact support."

