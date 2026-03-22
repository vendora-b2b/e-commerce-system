-- V1__init_schema.sql
-- Initial schema migration for vendora B2B e-commerce system

-- -------------------------------------------------------
-- 1. suppliers
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone               VARCHAR(255),
    address             VARCHAR(255),
    profile_picture     VARCHAR(255),
    profile_description VARCHAR(1000),
    business_license    VARCHAR(255) NOT NULL,
    rating              DOUBLE,
    verified            BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_email (email),
    UNIQUE KEY uk_supplier_license (business_license)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 2. retailers
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS retailers (
    id                    BIGINT NOT NULL AUTO_INCREMENT,
    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    phone                 VARCHAR(255),
    address               VARCHAR(255),
    profile_picture       VARCHAR(255),
    profile_description   VARCHAR(1000),
    business_license      VARCHAR(255) NOT NULL,
    loyalty_tier          VARCHAR(20) NOT NULL,
    credit_limit          DOUBLE,
    total_purchase_amount DOUBLE,
    loyalty_points        INTEGER,
    account_status        VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_retailer_email (email),
    UNIQUE KEY uk_retailer_license (business_license)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 3. categories
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    name       VARCHAR(200) NOT NULL,
    slug       VARCHAR(200),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 4. users
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                     BIGINT NOT NULL AUTO_INCREMENT,
    username               VARCHAR(50) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    role                   VARCHAR(20) NOT NULL,
    entity_id              BIGINT NOT NULL,
    enabled                BIT(1) NOT NULL,
    account_locked         BIT(1) NOT NULL,
    failed_login_attempts  INTEGER,
    last_login_at          DATETIME(6),
    created_at             DATETIME(6) NOT NULL,
    updated_at             DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_entity_role (entity_id, role),
    KEY idx_user_username (username),
    KEY idx_user_role (role),
    KEY idx_user_entity (entity_id),
    KEY idx_user_enabled (enabled),
    KEY idx_user_locked (account_locked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 5. products
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id                    BIGINT NOT NULL AUTO_INCREMENT,
    sku                   VARCHAR(50) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    description           VARCHAR(2000),
    supplier_id           BIGINT NOT NULL,
    base_price            DOUBLE NOT NULL,
    minimum_order_quantity INTEGER NOT NULL,
    unit                  VARCHAR(50) NOT NULL,
    created_at            DATETIME(6) NOT NULL,
    updated_at            DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_sku (sku),
    KEY idx_supplier_id (supplier_id),
    KEY idx_sku (sku),
    KEY idx_base_price (base_price),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 6. product_variants
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    product_id       BIGINT NOT NULL,
    sku              VARCHAR(100),
    color            VARCHAR(50),
    size             VARCHAR(50),
    price_adjustment DOUBLE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_variant_sku (sku),
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 7. product_categories (join table)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_categories (
    product_id  BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_pc_product  FOREIGN KEY (product_id)  REFERENCES products (id),
    CONSTRAINT fk_pc_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 8. product_images (element collection)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_images (
    product_id BIGINT NOT NULL,
    image_url  VARCHAR(500),
    CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 9. price_tiers
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS price_tiers (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    product_id       BIGINT NOT NULL,
    min_quantity     INTEGER NOT NULL,
    max_quantity     INTEGER,
    discount_percent DOUBLE,
    PRIMARY KEY (id),
    CONSTRAINT fk_tier_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 10. orders
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    order_number     VARCHAR(255) NOT NULL,
    retailer_id      BIGINT NOT NULL,
    supplier_id      BIGINT NOT NULL,
    total_amount     DOUBLE,
    status           VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500),
    order_date       DATETIME(6) NOT NULL,
    delivery_date    DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_number (order_number),
    KEY idx_order_retailer (retailer_id),
    KEY idx_order_supplier (supplier_id),
    KEY idx_order_status (status),
    KEY idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 11. order_items
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    order_id     BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    variant_id   BIGINT,
    quantity     INTEGER NOT NULL,
    price        DOUBLE NOT NULL,
    product_name VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 12. inventory
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id        BIGINT NOT NULL,
    product_id         BIGINT NOT NULL,
    variant_id         BIGINT,
    available_quantity INTEGER NOT NULL,
    reserved_quantity  INTEGER NOT NULL,
    reorder_level      INTEGER,
    reorder_quantity   INTEGER,
    warehouse_location VARCHAR(255),
    last_restocked     DATETIME(6),
    last_updated       DATETIME(6),
    status             VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_product_variant (product_id, variant_id),
    KEY idx_inventory_supplier (supplier_id),
    KEY idx_inventory_product (product_id),
    KEY idx_inventory_variant (variant_id),
    KEY idx_inventory_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 13. quotations
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS quotations (
    id                    BIGINT NOT NULL AUTO_INCREMENT,
    quotation_number      VARCHAR(255) NOT NULL,
    retailer_id           BIGINT NOT NULL,
    supplier_id           BIGINT NOT NULL,
    status                VARCHAR(30) NOT NULL,
    retailer_notes        TEXT,
    supplier_notes        TEXT,
    terms_and_conditions  TEXT,
    valid_until           DATETIME(6),
    order_id              BIGINT,
    created_at            DATETIME(6),
    responded_at          DATETIME(6),
    finalized_at          DATETIME(6),
    cancelled_at          DATETIME(6),
    cancellation_reason   TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_quotation_number (quotation_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 14. quotation_items
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS quotation_items (
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    quotation_id            BIGINT NOT NULL,
    variant_id              BIGINT NOT NULL,
    product_id              BIGINT,
    requested_quantity      INTEGER NOT NULL,
    target_price            DOUBLE,
    requested_delivery_date DATE,
    retailer_notes          TEXT,
    item_status             VARCHAR(20) NOT NULL,
    offered_quantity        INTEGER,
    offered_price           DOUBLE,
    offered_delivery_date   DATE,
    lead_time_days          INTEGER,
    supplier_notes          TEXT,
    rejection_reason        TEXT,
    retailer_action         VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT fk_qi_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 15. chat_sessions
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_sessions (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    session_token   VARCHAR(100) NOT NULL,
    user_id         BIGINT NOT NULL,
    title           VARCHAR(200),
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    last_message_at DATETIME(6),
    active          BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_token (session_token),
    KEY idx_chat_session_user (user_id),
    KEY idx_chat_session_token (session_token),
    KEY idx_chat_session_active (active),
    KEY idx_chat_session_last_message (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 16. chat_messages
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role       VARCHAR(20) NOT NULL,
    content    TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_message_session (session_id),
    KEY idx_chat_message_role (role),
    KEY idx_chat_message_created (created_at),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 17. notifications
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    user_id        BIGINT NOT NULL,
    type           VARCHAR(30) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    message        VARCHAR(1000),
    reference_id   BIGINT,
    reference_type VARCHAR(50),
    is_read        BIT(1) NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    read_at        DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_notification_user (user_id),
    KEY idx_notification_user_read (user_id, is_read),
    KEY idx_notification_created (created_at),
    KEY idx_notification_reference (reference_id, reference_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 18. user_interactions
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_interactions (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    user_id          BIGINT NOT NULL,
    product_id       BIGINT NOT NULL,
    variant_id       BIGINT,
    interaction_type VARCHAR(30) NOT NULL,
    session_id       VARCHAR(100),
    created_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_interaction_user (user_id),
    KEY idx_interaction_product (product_id),
    KEY idx_interaction_type (interaction_type),
    KEY idx_interaction_created (created_at),
    KEY idx_interaction_user_product (user_id, product_id),
    KEY idx_interaction_user_type (user_id, interaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 19. user_interaction_metadata (element collection map)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_interaction_metadata (
    interaction_id BIGINT NOT NULL,
    meta_key       VARCHAR(50) NOT NULL,
    meta_value     VARCHAR(255),
    CONSTRAINT fk_meta_interaction FOREIGN KEY (interaction_id) REFERENCES user_interactions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
