package com.yeongjun.yeongjun.transactions.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TransactionsCategory {
    private Long transaction_category_id; // 카테고리 ID (PK)
    private Integer income_expense_gb;    // 수입(1) / 지출(2) 구분
    private String category_nm;          // 카테고리 이름
    private Long parents_category_id;     // 상위 카테고리 ID
}