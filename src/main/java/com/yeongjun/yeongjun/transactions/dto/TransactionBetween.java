package com.yeongjun.yeongjun.transactions.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TransactionBetween {
    private String username;         // 사용자 이름
    private Date start_dt;
    private Date end_dt;
}
