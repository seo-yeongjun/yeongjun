package com.yeongjun.yeongjun.home.widget.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BalanceGame {
    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private LocalDateTime createdAt;

    // 통계 및 렌더링용 임시 필드
    private int countA;
    private int countB;
    private int totalCount;
    private double percentA;
    private double percentB;
    private boolean voted; // 현재 IP가 이미 투표에 참여했는지 여부
}
