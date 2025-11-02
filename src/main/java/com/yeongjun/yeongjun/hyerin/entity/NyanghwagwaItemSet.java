package com.yeongjun.yeongjun.hyerin.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NyanghwagwaItemSet {
    private Long set_id;
    private String set_name;
    private String description;
    private Long naver_product_no;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
