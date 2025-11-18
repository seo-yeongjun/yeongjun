package com.yeongjun.yeongjun.babfullmenu.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MenuVote {
    private Long id;
    private String normalizedMenuName; // REGEXP_REPLACE로 정규화된 메뉴명
    private String voteType; // "LIKE" or "DISLIKE"
    private String sessionId; // 세션 ID 또는 쿠키 ID
    private LocalDateTime createdAt;
}

