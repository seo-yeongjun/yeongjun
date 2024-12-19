package com.yeongjun.yeongjun.transactions.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    private Long transaction_id;      // 거래 ID (PK)
    private String username;         // 사용자 이름
    private Long category_id;         // 카테고리 ID
    private int income_expense_gb;
    private String category_nm;         // 카테고리 ID
    @Min(value = 1, message = "금액은 1 이상이어야 합니다.")
    private Integer amount;          // 거래 금액
    private Date transaction_date;    // 거래 날짜
    private String memo;             // 메모
    private LocalDateTime created_at; // 생성 시각
}


