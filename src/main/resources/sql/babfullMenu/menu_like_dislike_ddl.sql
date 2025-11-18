-- 좋아요/싫어요 집계 테이블
CREATE TABLE IF NOT EXISTS menu_like_dislike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    normalized_menu_name VARCHAR(255) NOT NULL UNIQUE COMMENT '정규화된 메뉴명 (한글만)',
    like_count INT DEFAULT 0 NOT NULL COMMENT '좋아요 수',
    dislike_count INT DEFAULT 0 NOT NULL COMMENT '싫어요 수',
    INDEX idx_normalized_menu_name (normalized_menu_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴별 좋아요/싫어요 집계';

-- 사용자 투표 기록 테이블
CREATE TABLE IF NOT EXISTS menu_vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    normalized_menu_name VARCHAR(255) NOT NULL COMMENT '정규화된 메뉴명 (한글만)',
    vote_type VARCHAR(10) NOT NULL COMMENT '투표 타입 (LIKE 또는 DISLIKE)',
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID (쿠키 기반)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '투표 생성 시간',
    UNIQUE KEY uk_menu_session (normalized_menu_name, session_id),
    INDEX idx_normalized_menu_name (normalized_menu_name),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자별 메뉴 투표 기록';

