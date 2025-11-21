-- Like/Dislike aggregate per date and raw menu name
CREATE TABLE IF NOT EXISTS menu_like_dislike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_date DATE NOT NULL,
    menu_name VARCHAR(255) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    dislike_count INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_menu_date_name (menu_date, menu_name),
    INDEX idx_menu_date (menu_date),
    INDEX idx_menu_name (menu_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Per-date menu like/dislike aggregate';

-- User vote history per date and raw menu name
CREATE TABLE IF NOT EXISTS menu_vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_date DATE NOT NULL,
    menu_name VARCHAR(255) NOT NULL,
    vote_type VARCHAR(10) NOT NULL COMMENT 'LIKE or DISLIKE',
    session_id VARCHAR(255) NOT NULL COMMENT 'session ID (cookie based)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'vote timestamp',
    UNIQUE KEY uk_menu_date_name_session (menu_date, menu_name, session_id),
    INDEX idx_menu_date (menu_date),
    INDEX idx_menu_name (menu_name),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User menu vote history';

