# Vendora AI Service

AI-powered chatbot and recommendation service for the Vendora B2B E-Commerce platform.

## Features

- **Chatbot**: RAG-based conversational AI for product search, tax/contract queries, and platform help
- **Recommendations**: User-based and item-to-item product recommendations using vector similarity
- **Knowledge Base**: Stores and retrieves documents (tax guides, contracts, platform documentation)

## Architecture

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   Frontend      │──────▶│  Spring Boot    │──────▶│  Python AI      │
│   (React/Vue)   │       │  (Main Backend) │       │  (This Service) │
└─────────────────┘       └─────────────────┘       └────────┬────────┘
                                                             │
                                                             ▼
                                                    ┌─────────────────┐
                                                    │     Qdrant      │
                                                    │  (Vector DB)    │
                                                    └─────────────────┘
```

## Tech Stack

- **FastAPI** - Web framework
- **Qdrant** - Vector database
- **Sentence Transformers** - Text embeddings
- **Google Gemini / OpenAI** - LLM for chat generation

## Quick Start

### Using Docker (Recommended)

```bash
# From the project root directory
docker-compose up -d

# The AI service will be available at http://localhost:8000
```

### Local Development

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Copy environment file and configure
cp .env.example .env
# Edit .env with your API keys

# Start Qdrant (requires Docker)
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant

# Run the service
uvicorn main:app --reload --port 8000
```

## API Endpoints

### Health Check
- `GET /health` - Service health status

### Ingestion
- `POST /ai/ingest/product` - Ingest a single product
- `POST /ai/ingest/products/bulk` - Bulk ingest products
- `POST /ai/ingest/document` - Ingest a document (tax, contract, guide)
- `DELETE /ai/ingest/product/{sku}` - Remove a product

### Chat
- `POST /ai/chat/generate` - Generate chat response
- `POST /ai/chat/classify` - Classify query type

### Recommendations
- `POST /ai/recommend/analytics/track` - Track user interaction
- `GET /ai/recommend/user/{user_id}` - Get personalized recommendations
- `GET /ai/recommend/similar/{product_id}` - Get similar products
- `GET /ai/recommend/homepage/{user_id}` - Get homepage recommendations

## Configuration

Environment variables (set in `.env` file):

| Variable | Description | Default |
|----------|-------------|---------|
| `QDRANT_HOST` | Qdrant server host | `localhost` |
| `QDRANT_PORT` | Qdrant server port | `6333` |
| `GOOGLE_API_KEY` | Google Gemini API key | - |
| `OPENAI_API_KEY` | OpenAI API key (alternative) | - |
| `SPRING_BOOT_URL` | Spring Boot backend URL | `http://localhost:8080` |
| `EMBEDDING_MODEL` | Sentence transformer model | `all-MiniLM-L6-v2` |

## Collections

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
