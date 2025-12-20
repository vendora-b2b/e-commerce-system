# User Tracking & Vector Learning Flow

## Overview
This document explains how user interactions are tracked and how user preference vectors are built in Qdrant for personalized recommendations.

## Critical Prerequisites

### ⚠️ Products MUST be in Qdrant First!
Before any tracking can work, products must be ingested into Qdrant's `products` collection:
- When: During product creation or via bulk ingestion
- How: `POST /ai/ingest/product` or `POST /ai/ingest/products/bulk`
- Why: The system needs product vectors to update user vectors

**If products are not in Qdrant:**
- Tracking requests will fail silently
- User vectors will NOT be created/updated
- Recommendations will show cold start (default products)

---

## Tracking Flow

### 1. User Views a Product (Frontend → Spring Boot)

```
GET /api/v1/products/{id}?userId={userId}
```

**ProductController.java:**
```java
👁️ USER VIEW EVENT: user=3, product=15
```

**Flow:**
1. User clicks on product
2. Frontend calls API with userId parameter
3. ProductController creates `TrackUserInteractionCommand.view(userId, productId)`
4. Calls `TrackUserInteractionUseCase.execute(command)`

---

### 2. Spring Boot Processes Tracking (TrackUserInteractionUseCase)

**TrackUserInteractionUseCase.java:**
```
✅ Saved interaction to DB: user=3, product=15, type=VIEW, interactionId=42
🔄 Forwarding to AI service: user=3, product=15, type=VIEW
```

**Flow:**
1. Validates userId, productId, interactionType
2. Saves interaction to MySQL (`user_interactions` table)
3. Calls `forwardToAiServiceAsync(command)` - async, non-blocking
4. Returns success to frontend

---

### 3. Spring Boot → Python AI Service (AiServiceClient)

**AiServiceClient.java:**
```
🌐 HTTP POST to AI service: /ai/recommend/analytics/track - user=3, product=15, action=VIEW
✅ AI service response received: {success: true, message: "Interaction tracked successfully"}
```

**Flow:**
1. Creates `TrackInteractionRequest` (userId, productId, action)
2. WebClient POST to `http://localhost:8000/ai/recommend/analytics/track`
3. Waits for response (30s timeout)
4. Logs success or error

---

### 4. Python AI Service Receives Request (recommend.py)

**recommend.py:**
```
📥 RECEIVED TRACKING REQUEST: user=3, product=15, action=VIEW
✅ TRACKING SUCCESSFUL: user=3, product=15
```

**Flow:**
1. FastAPI endpoint receives POST request
2. Validates product identifier present
3. Calls `recommendation_service.track_interaction()`
4. Returns success response

---

### 5. Update User Vector (recommendation_service.py)

**recommendation_service.py:**
```
🔍 TRACKING INTERACTION: user=3, product=15, sku=None, action=VIEW
🔍 Looking up product vector: product_id=15
✅ Product vector found: dimension=384
🔍 Looking up user vector: user_id=3
🆕 COLD START: Initializing user vector from product vector
✅ User vector initialized: dimension=384
💾 Saving user vector to Qdrant: user_id=3
✅✅✅ TRACKING COMPLETE: VIEW interaction for user 3 on product 15
```

**Flow (First Interaction - Cold Start):**
1. Lookup product vector in Qdrant `products` collection
   - ❌ **If not found:** Log error and abort
   - ✅ **If found:** Continue
2. Lookup user vector in Qdrant `user_preferences` collection
   - **Not found (cold start):** Initialize user vector = product vector
3. Save user vector to Qdrant `user_preferences` collection

**Flow (Subsequent Interactions):**
1. Lookup product vector (must exist)
2. Lookup existing user vector
3. Update using weighted formula:
   ```python
   weight = {VIEW: 1.0, ADD_TO_CART: 2.0, ORDER: 5.0}
   update_factor = (1 - decay) * weight * 0.05
   user_vector = user_vector * decay + product_vector * update_factor
   user_vector = normalize(user_vector)
   ```
4. Save updated user vector to Qdrant

---

## Tracking Events

### View Event
- **When:** User views product detail page
- **Where:** `ProductController.getProductById()`
- **Weight:** 1.0 (base)
- **Command:** `TrackUserInteractionCommand.view(userId, productId)`

### Add to Cart Event
- **When:** User adds product to cart
- **Where:** `CartController.addItem()` (if implemented)
- **Weight:** 2.0 (2x stronger than view)
- **Command:** `TrackUserInteractionCommand.addToCart(userId, productId, variantId)`

### Purchase Event
- **When:** Order is successfully placed
- **Where:** `PlaceOrderUseCase.trackPurchaseInteractionsAsync()`
- **Weight:** 5.0 (5x stronger than view)
- **Command:** `TrackUserInteractionCommand.purchase(retailerId, productId, variantId)`
- **Note:** Tracks each product in the order separately

---

## User Vector Mechanics

### Vector Dimension
- **Size:** 384 dimensions
- **Model:** `all-MiniLM-L6-v2` (sentence-transformers)
- **Storage:** Qdrant `user_preferences` collection

### Update Formula
```
V_new = V_old × decay + V_product × (1-decay) × weight × 0.05
V_new = normalize(V_new)
```

**Parameters:**
- `decay = 0.95` (95% retain old preferences)
- `weight = {VIEW: 1.0, ADD_TO_CART: 2.0, ORDER: 5.0}`
- `0.05` = scaling factor to prevent drastic changes

### Cold Start (New Users)
- **First interaction:** User vector = Product vector
- **Subsequent:** Uses update formula
- **Homepage recs:** Returns default/popular products if no vector

---

## Troubleshooting

### Problem: User vector not created

