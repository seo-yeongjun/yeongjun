package com.yeongjun.yeongjun.home.widget.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BalanceGameVote {
    private Long id;
    private Long questionId;
    private String ipAddress;
    private String selection;
    private LocalDateTime votedAt;
}
