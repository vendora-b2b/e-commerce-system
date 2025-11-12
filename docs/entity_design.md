# B2B E-Commerce Marketplace: System Control Flow Report
## Design of entities

### Supplier Entity

| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **sID** | UUID | Primary Key |
| **name** | String | Not Null |
| **email** | String | Not Null, Unique |
| **hashedPwd** | String | Not Null |
| **businessLicense** | String | Not Null, Unique |
| **phone** | String | Not Null |
| **address** | String | Not Null |
| **isVerified** | Boolean | Default: false |
| **avgRating** | Float | Default:  null |
| **profilePicture** | String | Default: null |
| **profileDescription** | String | Default: null |

### Retailer Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **rID** | UUID | Primary Key |
| **name** | String | Not Null |
| **email** | String | Not Null, Unique |
| **hashedPwd** | String | Not Null |
| **businessLicense** | String | Not Null, Unique |
| **phone** | String | Not Null |
| **address** | String | Not Null |
| **isVerified** | Boolean | Default: false |
| **avgRating** | Float | Default:  null |
| **profilePicture** | String | Default: null |
| **profileDescription** | String | Default: null |
| **loyaltyPoints** | Integer | Default: 0 |
| **loyaltyTier** | Enum (Bronze, Silver, Gold, Platinum) | Default: Bronze |
| **creditLimits** | Float | Default: 0.0 |
| **totalPurchasedAmount** | Float | Default: 0.0 |

### Category Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **cID** | UUID | Primary Key |
| **categoryName** | String | Not Null, Unique |

### Product Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **pID** | UUID | Primary Key |
| **productName** | String | Not Null |
| **description** | String | Not Null |
| **categoryIDs** | Array of UUIDs | Foreign Key (references Category.cID), Not Null |
| **mainImageURL** | String | Default: null |
| **additionalImageURLs** | Array of Strings | Default: null |
| **unit** | String | Not Null |
| **ingredients** | String | Default: null |
| **createdAt** | Timestamp | Default: current timestamp |
| **updatedAt** | Timestamp | Default: current timestamp on update |

- createdAt: When the product was first created
- updatedAt: Tracks the changes made to the product information (e.g., description, images, categories)

### Product Variant Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **vID** | UUID | Primary Key |
| **variantName** | String | Not Null |
| **productID** | UUID | Foreign Key (references Product.pID), Not Null |
| **sku** | String | Not Null, Unique |
| **basePrice** | Float | Not Null |
| **finalPrice** | Float | Not Null |
| **minimumOrderQuantity** | Integer | Default: 1 |
| **priceTiers** | Array of Price Tier Entities | Default: null |
| **createdAt** | Timestamp | Default: current timestamp |
| **updatedAt** | Timestamp | Default: current timestamp on update |

- createdAt: When the variant was first created
- updatedAt: Tracks the changes made to the variant information (e.g., price, SKU, name)

### Price Tier Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **tierID** | UUID | Primary Key |
| **variantID** | UUID | Foreign Key (references ProductVariant.vID), Not Null |
| **minQuantity** | Integer | Not Null |
| **maxQuantity** | Integer | Default: null (null means unlimited) |
| **pricePerUnit** | Float | Not Null |

### Order Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **oID** | UUID | Primary Key |
| **orderNumber** | String | Not Null, Unique |
| **retailerID** | UUID | Foreign Key (references Retailer.rID), Not Null |
| **supplierID** | UUID | Foreign Key (references Supplier.sID), Not Null |
| **totalAmount** | Float | Not Null |
| **status** | Enum (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED) | Default: PENDING |
| **shippingAddress** | String | Not Null |
| **orderDate** | Timestamp | Default: current timestamp |
| **expectedDeliveryDate** | Timestamp | Default: null |
| **actualDeliveryDate** | Timestamp | Default: null |

