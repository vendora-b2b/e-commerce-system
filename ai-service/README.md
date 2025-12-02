# Vendora AI Service

AI-powered chatbot and recommendation service for the Vendora B2B E-Commerce platform.

## Features

- **Agentic RAG Chatbot**: Multi-strategy retrieval with LLM routing for product search, tax/contract queries, and platform help
- **Recommendations**: User-based and item-to-item product recommendations using vector similarity
- **Knowledge Base**: Stores and retrieves documents (tax guides, contracts, platform documentation)
- **Real-time Integration**: Fetches live data from Spring Boot backend for accurate responses

## Architecture

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   Frontend      │──────▶│  Spring Boot    │──────▶│  Python AI      │
│   (React/Vue)   │       │  (Main Backend) │       │  (This Service) │
└─────────────────┘       └─────────────────┘       └────────┬────────┘
                                 │                           │
                                 │                           ▼
                                 │                  ┌─────────────────┐
                                 │◀─────────────────│     Qdrant      │
                                 │  (Internal API)  │  (Vector DB)    │
                                 │                  └─────────────────┘
```

### Agentic RAG Flow
```
User Query → LLM Router → Parallel Retrieval → Context Fusion → LLM Generator → Response
                │
                ├── product_search → Vector DB + Spring Boot API
                ├── knowledge → Knowledge Base Collection
                └── general → Direct LLM Response
```

## Tech Stack

- **FastAPI** - Web framework
- **Qdrant** - Vector database for semantic search
- **Sentence Transformers** - Text embeddings (all-MiniLM-L6-v2)
- **Google Gemini** - LLM for chat generation (primary)
- **OpenAI GPT** - LLM fallback option

## Quick Start

### Using Docker (Recommended)

```bash
# From the project root directory
docker-compose up -d

# Services started:
# - MySQL: localhost:3306
# - Qdrant: localhost:6333
# - AI Service: localhost:8000
# - Spring Boot: localhost:8080 (run separately)
```

### Local Development

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Set environment variables
export QDRANT_HOST=localhost
export QDRANT_PORT=6333
export SPRING_BOOT_URL=http://localhost:8080
export GOOGLE_API_KEY=your_key_here

# Start Qdrant (requires Docker)
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant

# Run the service
uvicorn main:app --reload --port 8000
```

## API Endpoints

### Health Check
- `GET /health` - Service health status

### Ingestion (Called by Spring Boot)
- `POST /ai/ingest/product` - Ingest a single product
- `POST /ai/ingest/products/bulk` - Bulk ingest products
- `POST /ai/ingest/document` - Ingest a document (tax, contract, guide)
- `DELETE /ai/ingest/product/{sku}` - Remove a product from vector DB

### Chat (Called by Spring Boot)
- `POST /ai/chat/generate` - Generate chat response using Agentic RAG
- `POST /ai/chat/classify` - Classify query type

### Recommendations (Called by Spring Boot)
- `POST /ai/recommend/analytics/track` - Track user interaction
- `GET /ai/recommend/user/{user_id}` - Get personalized recommendations
- `GET /ai/recommend/similar/{product_id}` - Get similar products
- `GET /ai/recommend/homepage` - Get homepage recommendations

## Integration with Spring Boot

### Channel A: Frontend ↔ Spring Boot (Public API)
```
GET  /api/v1/chat/sessions                    - List user's chat sessions
POST /api/v1/chat/sessions                    - Create new chat session
GET  /api/v1/chat/sessions/{id}/messages      - Get messages in session
POST /api/v1/chat/sessions/{id}/messages      - Ask a question (triggers AI)

GET  /api/v1/products/{id}/recommendations    - Get similar products
GET  /api/v1/recommendations/homepage         - Get homepage recommendations
GET  /api/v1/recommendations/user             - Get user recommendations

POST /api/v1/analytics/track                  - Track user interaction
```

### Channel B: Spring Boot → AI Service (Internal API)
```
POST /ai/ingest/product       - Called on product create/update
POST /ai/ingest/document      - Called to add knowledge base docs
DELETE /ai/ingest/product/{sku} - Called on product delete

POST /ai/chat/generate        - Called by AskQuestionUseCase

POST /ai/recommend/analytics/track  - Forward interaction tracking
GET  /ai/recommend/user/{id}        - Get user recommendations
GET  /ai/recommend/similar/{id}     - Get similar products
```

### Channel C: AI Service → Spring Boot (Data Enrichment)
```
GET /internal/ai/products/{id}        - Get product details
GET /internal/ai/products/search      - Search products with filters
GET /internal/ai/suppliers/{id}       - Get supplier info
GET /internal/ai/inventory/{id}       - Get inventory status
```

## Configuration

Environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `QDRANT_HOST` | Qdrant server host | `localhost` |
| `QDRANT_PORT` | Qdrant server port | `6333` |
| `GOOGLE_API_KEY` | Google Gemini API key | - |
| `OPENAI_API_KEY` | OpenAI API key (fallback) | - |
| `SPRING_BOOT_URL` | Spring Boot backend URL | `http://localhost:8080` |
| `EMBEDDING_MODEL` | Sentence transformer model | `all-MiniLM-L6-v2` |

## Vector Collections

| Collection | Purpose | Data Source |
|------------|---------|-------------|
| `product_catalog` | Product embeddings | Spring Boot → Ingest API |
| `knowledge_base` | Tax, contracts, guides | Manual ingestion |
| `user_preferences` | User interaction vectors | Analytics tracking |

## Testing

```bash
# Test health
curl http://localhost:8000/health

# Test chat (via Spring Boot)
curl -X POST http://localhost:8080/api/v1/chat/sessions \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "Test"}'

# Test direct AI chat
curl -X POST http://localhost:8000/ai/chat/generate \
  -H "Content-Type: application/json" \
  -d '{"query": "What laptops do you have?", "history": []}'
```

The service uses three Qdrant collections:

1. **`product_catalog`** - Product embeddings with metadata (sku, name, description, supplier_id, category)
2. **`knowledge_base`** - Document embeddings (tax guides, contracts, platform help)
3. **`user_vectors`** - User preference vectors for recommendations

## Development

```bash
# Run tests
pytest

# Format code
black .

# Type checking
mypy .
```

## API Documentation

When running locally, access:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc
