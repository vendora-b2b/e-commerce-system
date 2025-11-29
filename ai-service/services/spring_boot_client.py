"""
Spring Boot Client - HTTP client for calling Spring Boot backend APIs.

This service enables the Python AI service to fetch real-time data from
the Spring Boot backend (products, suppliers, inventory) for RAG context.
"""

import logging
from typing import List, Dict, Any, Optional
from dataclasses import dataclass

import httpx

from config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


@dataclass
class ProductDetails:
    """Product information from Spring Boot."""
    id: int
    sku: str
    name: str
    description: str
    categories: List[Dict[str, Any]]
    supplier_id: int
    base_price: float
    minimum_order_quantity: int
    unit: str
    images: List[str]
    colors: List[str]
    sizes: List[str]
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "ProductDetails":
        """Create ProductDetails from API response dict."""
        return cls(
            id=data.get("id"),
            sku=data.get("sku", ""),
            name=data.get("name", ""),
            description=data.get("description", ""),
            categories=data.get("categories", []),
            supplier_id=data.get("supplierId"),
            base_price=data.get("basePrice", 0.0),
            minimum_order_quantity=data.get("minimumOrderQuantity", 1),
            unit=data.get("unit", "piece"),
            images=data.get("images", []),
            colors=data.get("colors", []),
            sizes=data.get("sizes", [])
        )


@dataclass
class SupplierInfo:
    """Supplier information from Spring Boot."""
    id: int
    name: str
    email: str
    phone: str
    address: str
    profile_description: str
    rating: float
    verified: bool
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "SupplierInfo":
        """Create SupplierInfo from API response dict."""
        return cls(
            id=data.get("id"),
            name=data.get("name", ""),
            email=data.get("email", ""),
            phone=data.get("phone", ""),
            address=data.get("address", ""),
            profile_description=data.get("profileDescription", ""),
            rating=data.get("rating", 0.0),
            verified=data.get("verified", False)
        )


@dataclass
class InventoryStatus:
    """Inventory status from Spring Boot."""
    product_id: int
    variant_id: Optional[int]
    available_quantity: int
    reserved_quantity: int
    reorder_point: int
    in_stock: bool
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "InventoryStatus":
        """Create InventoryStatus from API response dict."""
        return cls(
            product_id=data.get("productId"),
            variant_id=data.get("variantId"),
            available_quantity=data.get("availableQuantity", 0),
            reserved_quantity=data.get("reservedQuantity", 0),
            reorder_point=data.get("reorderPoint", 0),
            in_stock=data.get("inStock", False)
        )


@dataclass
class ProductSearchResult:
    """Search result from Spring Boot product search."""
    id: int
    sku: str
    name: str
    description: str
    category: str
    supplier_id: int
    base_price: float
    in_stock: bool
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "ProductSearchResult":
        """Create ProductSearchResult from API response dict."""
        categories = data.get("categories", [])
        category_name = categories[0].get("name", "") if categories else ""
        return cls(
            id=data.get("id"),
            sku=data.get("sku", ""),
            name=data.get("name", ""),
            description=data.get("description", ""),
            category=category_name,
            supplier_id=data.get("supplierId"),
            base_price=data.get("basePrice", 0.0),
            in_stock=data.get("inStock", True)
        )