### Order Item Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **oiID** | UUID | Primary Key |
| **orderID** | UUID | Foreign Key (references Order.oID), Not Null |
| **variantID** | UUID | Foreign Key (references ProductVariant.vID), Not Null |
| **quantity** | Integer | Not Null |
| **unitPrice** | Float | Not Null (price at time of order) |
| **finalTotalPrice** | Float | Not Null |
| **specifications** | String | Default: null |
| **notes** | String | Default: null |

### Inventory Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **iID** | UUID | Primary Key |
| **supplierID** | UUID | Foreign Key (references Supplier.sID), Not Null |
| **variantID** | UUID | Foreign Key (references ProductVariant.vID), Not Null |
| **availableQuantity** | Integer | Default: 0 |
| **reservedQuantity** | Integer | Default: 0 |
| **reorderLevel** | Integer | Default: 10 |
| **reorderQuantity** | Integer | Default: 100 |
| **maxStockLevel** | Integer | Default: 10000 |
| **warehouseLocation** | String | Default: null |
| **status** | Enum (AVAILABLE, OUT_OF_STOCK, DISCONTINUED, LOW_STOCK) | Default: AVAILABLE |
| **createdAt** | Timestamp | Default: current timestamp |
| **updatedAt** | Timestamp | Default: current timestamp on update |

- createdAt: When the variant is first registered in inventory
- updatedAt: Tracks the changes made to the stock levels and status (an order is placed, stock is replenished or status changes)

### Quotation Request Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **qrID** | UUID | Primary Key |
| **requestNumber** | String | Not Null, Unique |
| **retailerID** | UUID | Foreign Key (references Retailer.rID), Not Null |
| **supplierID** | UUID | Foreign Key (references Supplier.sID), Not Null |
| **status** | Enum (DRAFT, SENT, RESPONDED, ACCEPTED, REJECTED, EXPIRED, CANCELLED) | Default: DRAFT |
| **requestDate** | Timestamp | Default: current timestamp |
| **validUntil** | Timestamp | Not Null |
| **notes** | String | Default: null |
| **createdBy** | UUID | Foreign Key (references UserAuthentication.authID), Not Null |
| **lastUpdatedAt** | Timestamp | Default: current timestamp on update |

### Quotation Request Item Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **qriID** | UUID | Primary Key |
| **quotationRequestID** | UUID | Foreign Key (references QuotationRequest.qrID), Not Null |
| **productID** | UUID | Foreign Key (references Product.pID), Not Null |
| **quantity** | Integer | Not Null |
| **specifications** | String | Default: null |
| **notes** | String | Default: null |

### Quotation Offer Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **qoID** | UUID | Primary Key |
| **offerNumber** | String | Not Null, Unique |
| **quotationRequestID** | UUID | Foreign Key (references QuotationRequest.qrID), Not Null |
| **retailerID** | UUID | Foreign Key (references Retailer.rID), Not Null |
| **supplierID** | UUID | Foreign Key (references Supplier.sID), Not Null |
| **status** | Enum (DRAFT, SUBMITTED, ACCEPTED, REJECTED, EXPIRED, WITHDRAWN) | Default: DRAFT |
| **totalAmount** | Float | Not Null |
| **offerDate** | Timestamp | Default: current timestamp |
| **validUntil** | Timestamp | Not Null |
| **notes** | String | Default: null |
| **termsAndConditions** | String | Default: null |
| **decisionReason** | String | Default: null |
| **decisionDate** | Timestamp | Default: null |
| **createdBy** | UUID | Foreign Key (references UserAuthentication.authID), Not Null |
| **lastUpdatedAt** | Timestamp | Default: current timestamp on update |

### Quotation Offer Item Entity
| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **qoiID** | UUID | Primary Key |
| **quotationOfferID** | UUID | Foreign Key (references QuotationOffer.qoID), Not Null |
| **productID** | UUID | Foreign Key (references Product.pID), Not Null |
| **quantity** | Integer | Not Null |
| **quotedPrice** | Float | Not Null |
| **specifications** | String | Default: null |
| **notes** | String | Default: null |