**Symptoms:**
```
User Vector: NOT FOUND (cold start)
```

**Check:**
1. ✅ Is product in Qdrant?
   ```bash
   docker logs -f vendora-ai-service
   # Look for: "❌ PRODUCT NOT IN QDRANT: product_id=15"
   ```

2. ✅ Did frontend pass userId?
   ```bash
   # Spring Boot logs should show:
   👁️ USER VIEW EVENT: user=3, product=15
   # NOT:
   ⚪ Anonymous view: product=15 (no tracking)
   ```

3. ✅ Was tracking successful?
   ```bash
   # AI service should show:
   ✅✅✅ TRACKING COMPLETE: VIEW interaction for user 3 on product 15
   ```

### Problem: Tracking fails silently

**Check Spring Boot logs:**
```bash
docker logs -f vendora-spring-boot
# Look for:
⚠️ Failed to forward interaction to AI service: Connection refused
```

**Solution:**
- Ensure AI service is running: `docker ps | grep vendora-ai-service`
- Check network connectivity
- Verify `AI_SERVICE_URL=http://vendora-ai-service:8000` in env

### Problem: Products not in Qdrant

**Solution: Bulk ingest products**
```bash
# From seed directory:
python ingest_csv_to_db.py
python init_qdrant.py
```

---

## Logging Guide

### Spring Boot Logs

**View tracking:**
```
👁️ USER VIEW EVENT: user=3, product=15
✅ Saved interaction to DB: user=3, product=15, type=VIEW, interactionId=42
🔄 Forwarding to AI service: user=3, product=15, type=VIEW
🌐 HTTP POST to AI service: /ai/recommend/analytics/track - user=3, product=15, action=VIEW
✅ AI service response received: {success: true}
```

**Purchase tracking:**
```
🛒 PURCHASE EVENT: user=1, product=10, variant=20, qty=5
✅ Saved interaction to DB: user=1, product=10, type=ORDER, interactionId=43
🔄 Forwarding to AI service: user=1, product=10, type=ORDER
```

### AI Service Logs

**Successful tracking (cold start):**
```
📥 RECEIVED TRACKING REQUEST: user=3, product=15, action=VIEW
🔍 TRACKING INTERACTION: user=3, product=15, sku=None, action=VIEW
🔍 Looking up product vector: product_id=15
✅ Product vector found: dimension=384
🔍 Looking up user vector: user_id=3
🆕 COLD START: Initializing user vector from product vector
✅ User vector initialized: dimension=384
💾 Saving user vector to Qdrant: user_id=3
✅✅✅ TRACKING COMPLETE: VIEW interaction for user 3 on product 15
✅ TRACKING SUCCESSFUL: user=3, product=15
```

**Product not in Qdrant:**
```
📥 RECEIVED TRACKING REQUEST: user=3, product=999, action=VIEW
🔍 TRACKING INTERACTION: user=3, product=999, sku=None, action=VIEW
🔍 Looking up product vector: product_id=999
❌ PRODUCT NOT IN QDRANT: product_id=999, sku=None - Cannot track interaction!
💡 Solution: Product must be ingested to Qdrant first via /ai/ingest/product
```

**Homepage recommendation:**
```
🆕 HOMEPAGE RECOMMENDATION REQUEST (NEW USER)
User ID: 3
User Vector: NOT FOUND (cold start)
===============================================================================
```

**After tracking:**
```
🔍 HOMEPAGE RECOMMENDATION REQUEST
User ID: 3
User Vector found in Qdrant:
  - Dimension: 384
  - First 10 values: [0.123, -0.456, 0.789, ...]
  - Last 10 values: [..., 0.234, -0.567, 0.890]
  - Vector norm: 1.0000
===============================================================================
```

---

## Testing Checklist

### 1. Verify Product Ingestion
```bash
# Check Qdrant has products
docker exec -it vendora-qdrant sh
# Use Qdrant dashboard: http://localhost:6333/dashboard
```

### 2. Test View Tracking
```bash
# Call API with userId
curl -X GET "http://localhost:8080/api/v1/products/1?userId=3"

# Check Spring Boot logs
docker logs -f vendora-spring-boot | grep "👁️"

# Check AI service logs
docker logs -f vendora-ai-service | grep "✅✅✅"
```

### 3. Verify User Vector Created
```bash
# Call homepage recommendations
curl -X GET "http://localhost:8000/ai/recommend/homepage/3?limit=6"

# Should show:
# User Vector found in Qdrant (not "NOT FOUND")
```

### 4. Test Purchase Tracking
```bash
# Place an order
curl -X POST "http://localhost:8080/api/v1/orders" -H "Content-Type: application/json" -d '{
  "retailerId": 1,
  "items": [{"productId": 1, "variantId": 1, "quantity": 10}]
}'

# Check for 🛒 PURCHASE EVENT in logs
docker logs -f vendora-spring-boot | grep "🛒"
```

---

## Summary

**Flow:**
1. Frontend → Spring Boot (ProductController)
2. Spring Boot → MySQL (save interaction)
3. Spring Boot → AI Service (async, non-blocking)
4. AI Service → Qdrant (lookup product vector)
5. AI Service → Qdrant (update user vector)

**Critical Points:**
- Products MUST be in Qdrant first
- userId MUST be passed from frontend
- Tracking is fire-and-forget (failures don't affect UX)
- User vectors are built incrementally
- Cold start = default recommendations

**Next Steps:**
1. Ensure all products are ingested: `python init_qdrant.py`
2. Test with userId: `GET /api/v1/products/1?userId=3`
3. Verify logs show ✅✅✅ TRACKING COMPLETE
4. Test recommendations: `GET /ai/recommend/homepage/3`
5. Confirm user vector exists (not cold start)