class SpringBootClient:
    """
    HTTP client for calling Spring Boot backend APIs.
    
    This client provides methods to fetch real-time data from the
    Spring Boot backend for use in RAG context enrichment.
    """
    
    def __init__(self):
        """Initialize the Spring Boot client."""
        self.base_url = settings.spring_boot_url
        self.timeout = httpx.Timeout(10.0, connect=5.0)
        logger.info(f"Spring Boot client initialized with URL: {self.base_url}")
    
    async def get_product_details(self, product_id: int) -> Optional[ProductDetails]:
        """
        Get full product details by ID.
        
        Calls: GET /internal/ai/products/{productId}
        
        Args:
            product_id: The product ID
            
        Returns:
            ProductDetails if found, None otherwise
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/internal/ai/products/{product_id}"
                )
                
                if response.status_code == 200:
                    data = response.json()
                    return ProductDetails.from_dict(data)
                elif response.status_code == 404:
                    logger.warning(f"Product not found: {product_id}")
                    return None
                else:
                    logger.error(f"Failed to get product {product_id}: {response.status_code}")
                    return None
                    
        except httpx.RequestError as e:
            logger.error(f"Request error getting product {product_id}: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error getting product {product_id}: {e}")
            return None
    
    async def search_products(
        self,
        query: Optional[str] = None,
        category: Optional[str] = None,
        supplier_id: Optional[int] = None,
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        limit: int = 10
    ) -> List[ProductSearchResult]:
        """
        Search products with filters.
        
        Calls: GET /internal/ai/products/search
        
        Args:
            query: Text search query (searches name, description)
            category: Filter by category slug
            supplier_id: Filter by supplier ID
            min_price: Minimum price filter
            max_price: Maximum price filter
            limit: Maximum number of results
            
        Returns:
            List of matching products
        """
        try:
            params = {"limit": limit}
            if query:
                params["query"] = query
            if category:
                params["category"] = category
            if supplier_id:
                params["supplierId"] = supplier_id
            if min_price is not None:
                params["minPrice"] = min_price
            if max_price is not None:
                params["maxPrice"] = max_price
            
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/internal/ai/products/search",
                    params=params
                )
                
                if response.status_code == 200:
                    data = response.json()
                    products = data.get("products", [])
                    return [ProductSearchResult.from_dict(p) for p in products]
                else:
                    logger.error(f"Product search failed: {response.status_code}")
                    return []
                    
        except httpx.RequestError as e:
            logger.error(f"Request error searching products: {e}")
            return []
        except Exception as e:
            logger.error(f"Unexpected error searching products: {e}")
            return []
    
    async def get_supplier_info(self, supplier_id: int) -> Optional[SupplierInfo]:
        """
        Get supplier information by ID.
        
        Calls: GET /internal/ai/suppliers/{supplierId}
        
        Args:
            supplier_id: The supplier ID
            
        Returns:
            SupplierInfo if found, None otherwise
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/internal/ai/suppliers/{supplier_id}"
                )
                
                if response.status_code == 200:
                    data = response.json()
                    return SupplierInfo.from_dict(data)
                elif response.status_code == 404:
                    logger.warning(f"Supplier not found: {supplier_id}")
                    return None
                else:
                    logger.error(f"Failed to get supplier {supplier_id}: {response.status_code}")
                    return None
                    
        except httpx.RequestError as e:
            logger.error(f"Request error getting supplier {supplier_id}: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error getting supplier {supplier_id}: {e}")
            return None
    
    async def get_inventory_status(
        self,
        product_id: int,
        variant_id: Optional[int] = None
    ) -> Optional[InventoryStatus]:
        """
        Get inventory status for a product or variant.
        
        Calls: GET /internal/ai/inventory/{productId}
        
        Args:
            product_id: The product ID
            variant_id: Optional variant ID for specific variant inventory
            
        Returns:
            InventoryStatus if found, None otherwise
        """
        try:
            params = {}
            if variant_id:
                params["variantId"] = variant_id
            
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/internal/ai/inventory/{product_id}",
                    params=params
                )
                
                if response.status_code == 200:
                    data = response.json()
                    return InventoryStatus.from_dict(data)
                elif response.status_code == 404:
                    logger.warning(f"Inventory not found for product: {product_id}")
                    return None
                else:
                    logger.error(f"Failed to get inventory for {product_id}: {response.status_code}")
                    return None
                    
        except httpx.RequestError as e:
            logger.error(f"Request error getting inventory {product_id}: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error getting inventory {product_id}: {e}")
            return None
    
    async def get_products_by_ids(self, product_ids: List[int]) -> List[ProductDetails]:
        """
        Get multiple products by their IDs.
        
        Calls: POST /internal/ai/products/batch
        
        Args:
            product_ids: List of product IDs to fetch
            
        Returns:
            List of ProductDetails for found products
        """
        if not product_ids:
            return []
            
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}/internal/ai/products/batch",
                    json={"productIds": product_ids}
                )
                
                if response.status_code == 200:
                    data = response.json()
                    products = data.get("products", [])
                    return [ProductDetails.from_dict(p) for p in products]
                else:
                    logger.error(f"Batch product fetch failed: {response.status_code}")
                    return []
                    
        except httpx.RequestError as e:
            logger.error(f"Request error in batch product fetch: {e}")
            return []
        except Exception as e:
            logger.error(f"Unexpected error in batch product fetch: {e}")
            return []
    
    async def health_check(self) -> bool:
        """
        Check if Spring Boot backend is healthy.
        
        Returns:
            True if backend is healthy, False otherwise
        """
        try:
            async with httpx.AsyncClient(timeout=httpx.Timeout(5.0)) as client:
                response = await client.get(f"{self.base_url}/actuator/health")
                return response.status_code == 200
        except Exception as e:
            logger.warning(f"Spring Boot health check failed: {e}")
            return False
