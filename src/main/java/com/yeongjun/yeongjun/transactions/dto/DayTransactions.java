package com.yeongjun.yeongjun.transactions.dto;

import com.yeongjun.yeongjun.transactions.model.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DayTransactions {
    private int year;
    private int month;
    private int day;
    private int sumOfPlusAmounts;
    private int sumOfMinusAmounts;
    private List<Transaction> transactions;

    // 생성자 / Getter / Setter 생략
}