## The core concepts of handling products and variants
### Product and Product Variant Relationship
- Each product can have multiple variants to accommodate different options (e.g., size, color).
- We have two separate entities: Product and Product Variant.
- A product has its **productID**, a variant has its **variantID** and is linked to a product via **productID**.
### Problems with management and tracking
- In the old inventory system, we use **productID** as the main identifier for stock management. This caused issues when different variants of the same product had different stock levels.
- For example, if a product "T-Shirt" has variants "Red - Size M" and "Blue - Size L", managing stock at the product level would not reflect the actual availability of each variant.
- In the old order processing system, orders were placed at the product level, making it difficult to track which specific variant was ordered.
### Solutions implemented
We will consider one product has at least one variant. In other words, product entity is the **container** of its variants (**actually sellable items**):
- Updated the inventory management system to track stock levels for each variant individually.
- Modified the order processing system to include variant information in order details.

However, a new challenge arose: **what happens if a product does not have any variants?** (e.g., paracetamol 500mg tablets)

$\rightarrow$ The assumption forces us to create a dummy variant for such products. This dummy variant will have the same name as the product and default values for other fields (e.g., SKU, price). 

**Next nightmare:** How if the product ***later needs variants?*** (e.g., paracetamol 250mg tablets, paracetamol 1000mg tablets)

$\rightarrow$ We will need to migrate the existing dummy variant to a real variant and create additional variants as needed. 

**How to handle this migration smoothly?**

Let's review the design of the product and product variant entities again.

**Product Entity**

| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **pID** | UUID | Primary Key |
| **productName** | String | Not Null |
| **description** | String | Not Null |
| **categoryIDs** | Array of UUIDs | Foreign Key (references Category.cID), Not Null |
| **mainImageURL** | String | Default: null |
| **additionalImageURLs** | Array of Strings | Default: null |
| **unit** | String | Not Null |
| **ingredients** | String | Default: null |
| **createdAt** | Timestamp | Default: current timestamp |
| **updatedAt** | Timestamp | Default: current timestamp on update |

- createdAt: When the product was first created
- updatedAt: Tracks the changes made to the product information (e.g., description, images, categories)

**Product Variant Entity**

| Fields | Data Types | Constraints |
| :--- | :--- | :--- |
| **vID** | UUID | Primary Key |
| **variantName** | String | Not Null |
| **productID** | UUID | Foreign Key (references Product.pID), Not Null |
| **sku** | String | Not Null, Unique |
| **basePrice** | Float | Not Null |
| **finalPrice** | Float | Not Null |
| **minimumOrderQuantity** | Integer | Not Null, Default: 1 |
| **priceTiers** | Array of Price Tier Entities | Default: null |
| **createdAt** | Timestamp | Default: current timestamp |
| **updatedAt** | Timestamp | Default: current timestamp on update |

- createdAt: When the variant was first created
- updatedAt: Tracks the changes made to the variant information (e.g., price, SKU, name)

Let's rewrite from:

$$\text{One product has at least one variant}$$

to:

$$\text{One product cannot exist without having at least one variant}$$

Comeback to the Paracetamol example:

1. Initially, the supplier creates a product "Paracetamol 500mg Tablets", in the UI, we need a **check box** or **toggle switch**: "Does this product have variants?"

2. If the supplier selects "Yes", they can proceed to add variants immediately by providing the **variantName**, **sku**, **basePrice** and **finalPrice** for each variant. The system will create the product and its variants in the database accordingly.

3. If the supplier selects "No", they have to give us the **sku**, **basePrice** and **finalPrice** for the dummy variant right away. Then we can confidently create both the product and its dummy variant in the database.

4. If the supplier later decides to add variants (e.g., "Paracetamol 250mg Tablets", "Paracetamol 1000mg Tablets"), they can go to the product management page, select **Edit** on the product, and change the "Does this product have variants?" option to **"Yes"**, then they can change the product name into "Paracetamol Tablets" and add the new variants accordingly. The existing dummy variant will be updated to reflect the correct information for "Paracetamol 500mg Tablets".