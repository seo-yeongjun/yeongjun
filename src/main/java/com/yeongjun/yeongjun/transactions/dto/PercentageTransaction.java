package com.yeongjun.yeongjun.transactions.dto;

import com.yeongjun.yeongjun.transactions.model.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PercentageTransaction{
    private Long category_id;
    private String category_nm;
    private int income_expense_gb;
    private Integer totalAmount;
    private float percentage;
    private Date start_transaction_date;
    private Date end_transaction_date;
    private List<Transaction> transactions;
}
