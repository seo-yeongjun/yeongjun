-- Nyanghwagwa inventory core tables
CREATE TABLE nyanghwagwa_items (
    item_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name      VARCHAR(100) NOT NULL UNIQUE,
    quantity       INT NOT NULL DEFAULT 0,
    unit           VARCHAR(20),
    description    TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE nyanghwagwa_item_sets (
    set_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_name       VARCHAR(100) NOT NULL UNIQUE,
    description    TEXT,
    naver_product_no BIGINT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE nyanghwagwa_item_set_entries (
    set_id             BIGINT NOT NULL,
    item_id            BIGINT NOT NULL,
    required_quantity  INT   NOT NULL CHECK (required_quantity > 0),
    PRIMARY KEY (set_id, item_id),
    CONSTRAINT fk_nyanghwagwa_entry_set
        FOREIGN KEY (set_id) REFERENCES nyanghwagwa_item_sets(set_id) ON DELETE CASCADE,
    CONSTRAINT fk_nyanghwagwa_entry_item
        FOREIGN KEY (item_id) REFERENCES nyanghwagwa_items(item_id) ON DELETE CASCADE
);

CREATE TABLE nyanghwagwa_inventory_logs (
    log_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_type         ENUM('PRODUCE', 'SALE') NOT NULL,
    set_id           BIGINT NULL,
    item_id          BIGINT NULL,
    quantity_change  INT NOT NULL,
    remaining_stock  INT NOT NULL,
    performed_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    actor_username   VARCHAR(100),
    notes            TEXT,
    CONSTRAINT fk_nyanghwagwa_log_set
        FOREIGN KEY (set_id) REFERENCES nyanghwagwa_item_sets(set_id) ON DELETE SET NULL,
    CONSTRAINT fk_nyanghwagwa_log_item
        FOREIGN KEY (item_id) REFERENCES nyanghwagwa_items(item_id) ON DELETE SET NULL
);
